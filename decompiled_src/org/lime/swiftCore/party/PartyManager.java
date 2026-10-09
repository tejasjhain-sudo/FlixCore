package org.lime.swiftCore.party;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lime.swiftCore.SwiftCore;

public class PartyManager {
   private final SwiftCore plugin;
   private final Map<UUID, Party> parties;
   private final Map<UUID, UUID> playerToParty;
   private final Map<UUID, b> pendingChallenges;
   private final Set<UUID> partyChatEnabled;
   private final Set<UUID> preserveOnJoin = ConcurrentHashMap.newKeySet();
   private final Set<UUID> joinerInMatchSet = ConcurrentHashMap.newKeySet();
   private final Map<UUID, String> partyGlobalIds = new ConcurrentHashMap<>();
   private String cachedBroadcastPermission;
   private int cachedDefaultMaxSize;
   private List<PartyManager._b> cachedMaxSizeTiers;

   public PartyManager(SwiftCore var1) {
      this.plugin = var1;
      this.parties = new ConcurrentHashMap<>();
      this.playerToParty = new ConcurrentHashMap<>();
      this.pendingChallenges = new ConcurrentHashMap<>();
      this.partyChatEnabled = ConcurrentHashMap.newKeySet();
      this.loadCachedConfig();
   }

   public void loadCachedConfig() {
      this.cachedBroadcastPermission = this.plugin.getConfig().getString("party.broadcast-permission", "");
      this.cachedDefaultMaxSize = this.plugin.getConfig().getInt("party.default-max-size", 8);
      ArrayList var1 = new ArrayList();

      for (Map var4 : this.plugin.getConfig().getMapList("party.max-size-permissions")) {
         Object var5 = var4.get("permission");
         Object var6 = var4.get("max-size");
         if (var5 != null && var6 != null) {
            var1.add(new PartyManager._b(var5.toString(), Integer.parseInt(var6.toString())));
         }
      }

      var1.sort(
         (var0, var1x) -> Integer.compare(
               var1x.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(),
               var0.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
            )
      );
      this.cachedMaxSizeTiers = var1;
   }

   public boolean hasBroadcastPermission(Player var1) {
      return this.cachedBroadcastPermission != null && !this.cachedBroadcastPermission.isEmpty() ? var1.hasPermission(this.cachedBroadcastPermission) : true;
   }

   public String getBroadcastPermission() {
      return this.cachedBroadcastPermission;
   }

   public int getMaxSizeForPlayer(Player var1) {
      for (PartyManager._b var3 : this.cachedMaxSizeTiers) {
         if (var1.hasPermission(
            var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
         )) {
            return var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
         }
      }

      return this.cachedDefaultMaxSize;
   }

   public Party createParty(UUID var1) {
      if (this.playerToParty.containsKey(var1)) {
         return null;
      } else {
         Player var2 = Bukkit.getPlayer(var1);
         int var3 = var2 != null ? this.getMaxSizeForPlayer(var2) : this.cachedDefaultMaxSize;
         Party var4 = new Party(var1, var3);
         this.parties.put(var1, var4);
         this.playerToParty.put(var1, var1);
         this.syncPartyToGlobal(var4);
         this.refreshPartyTabViews(Collections.singleton(var1));
         return var4;
      }
   }

   public void disbandParty(UUID var1) {
      Party var2 = this.parties.get(var1);
      if (!this.isTeamQueueLocked(var2)) {
         Party var3 = this.parties.remove(var1);
         if (var3 != null) {
            Set var4 = var3.getMembers();
            String var5 = this.partyGlobalIds.remove(var1);

            for (UUID var7 : var4) {
               this.playerToParty.remove(var7);
               this.partyChatEnabled.remove(var7);
            }

            this.pendingChallenges.remove(var1);
            if (var5 != null && this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
               this.plugin
                  .getLimboManager()
                  .getGlobalPartyManager()
                  .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
                     var5
                  );
            }

            this.refreshPartyTabViews(var4);
         }
      }
   }

   public Party getParty(UUID var1) {
      return this.parties.get(var1);
   }

   public Party getPlayerParty(UUID var1) {
      UUID var2 = this.playerToParty.get(var1);
      return var2 != null ? this.parties.get(var2) : null;
   }

   public boolean isInParty(UUID var1) {
      return this.playerToParty.containsKey(var1);
   }

   public boolean isPartyOwner(UUID var1) {
      Party var2 = this.getPlayerParty(var1);
      return var2 != null && var2.isOwner(var1);
   }

   public boolean consumeJoinerInMatch(UUID var1) {
      return this.joinerInMatchSet.remove(var1);
   }

   public boolean invitePlayer(UUID var1, UUID var2) {
      Party var3 = this.parties.get(var1);
      if (var3 == null) {
         return false;
      } else if (this.isTeamQueueLocked(var3)) {
         return false;
      } else if (var3.isFull()) {
         return false;
      } else if (this.playerToParty.containsKey(var2)) {
         return false;
      } else {
         var3.invite(var2);
         return true;
      }
   }

   public boolean joinParty(UUID var1, UUID var2) {
      Party var3 = this.parties.get(var2);
      if (var3 == null) {
         return false;
      } else if (this.isTeamQueueLocked(var3)) {
         return false;
      } else if (var3.isFull()) {
         return false;
      } else if (this.playerToParty.containsKey(var1)) {
         return false;
      } else {
         if (!var3.isPublic()) {
            int var4 = this.plugin.getConfig().getInt("party.invite-expire-time", 60);
            long var5 = (long)var4 * 1000L;
            if (!var3.hasInvite(var1, var5)) {
               return false;
            }
         }

         var3.addMember(var1);
         this.playerToParty.put(var1, var2);
         this.syncPartyToGlobal(var3);
         this.refreshPartyTabViews(var3.getMembers());
         if (this.plugin.getPartyGameManager().notifyNewJoinerIfInMatch(var1, var2)) {
            this.joinerInMatchSet.add(var1);
         }

         return true;
      }
   }

   public void registerMember(UUID var1, UUID var2) {
      this.playerToParty.put(var2, var1);
   }

   public boolean leaveParty(UUID var1) {
      Party var2 = this.getPlayerParty(var1);
      if (var2 == null) {
         return false;
      } else if (this.isTeamQueueLocked(var2)) {
         return false;
      } else {
         UUID var3 = this.playerToParty.get(var1);
         if (var2.isOwner(var1)) {
            if (var2.getSize() == 1) {
               this.disbandParty(var3);
               return true;
            }

            UUID var4 = var2.getMembers().stream().filter(var1x -> !var1x.equals(var1)).findFirst().orElse(null);
            if (var4 != null) {
               this.transferOwnership(var1, var4);
            }
         }

         Party var6 = this.getPlayerParty(var1);
         if (var6 != null) {
            var6.removeMember(var1);
            this.syncPartyToGlobal(var6);
            Set var5 = var6.getMembers();
            var5.add(var1);
            this.playerToParty.remove(var1);
            this.partyChatEnabled.remove(var1);
            this.refreshPartyTabViews(var5);
         } else {
            var2.removeMember(var1);
            this.syncPartyToGlobal(var2);
            Set var7 = var2.getMembers();
            var7.add(var1);
            this.playerToParty.remove(var1);
            this.partyChatEnabled.remove(var1);
            this.refreshPartyTabViews(var7);
         }

         return true;
      }
   }

   public boolean kickMember(UUID var1, UUID var2) {
      Party var3 = this.parties.get(var1);
      if (var3 == null) {
         return false;
      } else if (this.isTeamQueueLocked(var3)) {
         return false;
      } else if (!var3.isMember(var2)) {
         return false;
      } else if (var3.isOwner(var2)) {
         return false;
      } else {
         var3.removeMember(var2);
         this.playerToParty.remove(var2);
         this.partyChatEnabled.remove(var2);
         this.syncPartyToGlobal(var3);
         Set var4 = var3.getMembers();
         var4.add(var2);
         this.refreshPartyTabViews(var4);
         return true;
      }
   }

   public boolean transferOwnership(UUID var1, UUID var2) {
      Party var3 = this.parties.get(var1);
      if (this.isTeamQueueLocked(var3)) {
         return false;
      } else {
         Party var4 = this.parties.remove(var1);
         if (var4 == null) {
            return false;
         } else if (!var4.isMember(var2)) {
            return false;
         } else {
            String var5 = this.partyGlobalIds.remove(var1);
            Party var6 = new Party(var2, var4.getMaxSize());
            var6.setPublic(var4.isPublic());
            var6.setBroadcasting(var4.isBroadcasting());
            var6.setMode(var4.getMode());
            var6.setAllowImbalance(var4.isAllowImbalance());

            for (UUID var8 : var4.getMembers()) {
               if (!var8.equals(var2)) {
                  var6.addMember(var8);
               }

               this.playerToParty.put(var8, var2);
            }

            this.parties.put(var2, var6);
            if (var5 != null) {
               this.partyGlobalIds.put(var2, var5);
            }

            this.syncPartyToGlobal(var6);
            this.refreshPartyScoreboards(var6);
            this.refreshPartyTabViews(var6.getMembers());
            this.refreshOwnershipViews(var6, var1, var2);
            return true;
         }
      }
   }

   private void refreshOwnershipViews(Party var1, UUID var2, UUID var3) {
      if (var1 != null) {
         Player var4 = Bukkit.getPlayer(var2);
         if (var4 != null && var4.isOnline() && !this.plugin.getPartyGameManager().isPlayerInPartyGame(var2)) {
            this.plugin.getSpawnItemsManager().giveSpawnItems(var4, "party", true, false);
         }

         Player var5 = Bukkit.getPlayer(var3);
         if (var5 != null && var5.isOnline() && !this.plugin.getPartyGameManager().isPlayerInPartyGame(var3)) {
            this.plugin.getSpawnItemsManager().giveSpawnItems(var5, "party", true, true);
         }

         if (this.plugin.getPartyInfoGUI() != null) {
            this.plugin
               .getPartyInfoGUI()
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var1
               );
         }
      }
   }

   public boolean transferOwnershipToMember(UUID var1) {
      Party var2 = this.getPlayerParty(var1);
      if (var2 == null) {
         return false;
      } else {
         UUID var3 = var2.getOwner();
         return var3.equals(var1) ? true : this.transferOwnership(var3, var1);
      }
   }

   public void setPartyMode(UUID var1, PartyMode var2) {
      Party var3 = this.parties.get(var1);
      if (var3 != null) {
         var3.setMode(var2);
         this.syncPartyToGlobal(var3);
      }
   }

   private void refreshPartyTabViews(Collection<UUID> var1) {
      if (this.plugin.getTablistManager() != null && var1 != null && !var1.isEmpty()) {
         for (UUID var3 : var1) {
            Player var4 = Bukkit.getPlayer(var3);
            if (var4 != null && var4.isOnline()) {
               this.plugin.getTablistManager().invalidateLobbySocialView(var3);
               this.plugin.getTablistManager().refreshLobbySocialView(var4);
            }
         }
      }
   }

   private void refreshPartyScoreboards(Party var1) {
      if (var1 != null && this.plugin.getScoreboardManager() != null) {
         Player var2 = Bukkit.getPlayer(var1.getOwner());
         String var3 = var2 != null ? var2.getName() : "Unknown";
         HashMap var4 = new HashMap();
         var4.put("party_owner", var3);
         var4.put("party_size", String.valueOf(var1.getSize()));
         var4.put("party_max_size", String.valueOf(var1.getMaxSize()));
         var4.put("party_mode", var1.getMode().toString());
         Runnable var5 = () -> {
            for (UUID var4x : var1.getMembers()) {
               Player var5x = Bukkit.getPlayer(var4x);
               if (var5x != null && var5x.isOnline()) {
                  this.plugin.getScoreboardManager().setPlaceholders(var5x, var4);
                  this.plugin.getScoreboardManager().updateScoreboard(var5x, true);
               }
            }
         };
         if (Bukkit.isPrimaryThread()) {
            var5.run();
         } else {
            Bukkit.getScheduler().runTask(this.plugin, var5);
         }
      }
   }

   public void sendPartyChallenge(UUID var1, UUID var2, String var3) {
      this.sendPartyChallenge(var1, var2, var3, 1);
   }

   public void sendPartyChallenge(UUID var1, UUID var2, String var3, int var4) {
      Player var5 = Bukkit.getPlayer(var1);
      Player var6 = Bukkit.getPlayer(var2);
      boolean var7 = var3 != null && var3.startsWith("customkit");
      boolean var8 = var7
         || var5 == null
         || this.plugin
            .getKitManager()
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
               var5, var3
            );
      boolean var9 = var7
         || var6 == null
         || this.plugin
            .getKitManager()
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
               var6, var3
            );
      if (var8 && var9) {
         b var10 = new b(var1, var2, var3, var4);
         this.pendingChallenges.put(var2, var10);
      }
   }

   public b getPendingChallenge(UUID var1) {
      b var2 = this.pendingChallenges.get(var1);
      if (var2 != null) {
         int var3 = this.plugin.getConfig().getInt("party.challenge-expire-time", 120);
         long var4 = (long)var3 * 1000L;
         if (var2.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var4
         )) {
            this.pendingChallenges.remove(var1);
            return null;
         }
      }

      return var2;
   }

   public void removePendingChallenge(UUID var1) {
      this.pendingChallenges.remove(var1);
   }

   public void togglePartyChat(UUID var1) {
      if (this.partyChatEnabled.contains(var1)) {
         this.partyChatEnabled.remove(var1);
      } else {
         this.partyChatEnabled.add(var1);
      }
   }

   public boolean isPartyChatEnabled(UUID var1) {
      return this.partyChatEnabled.contains(var1);
   }

   public void sendPartyMessage(UUID var1, String var2) {
      Party var3 = this.getPlayerParty(var1);
      if (var3 != null) {
         Player var4 = Bukkit.getPlayer(var1);
         if (var4 != null) {
            String var5 = "&d&lPARTY &r&7" + var4.getName() + "&8: &f" + var2;

            for (UUID var7 : var3.getMembers()) {
               Player var8 = Bukkit.getPlayer(var7);
               if (var8 != null && var8.isOnline()) {
                  var8.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(var5));
               }
            }

            String var9 = this.partyGlobalIds.get(var3.getOwner());
            if (var9 != null && this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
               this.plugin
                  .getLimboManager()
                  .getPartyBroadcast()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var9, var5
                  );
            }
         }
      }
   }

   public Collection<Party> getAllParties() {
      return new ArrayList<>(this.parties.values());
   }

   public void handlePlayerQuit(UUID var1) {
      if (this.plugin.getTeamQueueManager() != null) {
         this.plugin
            .getTeamQueueManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var1
            );
      }

      if (this.isInParty(var1)) {
         Party var2 = this.getPlayerParty(var1);
         boolean var3 = var2 != null && var2.isOwner(var1);
         this.leaveParty(var1);
         if (var3 && var2 != null) {
            for (UUID var5 : var2.getMembers()) {
               if (!var5.equals(var1)) {
                  Party var6 = this.getPlayerParty(var5);
                  if (var6 != null && var6.isOwner(var5)) {
                     Player var7 = Bukkit.getPlayer(var5);
                     if (var7 != null && !this.plugin.getPartyGameManager().isPlayerInPartyGame(var5)) {
                        this.plugin.getSpawnItemsManager().giveSpawnItems(var7, "party", true, true);
                     }
                     break;
                  }
               }
            }
         }
      }

      this.partyChatEnabled.remove(var1);
   }

   private boolean isTeamQueueLocked(Party var1) {
      return var1 != null
         && this.plugin.getTeamQueueManager() != null
         && this.plugin
            .getTeamQueueManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var1
            );
   }

   public void markPreserveOnJoin(UUID var1) {
      this.preserveOnJoin.add(var1);
   }

   public void handlePlayerJoin(UUID var1) {
      if (!this.preserveOnJoin.remove(var1)) {
         this.playerToParty.remove(var1);
         this.partyChatEnabled.remove(var1);

         for (Party var3 : this.parties.values()) {
            var3.removeMember(var1);
            var3.removeInvite(var1);
         }
      }
   }

   public String getPartyName(UUID var1) {
      Party var2 = this.getPlayerParty(var1);
      if (var2 == null) {
         return null;
      } else {
         Player var3 = Bukkit.getPlayer(var2.getOwner());
         return var3 != null ? var3.getName() + "'s Party" : "Unknown Party";
      }
   }

   public String getGlobalPartyId(UUID var1) {
      return this.partyGlobalIds.get(var1);
   }

   private void syncPartyToGlobal(Party var1) {
      if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
         org.lime.swiftCore.v.k.d var2 = this.plugin.getLimboManager().getGlobalPartyManager();
         if (var2 != null) {
            String var3 = this.partyGlobalIds.get(var1.getOwner());
            if (var3 == null) {
               org.lime.swiftCore.v.k.c var4 = var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var1.getOwner()
               );
               var3 = var4.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void();
               this.partyGlobalIds.put(var1.getOwner(), var3);
               var4.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
                  .clear();
               var4.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
                  .addAll(var1.getMembers());
               var4.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1.isPublic()
               );
               var2.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var4
               );
            } else {
               org.lime.swiftCore.v.k.c var6 = var2.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                  var3
               );
               if (var6 != null) {
                  var6.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
                     .clear();
                  var6.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
                     .addAll(var1.getMembers());
                  var6.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1.getOwner()
                  );
                  var6.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1.isPublic()
                  );
                  var2.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var6
                  );
               }
            }
         }
      }
   }

   public static record _b(
      String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super,
      int Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
   ) {
      public String Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new() {
         return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
      }

      public int o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super() {
         return this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
      }
   }
}
