package org.lime.swiftCore.kit;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lime.swiftCore.SwiftCore;
import org.lime.swiftCore.api.CustomKitAPI;

public class QueueManager {
   private final SwiftCore plugin;
   private final Map<String, Set<UUID>> queues;
   private final Map<String, Set<UUID>> fightingPlayers;
   private final Set<UUID> allQueuedPlayers;
   private final Set<UUID> allFightingPlayers;
   private final Map<UUID, Map<String, Long>> queueJoinTimes;
   private final Map<UUID, Set<String>> playerToQueues;
   private final Map<UUID, String> playerToFightingKit;
   private QueueManager._b matchFoundCallback;

   public QueueManager(SwiftCore var1) {
      this.plugin = var1;
      this.queues = new ConcurrentHashMap<>();
      this.fightingPlayers = new ConcurrentHashMap<>();
      this.allQueuedPlayers = ConcurrentHashMap.newKeySet();
      this.allFightingPlayers = ConcurrentHashMap.newKeySet();
      this.queueJoinTimes = new ConcurrentHashMap<>();
      this.playerToQueues = new ConcurrentHashMap<>();
      this.playerToFightingKit = new ConcurrentHashMap<>();
   }

   public void setMatchFoundCallback(QueueManager._b var1) {
      this.matchFoundCallback = var1;
   }

   public boolean addToQueue(UUID var1, String var2) {
      String var3 = var2.toLowerCase();
      Player var4 = Bukkit.getPlayer(var1);
      if (var4 != null
         && m.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            this.plugin, var4, var3
         )) {
         return false;
      } else if (var4 != null && ColorPartyKitGuard.blockQueueUse(this.plugin, var4, var3)) {
         return false;
      } else if (var4 != null
         && w.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            this.plugin, var4, var3, "queue"
         )) {
         return false;
      } else if (var4 != null
         && !this.plugin
            .getKitManager()
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
               var4, var3
            )) {
         return false;
      } else if (var4 != null && this.isBusyForQueue(var4)) {
         return false;
      } else if (var4 != null
         && this.plugin.getSpectatorManager() != null
         && this.plugin
            .getSpectatorManager()
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
               var1
            )) {
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var4, "queue-already-in-match"
            );
         return false;
      } else {
         if (this.plugin.getRankedManager() != null && this.plugin.getRankedManager().isInQueue(var1)) {
            this.plugin.getRankedManager().claimQueuesForMatch(var1);
         }

         Set var5 = this.playerToQueues.computeIfAbsent(var1, var0 -> ConcurrentHashMap.newKeySet());
         if (var5.contains(var3)) {
            return false;
         } else {
            if (!this.plugin
                  .getMultiQueueManager()
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     false
                  )
               && !var5.isEmpty()) {
               this.removeFromAllQueues(var1);
               var5 = this.playerToQueues.computeIfAbsent(var1, var0 -> ConcurrentHashMap.newKeySet());
            }

            if (!this.plugin
               .getMultiQueueManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  false, var5.size()
               )) {
               if (var4 != null) {
                  this.plugin
                     .getMessagesManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var4,
                        "multi-queue-limit-reached",
                        Map.of(
                           "limit",
                           String.valueOf(
                              this.plugin
                                 .getMultiQueueManager()
                                 .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                    false
                                 )
                           )
                        )
                     );
               }

               return false;
            } else {
               boolean var6 = this.queues.computeIfAbsent(var3, var0 -> ConcurrentHashMap.newKeySet()).add(var1);
               if (var6) {
                  this.allQueuedPlayers.add(var1);
                  var5.add(var3);
                  this.queueJoinTimes.computeIfAbsent(var1, var0 -> new ConcurrentHashMap<>()).put(var3, System.currentTimeMillis());
                  if (var4 != null && this.plugin.getDuelManager() != null) {
                     String var7 = this.plugin
                           .getMultiQueueManager()
                           .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                              false
                           )
                        ? this.plugin
                           .getMultiQueueManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var4, false
                           )
                        : this.resolveQueueDisplayName(var1, var3);
                     this.plugin
                        .getDuelManager()
                        .getMatchFoundEffects()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var4, var7, var3
                        );
                  }

                  if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
                     String var9 = var4 != null ? var4.getName() : var1.toString();
                     if (this.plugin.isDebug()) {
                        this.plugin
                           .getLogger()
                           .info("[CrossServer Debug] Adding " + var9 + " to GLOBAL queue '" + var3 + "' (cross-server enabled). Local matchmaking skipped.");
                     }

                     this.plugin
                        .getLimboManager()
                        .getGlobalQueueManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var1, var9, var3
                        );
                  } else {
                     if (this.plugin.isDebug()) {
                        boolean var8 = this.plugin.getLimboManager() == null;
                        this.plugin
                           .getLogger()
                           .info(
                              "[CrossServer Debug] Adding "
                                 + var1
                                 + " to LOCAL queue '"
                                 + var3
                                 + "' (limboManager="
                                 + (var8 ? "null" : "present")
                                 + ", isCrossServer="
                                 + (var8 ? "n/a" : this.plugin.getLimboManager().isCrossServer())
                                 + "). Running local matchmaking."
                           );
                     }

                     Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> this.tryMatchmaking(var3));
                  }
               }

               return var6;
            }
         }
      }
   }

   private String resolveQueueDisplayName(UUID var1, String var2) {
      if (var2 != null && var2.startsWith("customkit:") && this.plugin.getCustomKitAPI() != null) {
         CustomKitAPI.CustomKitData var3 = this.plugin.getCustomKitAPI().getKit(var1);
         if (var3 != null && var3.displayName() != null && !var3.displayName().isBlank()) {
            return var3.displayName();
         }
      }

      return this.plugin
         .getDataManager()
         .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
            var2
         );
   }

   private void tryMatchmaking(String var1) {
      Set var2 = this.queues.get(var1);
      if (var2 != null && var2.size() >= 2) {
         UUID[] var3 = var2.toArray(new UUID[0]);
         if (var3.length >= 2) {
            UUID var4 = var3[0];
            UUID var5 = var3[1];
            if (var4.equals(var5)) {
               var2.remove(var4);
               this.removeMembership(var4, var1);
            } else if (var2.remove(var4) && var2.remove(var5)) {
               long var6 = this.getWaitTime(var4, var1);
               long var8 = this.getWaitTime(var5, var1);
               this.plugin
                  .getQueueMatchTimeTracker()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1, var6, var8
                  );
               this.removeMembership(var4, var1);
               this.removeMembership(var5, var1);
               Player var10 = Bukkit.getPlayer(var4);
               Player var11 = Bukkit.getPlayer(var5);
               if (var10 != null && var10.isOnline() && var11 != null && var11.isOnline()) {
                  if (this.isBusyForQueue(var10) || this.isBusyForQueue(var11) || this.isSpectating(var4) || this.isSpectating(var5)) {
                     this.cleanupBusyMatchCandidate(var2, var4, var5);
                  } else if (!this.hasMatchFound(var4)
                     && !this.hasMatchFound(var5)
                     && this.plugin
                        .getMultiQueueManager()
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                           var4, var5
                        )) {
                     Set var12 = this.getPlayerQueues(var4);
                     Set var13 = this.getPlayerQueues(var5);
                     this.plugin
                        .getMultiQueueManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var4
                        );
                     this.plugin
                        .getMultiQueueManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var5
                        );
                     this.addFightingPlayer(var4, var1);
                     this.addFightingPlayer(var5, var1);
                     this.plugin
                        .getMultiQueueManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var4, var5
                        );
                     if (this.plugin.getDuelManager() != null) {
                        this.plugin
                           .getDuelManager()
                           .getMatchFoundEffects()
                           .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                              var4
                           );
                        this.plugin
                           .getDuelManager()
                           .getMatchFoundEffects()
                           .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                              var5
                           );
                     }

                     if (this.matchFoundCallback != null) {
                        Bukkit.getScheduler()
                           .runTask(
                              this.plugin,
                              () -> this.matchFoundCallback
                                    .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                       var10, var11, var1, var12, var13
                                    )
                           );
                     }
                  } else {
                     if (!this.hasMatchFound(var4)
                        && !this.plugin
                           .getMultiQueueManager()
                           .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                              var4
                           )) {
                        var2.add(var4);
                        this.addMembership(var4, var1);
                     }

                     if (!this.hasMatchFound(var5)
                        && !this.plugin
                           .getMultiQueueManager()
                           .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                              var5
                           )) {
                        var2.add(var5);
                        this.addMembership(var5, var1);
                     }
                  }
               } else {
                  if (var10 != null && var10.isOnline()) {
                     var2.add(var4);
                     this.addMembership(var4, var1);
                  }

                  if (var11 != null && var11.isOnline()) {
                     var2.add(var5);
                     this.addMembership(var5, var1);
                  }
               }
            }
         }
      }
   }

   public boolean removeFromQueue(UUID var1, String var2) {
      Set var3 = this.queues.get(var2.toLowerCase());
      if (var3 != null) {
         boolean var4 = var3.remove(var1);
         if (var4) {
            this.removeMembership(var1, var2.toLowerCase());
            if (this.plugin.getDuelManager() != null) {
               if (!this.isInAnyQueue(var1)) {
                  this.plugin
                     .getDuelManager()
                     .getMatchFoundEffects()
                     .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                        var1
                     );
               } else {
                  Player var5 = Bukkit.getPlayer(var1);
                  String var6 = this.getPlayerQueue(var1).orElse(var2);
                  if (var5 != null) {
                     String var7 = this.plugin
                        .getMultiQueueManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var5, false
                        );
                     this.plugin.getScoreboardManager().setPlaceholder(var5, "kit", var7);
                     this.plugin.getScoreboardManager().setPlaceholder(var5, "in_queue_kitname", var7);
                     this.plugin
                        .getDuelManager()
                        .getMatchFoundEffects()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var5, var7, var6
                        );
                  }
               }
            }

            if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
               this.plugin
                  .getLimboManager()
                  .getGlobalQueueManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1, var2
                  );
            }
         }

         return var4;
      } else {
         return false;
      }
   }

   public void removeFromAllQueues(UUID var1) {
      Set var2 = this.playerToQueues.remove(var1);
      if (var2 != null) {
         for (String var4 : new HashSet(var2)) {
            Set var5 = this.queues.get(var4);
            if (var5 != null) {
               var5.remove(var1);
            }

            if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
               this.plugin
                  .getLimboManager()
                  .getGlobalQueueManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1, var4
                  );
            }
         }
      }

      this.allQueuedPlayers.remove(var1);
      this.queueJoinTimes.remove(var1);
      if (this.plugin.getDuelManager() != null) {
         this.plugin
            .getDuelManager()
            .getMatchFoundEffects()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var1
            );
      }
   }

   public boolean isInQueue(UUID var1, String var2) {
      Set var3 = this.queues.get(var2.toLowerCase());
      return var3 != null && var3.contains(var1);
   }

   public boolean isQueuedForKit(UUID var1, String var2) {
      if (var2 != null && !var2.isBlank()) {
         String var3 = var2.toLowerCase();
         if (this.isInQueue(var1, var3)) {
            return true;
         } else if (this.plugin.getRankedManager() != null && this.plugin.getRankedManager().isInQueue(var1, var3)) {
            return true;
         } else if (var3.startsWith("tier")) {
            String var4 = var3.substring(4);
            return this.isInQueue(var1, var4) || this.plugin.getRankedManager() != null && this.plugin.getRankedManager().isInQueue(var1, var4);
         } else {
            return this.plugin.getRankedManager() != null && this.plugin.getRankedManager().isInQueue(var1, "tier" + var3);
         }
      } else {
         return false;
      }
   }

   public boolean isInAnyQueue(UUID var1) {
      return this.allQueuedPlayers.contains(var1);
   }

   public boolean isInFighting(UUID var1) {
      return this.allFightingPlayers.contains(var1);
   }

   public boolean hasMatchFound(UUID var1) {
      return this.isInFighting(var1);
   }

   public int getQueueSize(String var1) {
      Set var2 = this.queues.get(var1.toLowerCase());
      return var2 != null ? var2.size() : 0;
   }

   public Set<UUID> getQueue(String var1) {
      Set var2 = this.queues.get(var1.toLowerCase());
      return var2 != null ? new HashSet<>(var2) : new HashSet<>();
   }

   public Optional<String> getPlayerQueue(UUID var1) {
      return this.getPlayerQueues(var1).stream().findFirst();
   }

   public Set<String> getPlayerQueues(UUID var1) {
      Set var2 = this.playerToQueues.get(var1);
      if (var2 == null) {
         return Set.of();
      } else {
         Map var3 = this.queueJoinTimes.getOrDefault(var1, Map.of());
         return var2.stream()
            .sorted(Comparator.comparingLong(var1x -> var3.getOrDefault(var1x, Long.MAX_VALUE)))
            .collect(Collectors.toCollection(LinkedHashSet::new));
      }
   }

   public Map<String, Integer> getQueuedKitCountsSnapshot() {
      HashMap var1 = new HashMap();

      for (Entry var3 : this.queues.entrySet()) {
         var1.put((String)var3.getKey(), ((Set)var3.getValue()).size());
      }

      return var1;
   }

   private boolean isBusyForQueue(Player var1) {
      UUID var2 = var1.getUniqueId();
      return this.plugin.getDuelManager().isInMatch(var2)
         || this.plugin.getTeamQueueManager() != null
            && this.plugin
               .getTeamQueueManager()
               .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                  var2
               )
         || this.plugin.getBotDuelManager() != null && this.plugin.getBotDuelManager().isInBotDuel(var2)
         || this.plugin.getFFAManager().isInFFA(var2)
         || this.plugin.getPartyGameManager().isPlayerInPartyGame(var2)
         || this.plugin
            .getTournamentManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var2
            )
         || this.plugin
            .getEventManager()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var2
            );
   }

   private boolean isSpectating(UUID var1) {
      return this.plugin.getSpectatorManager() != null
         && this.plugin
            .getSpectatorManager()
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
               var1
            );
   }

   private void cleanupBusyMatchCandidate(Set<UUID> var1, UUID var2, UUID var3) {
      var1.remove(var2);
      var1.remove(var3);
      this.removeFromAllQueues(var2);
      this.removeFromAllQueues(var3);
      if (this.plugin.getDuelManager() != null) {
         this.plugin
            .getDuelManager()
            .getMatchFoundEffects()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var2
            );
         this.plugin
            .getDuelManager()
            .getMatchFoundEffects()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var3
            );
      }
   }

   public void addFightingPlayer(UUID var1, String var2) {
      String var3 = var2.toLowerCase();
      this.fightingPlayers.computeIfAbsent(var3, var0 -> ConcurrentHashMap.newKeySet()).add(var1);
      this.allFightingPlayers.add(var1);
      this.playerToFightingKit.put(var1, var3);
   }

   public void removeFightingPlayer(UUID var1) {
      String var2 = this.playerToFightingKit.remove(var1);
      if (var2 != null) {
         Set var3 = this.fightingPlayers.get(var2);
         if (var3 != null) {
            var3.remove(var1);
         }
      }

      this.allFightingPlayers.remove(var1);
   }

   public boolean isFightingPlayer(UUID var1) {
      return this.allFightingPlayers.contains(var1);
   }

   public int getFightingCount(String var1) {
      Set var2 = this.fightingPlayers.get(var1.toLowerCase());
      return var2 != null ? var2.size() : 0;
   }

   public long getWaitTime(UUID var1) {
      Map var2 = this.queueJoinTimes.get(var1);
      if (var2 != null && !var2.isEmpty()) {
         long var3 = var2.values().stream().mapToLong(var0 -> var0).min().orElse(System.currentTimeMillis());
         return System.currentTimeMillis() - var3;
      } else {
         return 0L;
      }
   }

   public long getWaitTime(UUID var1, String var2) {
      String var3 = var2.toLowerCase();
      Long var4 = this.queueJoinTimes.getOrDefault(var1, Map.of()).get(var3);
      return var4 == null ? 0L : System.currentTimeMillis() - var4;
   }

   private void addMembership(UUID var1, String var2) {
      this.allQueuedPlayers.add(var1);
      this.playerToQueues.computeIfAbsent(var1, var0 -> ConcurrentHashMap.newKeySet()).add(var2);
      this.queueJoinTimes.computeIfAbsent(var1, var0 -> new ConcurrentHashMap<>()).putIfAbsent(var2, System.currentTimeMillis());
   }

   private void removeMembership(UUID var1, String var2) {
      Set var3 = this.playerToQueues.get(var1);
      if (var3 != null) {
         var3.remove(var2);
         if (var3.isEmpty()) {
            this.playerToQueues.remove(var1);
            this.allQueuedPlayers.remove(var1);
         }
      }

      Map var4 = this.queueJoinTimes.get(var1);
      if (var4 != null) {
         var4.remove(var2);
         if (var4.isEmpty()) {
            this.queueJoinTimes.remove(var1);
         }
      }
   }

   public void resyncGlobalQueueEntries() {
      if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
         org.lime.swiftCore.v.r.d var1 = this.plugin.getLimboManager().getGlobalQueueManager();
         if (var1 != null) {
            for (UUID var3 : new HashSet<>(this.allQueuedPlayers)) {
               Player var4 = Bukkit.getPlayer(var3);
               if (var4 != null && var4.isOnline()) {
                  for (String var6 : this.getPlayerQueues(var3)) {
                     var1.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var3, var4.getName(), var6, false
                     );
                  }
               }
            }
         }
      }
   }

   @FunctionalInterface
   public interface _b {
      void o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         Player var1, Player var2, String var3, Collection<String> var4, Collection<String> var5
      );
   }
}
