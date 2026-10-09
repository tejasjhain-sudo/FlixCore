package org.lime.swiftCore.ffa;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitTask;
import org.lime.swiftCore.SwiftCore;
import org.lime.swiftCore.arena.ArenaType;
import org.lime.swiftCore.arena.i;
import org.lime.swiftCore.b.h;
import org.lime.swiftCore.b.k;
import org.lime.swiftCore.kit.ColorPartyKitGuard;
import org.lime.swiftCore.kit.KitRule;
import org.lime.swiftCore.kit.t;
import org.lime.swiftCore.kit.w;
import org.lime.swiftCore.scoreboard.ScoreboardState;

public class FFAManager {
   private final SwiftCore plugin;
   private final i arenaManager;
   private final org.lime.swiftCore.kit.e kitManager;
   private final Map<String, Set<UUID>> arenaPlayers;
   private final Map<UUID, String> playerArenas;
   private final Map<UUID, String> playerKits;
   private final Set<UUID> invulnerablePlayers;
   private final Map<UUID, Integer> sessionKills;
   private final Map<UUID, Integer> sessionDeaths;
   private final Map<UUID, Long> joinTimes;
   private final Map<UUID, BukkitTask> pendingLeaveCountdowns;
   private final Map<UUID, Location> leaveStartLocations;
   private final Map<UUID, Location> joinLocations;
   private final Set<UUID> randomFFAPlayers;
   private final Map<UUID, Long> postLeaveEditorLocks;
   private int cachedLeaveCountdown;
   private String cachedLeaveActionbar;
   private String cachedLeaveCancelledMessage;
   private String cachedLeaveCompleteMessage;
   private boolean cachedKillDeathTitleEnabled;
   private String cachedKillTitle;
   private String cachedKillSubtitle;
   private k resolvedKillSound;
   private String cachedDeathTitle;
   private String cachedDeathSubtitle;
   private k resolvedDeathSound;
   private boolean cachedAutoRejoin;
   private int cachedFfaCountdown;
   private k resolvedCountdownSound;
   private k resolvedStartSound;
   private boolean cachedRandomFFAEnabled;
   private Map<String, FFAManager._b> cachedRandomFFAArenaConfigs;
   private boolean cachedRandomFFALootDrop;
   private boolean cachedRandomFFASaveInventory;
   private long cachedPostLeaveEditorLockMs;
   private e capacityPolicy;
   private List<Map<?, ?>> cachedTitles;
   private List<Map<?, ?>> cachedFinalTitle;

   public FFAManager(SwiftCore var1, i var2, org.lime.swiftCore.kit.e var3) {
      this.plugin = var1;
      this.arenaManager = var2;
      this.kitManager = var3;
      this.arenaPlayers = new ConcurrentHashMap<>();
      this.playerArenas = new ConcurrentHashMap<>();
      this.playerKits = new ConcurrentHashMap<>();
      this.invulnerablePlayers = ConcurrentHashMap.newKeySet();
      this.sessionKills = new ConcurrentHashMap<>();
      this.sessionDeaths = new ConcurrentHashMap<>();
      this.joinTimes = new ConcurrentHashMap<>();
      this.pendingLeaveCountdowns = new ConcurrentHashMap<>();
      this.leaveStartLocations = new ConcurrentHashMap<>();
      this.joinLocations = new ConcurrentHashMap<>();
      this.randomFFAPlayers = ConcurrentHashMap.newKeySet();
      this.postLeaveEditorLocks = new ConcurrentHashMap<>();
      this.cacheLeaveConfig();
   }

   private void cacheLeaveConfig() {
      this.cachedLeaveCountdown = this.plugin.getConfig().getInt("free-for-all.leave-countdown.time", 3);
      this.cachedLeaveActionbar = this.plugin.getConfig().getString("free-for-all.leave-countdown.actionbar", "&cLeaving FFA in &e%time%s&c...");
      this.cachedLeaveCancelledMessage = this.plugin.getConfig().getString("free-for-all.leave-countdown.cancelled", "&cLeave cancelled");
      this.cachedLeaveCompleteMessage = this.plugin.getConfig().getString("free-for-all.leave-countdown.complete", "&aYou have left the FFA arena");
      this.cachedKillDeathTitleEnabled = this.plugin.getConfig().getBoolean("free-for-all.kill-death-title.enabled", true);
      this.cachedKillTitle = this.plugin.getConfig().getString("free-for-all.kill-death-title.kill.title", "You Killed");
      this.cachedKillSubtitle = this.plugin.getConfig().getString("free-for-all.kill-death-title.kill.subtitle", "%player%");
      this.resolvedKillSound = this.configuredSoundOrFallback("free-for-all.kill-death-title.kill.sound", "free-for-all.sounds.win", "ENTITY_PLAYER_LEVELUP");
      this.cachedDeathTitle = this.plugin.getConfig().getString("free-for-all.kill-death-title.death.title", "You Were Killed");
      this.cachedDeathSubtitle = this.plugin.getConfig().getString("free-for-all.kill-death-title.death.subtitle", "by %player%");
      this.resolvedDeathSound = this.configuredSoundOrFallback("free-for-all.kill-death-title.death.sound", "free-for-all.sounds.lose", "ENTITY_VILLAGER_DEATH");
      this.cachedAutoRejoin = this.plugin.getConfig().getBoolean("free-for-all.rejoin-automatically", true);
      this.cachedFfaCountdown = this.plugin.getConfig().getInt("free-for-all.ffa-countdown", 4);
      this.resolvedCountdownSound = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig(), "free-for-all.sounds.countdown", "BLOCK_NOTE_BLOCK_PLING"
      );
      this.resolvedStartSound = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig(), "free-for-all.sounds.start", "BLOCK_NOTE_BLOCK_CHIME"
      );
      this.cachedRandomFFAEnabled = this.plugin.getConfig().getBoolean("free-for-all.random-ffa.enabled", false);
      LinkedHashMap var1 = new LinkedHashMap();
      ConfigurationSection var2 = this.plugin.getConfig().getConfigurationSection("free-for-all.random-ffa.arenas");
      if (var2 != null) {
         for (String var4 : var2.getKeys(false)) {
            String var5 = var2.getString(var4 + ".mode", "exclude");
            List var6 = var2.getStringList(var4 + ".kits").stream().map(String::toLowerCase).collect(Collectors.toList());
            var1.put(var4.toLowerCase(), new FFAManager._b(var5, var6));
         }
      }

      this.cachedRandomFFAArenaConfigs = var1;
      this.cachedRandomFFALootDrop = this.plugin.getConfig().getBoolean("free-for-all.random-ffa.loot-drop", false);
      this.cachedRandomFFASaveInventory = this.plugin.getConfig().getBoolean("free-for-all.random-ffa.save-inventory", false);
      this.cachedPostLeaveEditorLockMs = Math.max(0L, this.plugin.getConfig().getLong("kit-editor.post-ffa-leave-edit-lock-ms", 5000L));
      LinkedHashMap var7 = new LinkedHashMap();
      ConfigurationSection var8 = this.plugin.getConfig().getConfigurationSection("free-for-all.instances.limits");
      if (var8 != null) {
         for (String var10 : var8.getKeys(false)) {
            var7.put(var10, var8.getInt(var10));
         }
      }

      this.capacityPolicy = new e(this.plugin.getConfig().getInt("free-for-all.instances.default-limit", 100), var7);
      this.cachedTitles = this.plugin.getConfig().getMapList("free-for-all.titles");
      this.cachedFinalTitle = this.plugin.getConfig().getMapList("free-for-all.final-title");
   }

   private k configuredSoundOrFallback(String var1, String var2, String var3) {
      String var4 = this.plugin.getConfig().getString(var1, "");
      return var4 != null && !var4.isBlank()
         ? k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            this.plugin.getConfig(), var1, var3
         )
         : k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            this.plugin.getConfig(), var2, var3
         );
   }

   private void playFfaSound(Player var1, k var2, String var3) {
      if (var1 != null && var2 != null) {
         UUID var4 = var1.getUniqueId();
         Bukkit.getScheduler()
            .runTaskLater(
               this.plugin,
               () -> {
                  Player var4x = Bukkit.getPlayer(var4);
                  if (var4x != null && var4x.isOnline()) {
                     try {
                        var2.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var4x
                        );
                        if (this.plugin.isDebug()) {
                           this.plugin.getLogger().info("[FFA Sound Debug] Played " + var3 + " sound " + var2 + " for " + var4x.getName());
                        }
                     } catch (Exception var6) {
                        this.plugin
                           .getLogger()
                           .warning("[FFA Sound] Failed to play " + var3 + " sound " + var2 + " for " + var4x.getName() + ": " + var6.getMessage());
                     }
                  }
               },
               2L
            );
      }
   }

   public boolean isRandomFFAEnabled() {
      return this.cachedRandomFFAEnabled;
   }

   public boolean isRandomFFAPlayer(UUID var1) {
      return this.randomFFAPlayers.contains(var1);
   }

   public boolean isRandomFFALootDropEnabled() {
      return this.cachedRandomFFALootDrop;
   }

   public boolean isRandomFFASaveInventoryEnabled() {
      return this.cachedRandomFFASaveInventory;
   }

   public List<String> getRandomFFAArenas() {
      return new ArrayList<>(this.cachedRandomFFAArenaConfigs.keySet());
   }

   public boolean isRandomFFAArena(String var1) {
      return this.cachedRandomFFAArenaConfigs.isEmpty() ? false : this.cachedRandomFFAArenaConfigs.containsKey(var1.toLowerCase());
   }

   public String selectRandomFFAKit(String var1) {
      org.lime.swiftCore.arena.d var2 = null;
      if (var1 != null && !var1.isEmpty()) {
         var2 = this.arenaManager
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var1
            );
      }

      FFAManager._b var3 = var1 != null ? this.cachedRandomFFAArenaConfigs.get(var1.toLowerCase()) : null;
      String var4 = var3 != null
         ? var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
         : "exclude";
      List var5 = var3 != null
         ? var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
         : List.of();
      Set var6 = this.kitManager
         .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String();
      List var7;
      if (var4.equalsIgnoreCase("include")) {
         var7 = var5.stream().filter(var6::contains).collect(Collectors.toList());
      } else {
         var7 = var6.stream()
            .filter(var1x -> !var5.contains(var1x.toLowerCase()))
            .filter(var0 -> !var0.toLowerCase().startsWith("tier"))
            .filter(
               var1x -> this.plugin.getRandomKitManager() == null
                     || !this.plugin
                        .getRandomKitManager()
                        .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                           var1x
                        )
            )
            .collect(Collectors.toList());
      }

      if (var2 != null) {
         ArenaType var8 = var2.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do();
         var7 = var7.stream()
            .filter(
               var2x -> {
                  t var3x = this.kitManager
                     .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                        var2x
                     );
                  return var3x != null
                     && var3x.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                        == var8;
               }
            )
            .collect(Collectors.toList());
      }

      return var7.isEmpty() ? null : (String)var7.get(new Random().nextInt(var7.size()));
   }

   public void reloadConfig() {
      this.cacheLeaveConfig();
   }

   public boolean isPostLeaveEditorLocked(UUID var1) {
      Long var2 = this.postLeaveEditorLocks.get(var1);
      if (var2 == null) {
         return false;
      } else if (var2 <= System.currentTimeMillis()) {
         this.postLeaveEditorLocks.remove(var1, var2);
         return false;
      } else {
         return true;
      }
   }

   private void lockKitEditorAfterLeave(UUID var1) {
      if (this.cachedPostLeaveEditorLockMs > 0L) {
         long var2 = System.currentTimeMillis() + this.cachedPostLeaveEditorLockMs;
         this.postLeaveEditorLocks.put(var1, var2);
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            Long var2x = this.postLeaveEditorLocks.get(var1);
            if (var2x != null && var2x <= System.currentTimeMillis()) {
               this.postLeaveEditorLocks.remove(var1, var2x);
            }
         }, Math.max(1L, this.cachedPostLeaveEditorLockMs / 50L + 1L));
      }
   }

   public void joinFFA(Player var1, String var2) {
      this.joinFFA(var1, var2, null, null);
   }

   public void joinFFA(Player var1, String var2, Location var3) {
      this.joinFFA(var1, var2, var3, null);
   }

   public void joinFFA(Player var1, String var2, Location var3, String var4) {
      this.joinFFAInternal(var1, var2, var3, var4, false, var1.getUniqueId());
   }

   public void joinFFAFromTransfer(Player var1, String var2, Location var3, String var4) {
      this.joinFFAFromTransfer(var1, var2, var3, var4, var1.getUniqueId());
   }

   public void joinFFAFromTransfer(Player var1, String var2, Location var3, String var4, UUID var5) {
      this.joinFFAInternal(var1, var2, var3, var4, true, var5);
   }

   private void joinFFAInternal(Player var1, String var2, Location var3, String var4, boolean var5, UUID var6) {
      org.lime.swiftCore.arena.d var7 = this.arenaManager
         .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            var2
         );
      if (var7 != null
         && var7.øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interfacesuper()
         && var7.Õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000floatsuper()
         )
       {
         if (var4 != null
            || var7.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper()
            )
          {
            if (var7.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                  != null
               && var7.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                     .getWorld()
                  != null) {
               String var8 = var4 != null
                  ? var4
                  : var7.öÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000ifnew()
                     .iterator()
                     .next();
               t var9 = this.kitManager
                  .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                     var8
                  );
               if (var9 != null) {
                  boolean var10 = this.cachedRandomFFAEnabled
                     && this.isRandomFFAArena(
                        var7.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                     );
                  if (var9.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                        == var7.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
                     && (
                        var7.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
                              var8
                           )
                           || var10
                     )) {
                     if (!ColorPartyKitGuard.blockFfaUse(this.plugin, var1, var8)) {
                        if (!w.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           this.plugin, var1, var8, "FFA"
                        )) {
                           UUID var11 = var1.getUniqueId();
                           int var12 = this.capacityPolicy
                              .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                                 var7.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                              );
                           org.lime.swiftCore.v.b.b var13 = this.plugin.getLimboManager() != null ? this.plugin.getLimboManager().getFfaNetworkManager() : null;
                           boolean var14 = false;
                           if (var13 != null && this.plugin.getLimboManager().isCrossServer()) {
                              org.lime.swiftCore.v.b.b._b var15 = var13.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var7.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(),
                                 var11,
                                 var6,
                                 var12,
                                 var5
                              );
                              if (var15
                                 != org.lime.swiftCore.v.b.b._b.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
                                 )
                               {
                                 this.plugin
                                    .getMessagesManager()
                                    .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                       var1,
                                       var15
                                             == org.lime.swiftCore.v.b.b._b.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
                                          ? "ffa-reservation-expired"
                                          : "ffa-arena-full"
                                    );
                                 return;
                              }

                              var14 = true;
                           } else if (var12 > 0
                              && this.getArenaPlayerCount(
                                    var7.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                 )
                                 >= var12) {
                              this.plugin
                                 .getMessagesManager()
                                 .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                    var1, "ffa-arena-full"
                                 );
                              return;
                           }

                           if (this.plugin
                              .getSpectatorManager()
                              .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                                 var11
                              )) {
                              this.plugin
                                 .getSpectatorManager()
                                 .ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private(
                                    var1
                                 );
                           }

                           if (this.plugin.getPartyManager().isInParty(var11)) {
                              this.plugin.getPartyManager().leaveParty(var11);
                           }

                           this.cancelLeaveCountdown(var1);
                           this.plugin.getArenaListener().cancelOutboundCountdown(var11);
                           String var17 = var2.toLowerCase();
                           this.arenaPlayers.putIfAbsent(var17, ConcurrentHashMap.newKeySet());
                           this.arenaPlayers.get(var17).add(var11);
                           this.playerArenas.put(var11, var17);
                           if (this.plugin.getClanManager() != null) {
                              this.plugin
                                 .getClanManager()
                                 .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
                           }

                           this.playerKits.put(var11, var8);
                           this.plugin.getKitRulesListener().addActivePlayer(var11, var8);
                           if (var4 != null) {
                              this.randomFFAPlayers.add(var11);
                           }

                           this.sessionKills.put(var11, 0);
                           this.sessionDeaths.put(var11, 0);
                           this.joinTimes.put(var11, System.currentTimeMillis());
                           var1.getInventory().clear();
                           var1.setAllowFlight(false);
                           var1.setFlying(false);
                           var1.setInvulnerable(false);
                           var1.setCollidable(true);
                           Location var16 = var3 != null
                              ? var3
                              : var7.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
                           this.joinLocations.put(var11, var16.clone());
                           if (var16.getWorld() == null) {
                              var1.sendMessage(Component.text("Arena world is not loaded!").color(NamedTextColor.RED));
                              this.arenaPlayers.get(var17).remove(var11);
                              this.playerArenas.remove(var11);
                              this.playerKits.remove(var11);
                              this.plugin.getKitRulesListener().removeActivePlayer(var11);
                              this.sessionKills.remove(var11);
                              this.sessionDeaths.remove(var11);
                              this.joinTimes.remove(var11);
                              this.joinLocations.remove(var11);
                              if (var14 && var13 != null) {
                                 var13.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                    var7.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(),
                                    var11
                                 );
                              }
                           } else {
                              Bukkit.getScheduler()
                                 .runTask(
                                    this.plugin,
                                    () -> {
                                       this.plugin.getArenaListener().addTeleportGracePeriod(var11);
                                       if (var1.isInsideVehicle()) {
                                          var1.leaveVehicle();
                                       }

                                       if (!var1.getPassengers().isEmpty()) {
                                          for (Entity var9x : var1.getPassengers()) {
                                             var1.removePassenger(var9x);
                                          }
                                       }

                                       Location var14x = new Location(
                                          var16.getWorld(), var16.getX(), var16.getY(), var16.getZ(), var16.getYaw(), var16.getPitch()
                                       );
                                       var1.teleportAsync(var14x, TeleportCause.PLUGIN).thenAccept(var3xx -> {
                                          if (!var3xx) {
                                             this.plugin.getLogger().warning("[FFA] Teleport failed for " + var1.getName());
                                          } else {
                                             Bukkit.getScheduler().runTask(this.plugin, () -> {
                                                Player var1xxx = Bukkit.getPlayer(var11);
                                                if (var1xxx != null && var1xxx.isOnline()) {
                                                   var1xxx.setAllowFlight(false);
                                                   var1xxx.setFlying(false);
                                                   var1xxx.setInvulnerable(false);
                                                   var1xxx.setCollidable(true);
                                                   var1xxx.setFallDistance(0.0F);
                                                }
                                             });
                                          }
                                       });
                                       if (this.randomFFAPlayers.contains(var11)) {
                                          c var15x = this.plugin.getRandomFFAInventoryManager();
                                          if (var15x != null
                                             && var15x.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                                             )
                                           {
                                             var15x.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                                var1,
                                                () -> this.plugin
                                                      .getMessagesManager()
                                                      .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                                         var1, "randomffa-inventory-restored"
                                                      ),
                                                () -> this.kitManager
                                                      .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
                                                         var1, var8
                                                      )
                                             );
                                          } else {
                                             this.kitManager
                                                .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
                                                   var1, var8
                                                );
                                          }
                                       } else {
                                          this.kitManager
                                             .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
                                                var1, var8
                                             );
                                       }

                                       this.applyKitHealth(var1, var8);
                                       this.plugin.getScoreboardManager().setState(var1, ScoreboardState.FFA);
                                       this.plugin.getScoreboardManager().setPlaceholder(var1, "in_ffa", "true");
                                       this.plugin.getScoreboardManager().setPlaceholder(var1, "is_ffa", "true");
                                       this.plugin
                                          .getScoreboardManager()
                                          .setPlaceholder(
                                             var1,
                                             "kit",
                                             this.plugin
                                                .getDataManager()
                                                .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                                   var8
                                                )
                                          );
                                       this.plugin.getScoreboardManager().setPlaceholder(var1, "ffa_arena", var2);
                                       this.plugin
                                          .getScoreboardManager()
                                          .setPlaceholder(var1, "ffa_players", String.valueOf(this.arenaPlayers.get(var17).size()));
                                       this.arenaManager
                                          .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                             var1.getUniqueId(), var2
                                          );
                                       if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().getActiveMatchRegistry() != null) {
                                          this.plugin
                                             .getLimboManager()
                                             .getActiveMatchRegistry()
                                             .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                                "ffa-" + var11,
                                                List.of(var11),
                                                var7.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(),
                                                var8,
                                                "FFA"
                                             );
                                       }

                                       this.plugin
                                          .getChunkyIntegration()
                                          .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                             var7
                                          );
                                       this.updateFFAPlayerCount(var17);
                                       this.updateHpIndicator(var17, var8);
                                       Set var16x = this.arenaPlayers.get(var17);
                                       if (var16x != null) {
                                          ArrayList var10x = new ArrayList();

                                          for (UUID var12x : var16x) {
                                             if (!var12x.equals(var11)) {
                                                Player var13x = Bukkit.getPlayer(var12x);
                                                if (var13x != null && var13x.isOnline()) {
                                                   var10x.add(var13x);
                                                }
                                             }
                                          }

                                          if (!var10x.isEmpty()) {
                                             this.plugin
                                                .getPlayerSettingsManager()
                                                .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                                   var1, var10x.toArray(new Player[0])
                                                );
                                          }

                                          var10x.add(var1);
                                          this.plugin
                                             .getPlayerSettingsManager()
                                             .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                                var10x
                                             );
                                       }

                                       if (this.plugin.getTablistManager() != null) {
                                          this.plugin.getTablistManager().setupFFAContext(var1, var2);
                                          this.plugin.getTablistManager().refreshFFAContext(var17, null);
                                       }

                                       this.startCountdown(var1);
                                    }
                                 );
                           }
                        }
                     }
                  } else {
                     this.plugin
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var1, "ffa-mode-not-supported"
                        );
                  }
               }
            } else {
               var1.sendMessage(Component.text("Arena center is not set or world is not loaded!").color(NamedTextColor.RED));
            }
         }
      }
   }

   public void startLeaveCountdown(Player var1) {
      UUID var2 = var1.getUniqueId();
      if (this.pendingLeaveCountdowns.containsKey(var2)) {
         this.cancelLeaveCountdown(var1);
      } else if (this.plugin.getFFACombatManager() != null
         && this.plugin
            .getFFACombatManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var2
            )) {
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "ffa-leave-in-combat"
            );
      } else {
         int[] var3 = new int[]{this.cachedLeaveCountdown};
         Location var4 = var1.getLocation().clone();
         this.leaveStartLocations.put(var2, var4);
         BukkitTask var5 = Bukkit.getScheduler()
            .runTaskTimer(
               this.plugin,
               () -> {
                  if (var1.isOnline() && this.isInFFA(var2)) {
                     Location var4x = var1.getLocation();
                     Location var5x = this.leaveStartLocations.get(var2);
                     if (var5x == null || var4x.getX() == var5x.getX() && var4x.getY() == var5x.getY() && var4x.getZ() == var5x.getZ()) {
                        if (this.plugin.getFFACombatManager() != null
                           && this.plugin
                              .getFFACombatManager()
                              .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                                 var2
                              )) {
                           this.cancelLeaveCountdown(var1);
                        } else if (var3[0] <= 0) {
                           BukkitTask var8 = this.pendingLeaveCountdowns.remove(var2);
                           this.leaveStartLocations.remove(var2);
                           if (var8 != null) {
                              var8.cancel();
                           }

                           TextComponent var9 = LegacyComponentSerializer.legacyAmpersand().deserialize(this.cachedLeaveCompleteMessage);
                           var1.sendActionBar(var9);
                           this.leaveFFAInstant(var1, true);
                        } else {
                           String var6 = this.cachedLeaveActionbar.replace("%time%", String.valueOf(var3[0]));
                           TextComponent var7 = LegacyComponentSerializer.legacyAmpersand().deserialize(var6);
                           var1.sendActionBar(var7);
                           var3[0]--;
                        }
                     } else {
                        this.cancelLeaveCountdown(var1);
                     }
                  } else {
                     this.cancelLeaveCountdown(var1);
                  }
               },
               0L,
               20L
            );
         this.pendingLeaveCountdowns.put(var2, var5);
      }
   }

   public void cancelLeaveCountdown(Player var1) {
      UUID var2 = var1.getUniqueId();
      this.leaveStartLocations.remove(var2);
      BukkitTask var3 = this.pendingLeaveCountdowns.remove(var2);
      if (var3 != null) {
         var3.cancel();
         if (var1.isOnline()) {
            TextComponent var4 = LegacyComponentSerializer.legacyAmpersand().deserialize(this.cachedLeaveCancelledMessage);
            var1.sendActionBar(var4);
         }
      }
   }

   public boolean hasPendingLeaveCountdown(UUID var1) {
      return this.pendingLeaveCountdowns.containsKey(var1);
   }

   public void leaveFFA(Player var1) {
      this.cancelLeaveCountdown(var1);
      this.leaveFFAInstant(var1, true);
   }

   public void leaveFFANoSave(Player var1) {
      this.cancelLeaveCountdown(var1);
      this.leaveFFAInstant(var1, false);
   }

   private void leaveFFAInstant(Player var1, boolean var2) {
      UUID var3 = var1.getUniqueId();
      this.lockKitEditorAfterLeave(var3);
      String var4 = this.playerArenas.remove(var3);
      if (this.plugin.getClanManager() != null) {
         this.plugin
            .getClanManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      }

      if (var2 && this.randomFFAPlayers.contains(var3)) {
         c var5 = this.plugin.getRandomFFAInventoryManager();
         if (var5 != null
            && var5.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
            )
          {
            var5.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var1
            );
         }
      }

      this.playerKits.remove(var3);
      this.randomFFAPlayers.remove(var3);
      this.plugin.getKitRulesListener().removeActivePlayer(var3);
      if (this.plugin.getHPIndicatorManager() != null) {
         this.plugin.getHPIndicatorManager().removeFromGroup(var3);
      }

      this.invulnerablePlayers.remove(var3);
      this.sessionKills.remove(var3);
      this.sessionDeaths.remove(var3);
      this.joinTimes.remove(var3);
      this.joinLocations.remove(var3);
      if (this.plugin.getFFACombatManager() != null) {
         this.plugin
            .getFFACombatManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var3
            );
      }

      if (var4 != null) {
         Set var7 = this.arenaPlayers.get(var4);
         if (var7 != null) {
            var7.remove(var3);
            if (var7.isEmpty()) {
               this.arenaPlayers.remove(var4);
               org.lime.swiftCore.arena.d var6 = this.arenaManager
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var4
                  );
               if (var6 != null) {
                  this.plugin
                     .getArenaResetManager()
                     .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
                        var6
                     );
               }
            }
         }

         if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().getFfaNetworkManager() != null) {
            this.plugin
               .getLimboManager()
               .getFfaNetworkManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var4, var3
               );
         }
      }

      this.plugin.getArenaListener().cancelOutboundCountdown(var3);
      this.plugin.getArenaListener().blockEnderPearlTeleportAfterMatch(var3);
      this.plugin
         .getSpectatorManager()
         .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
            var3
         );
      if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().getActiveMatchRegistry() != null) {
         this.plugin
            .getLimboManager()
            .getActiveMatchRegistry()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               "ffa-" + var3, List.of(var3)
            );
      }

      var1.getInventory().clear();
      this.plugin
         .getKitManager()
         .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
            var1
         );
      if (this.plugin
         .getKitEditor()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1
         )) {
         this.plugin
            .getKitEditor()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var1
            );
         var1.closeInventory();
      }

      this.resetPlayerHealth(var1);
      this.sendPlayerToLobby(var1);
      this.plugin.getSpawnItemsManager().giveSpawnItems(var1, "default", false, false);
      this.plugin.getScoreboardManager().setState(var1, ScoreboardState.DEFAULT);
      this.plugin.getScoreboardManager().setPlaceholder(var1, "in_ffa", "false");
      this.plugin.getScoreboardManager().setPlaceholder(var1, "is_ffa", "false");
      this.plugin.getScoreboardManager().setPlaceholder(var1, "kit", "");
      this.plugin.getScoreboardManager().setPlaceholder(var1, "ffa_arena", "");
      this.plugin.getScoreboardManager().setPlaceholder(var1, "ffa_players", "");
      this.plugin.getKitRulesListener().clearCooldowns(var3);
      this.plugin
         .getPlayerSettingsManager()
         .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            var1
         );
      this.arenaManager
         .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            var3
         );
      if (var4 != null) {
         this.updateFFAPlayerCount(var4);
      }

      if (this.plugin.getTablistManager() != null) {
         if (var4 != null) {
            this.plugin.getTablistManager().refreshFFAContext(var4, var3);
         }

         this.plugin.getTablistManager().resetContext(var1);
      }
   }

   public void handleDeath(Player var1) {
      UUID var2 = var1.getUniqueId();
      String var3 = this.playerArenas.get(var2);
      if (var3 != null) {
         if (this.plugin.getFFACombatManager() != null) {
            this.plugin
               .getFFACombatManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var2
               );
         }

         org.lime.swiftCore.arena.d var4 = this.arenaManager
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var3
            );
         if (var4 != null) {
            String var5 = this.playerKits
               .getOrDefault(
                  var2,
                  var4.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper()
                     ? var4.öÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000ifnew()
                        .iterator()
                        .next()
                     : null
               );
            if (var5 != null) {
               t var6 = this.kitManager
                  .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                     var5
                  );
               if (var6 != null) {
                  Player var7 = var1.getKiller();
                  UUID var8 = var7 != null ? var7.getUniqueId() : null;
                  this.plugin
                     .getStatsManager()
                     .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                        var2, var3, var8, var1
                     );
                  this.sessionDeaths.merge(var2, 1, Integer::sum);
                  this.sessionKills.put(var2, 0);
                  if (var7 != null && this.isInFFA(var7.getUniqueId())) {
                     this.plugin
                        .getStatsManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var7.getUniqueId(), var3, var2, var7
                        );
                     this.sessionKills.merge(var7.getUniqueId(), 1, Integer::sum);
                     this.plugin
                        .getKillEffectManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var7, var1
                        );
                     if (this.plugin.getKillSoundManager() != null) {
                        this.plugin
                           .getKillSoundManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var7
                           );
                     }

                     String var9 = this.playerKits.getOrDefault(var7.getUniqueId(), var5);
                     this.healPlayer(var7, var9);
                     if (var7.getFireTicks() > 0) {
                        var7.setFireTicks(0);
                     }

                     if (!this.cachedRandomFFALootDrop || !this.randomFFAPlayers.contains(var7.getUniqueId())) {
                        this.kitManager
                           .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
                              var7, var9
                           );
                     }

                     if (this.cachedKillDeathTitleEnabled) {
                        String var10 = this.cachedKillSubtitle.replace("%player%", var1.getName());
                        TextComponent var11 = LegacyComponentSerializer.legacyAmpersand().deserialize(this.cachedKillTitle);
                        TextComponent var12 = LegacyComponentSerializer.legacyAmpersand().deserialize(var10);
                        var7.showTitle(Title.title(var11, var12, Times.times(Duration.ofMillis(100L), Duration.ofSeconds(2L), Duration.ofMillis(500L))));
                        this.playFfaSound(var7, this.resolvedKillSound, "kill");
                        String var13 = this.cachedDeathSubtitle.replace("%player%", var7.getName());
                        TextComponent var14 = LegacyComponentSerializer.legacyAmpersand().deserialize(this.cachedDeathTitle);
                        TextComponent var15 = LegacyComponentSerializer.legacyAmpersand().deserialize(var13);
                        var1.showTitle(Title.title(var14, var15, Times.times(Duration.ofMillis(100L), Duration.ofSeconds(2L), Duration.ofMillis(500L))));
                        this.playFfaSound(var1, this.resolvedDeathSound, "death");
                     }

                     if (this.plugin.getClanManager() != null) {
                        this.plugin
                           .getClanManager()
                           .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
                              var7.getUniqueId()
                           );
                     }
                  }

                  this.plugin
                     .getKitManager()
                     .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                        var1
                     );
                  boolean var16 = this.cachedAutoRejoin;
                  Bukkit.getScheduler()
                     .runTaskLater(
                        this.plugin,
                        () -> {
                           if (var1.isOnline() && this.playerArenas.containsKey(var2)) {
                              var1.spigot().respawn();
                              if (var1.getFireTicks() > 0) {
                                 var1.setFireTicks(0);
                              }

                              if (var16) {
                                 Location var7x = this.joinLocations.get(var2);
                                 Location var8x = var7x != null
                                    ? var7x
                                    : var4.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
                                 if (var8x != null && var8x.getWorld() != null) {
                                    this.plugin.getArenaListener().addTeleportGracePeriod(var2);
                                    var1.setAllowFlight(false);
                                    var1.setFlying(false);
                                    var1.setInvulnerable(false);
                                    var1.setCollidable(true);
                                    if (var1.isInsideVehicle()) {
                                       var1.leaveVehicle();
                                    }

                                    Location var9x = new Location(var8x.getWorld(), var8x.getX(), var8x.getY(), var8x.getZ(), var8x.getYaw(), var8x.getPitch());
                                    var1.teleportAsync(var9x, TeleportCause.PLUGIN);
                                 }

                                 String var11x = var5;
                                 if (this.randomFFAPlayers.contains(var2)) {
                                    String var10x = this.selectRandomFFAKit(var3);
                                    if (var10x != null) {
                                       var11x = var10x;
                                       this.playerKits.put(var2, var10x);
                                       this.plugin
                                          .getScoreboardManager()
                                          .setPlaceholder(
                                             var1,
                                             "kit",
                                             this.plugin
                                                .getDataManager()
                                                .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                                   var10x
                                                )
                                          );
                                    }
                                 }

                                 this.kitManager
                                    .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
                                       var1, var11x
                                    );
                                 this.applyKitHealth(var1, var11x);
                                 Set var12x = this.arenaPlayers.get(var3);
                                 if (var12x != null) {
                                    this.plugin.getScoreboardManager().setPlaceholder(var1, "ffa_players", String.valueOf(var12x.size()));
                                 }

                                 this.startCountdown(var1);
                              } else {
                                 this.leaveFFANoSave(var1);
                                 Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                                    Player var1xx = Bukkit.getPlayer(var2);
                                    if (var1xx != null && var1xx.isOnline()) {
                                       if (var1xx.getFireTicks() > 0) {
                                          var1xx.setFireTicks(0);
                                       }
                                    }
                                 }, 10L);
                              }
                           }
                        },
                        1L
                     );
               }
            }
         }
      }
   }

   public boolean isInFFA(UUID var1) {
      return this.playerArenas.containsKey(var1);
   }

   public String getPlayerArena(UUID var1) {
      return this.playerArenas.get(var1);
   }

   public String getPlayerKit(UUID var1) {
      return this.playerKits.get(var1);
   }

   public int getArenaPlayerCount(String var1) {
      Set var2 = this.arenaPlayers.get(var1.toLowerCase());
      return var2 != null ? var2.size() : 0;
   }

   public Set<UUID> getArenaPlayers(String var1) {
      Set var2 = this.arenaPlayers.get(var1.toLowerCase());
      return var2 != null ? new HashSet<>(var2) : new HashSet<>();
   }

   public int getTotalPlayers() {
      return this.playerArenas.size();
   }

   public int getInstanceLimit(String var1) {
      return this.capacityPolicy
         .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            var1
         );
   }

   public Map<UUID, String> getPlayerArenasSnapshot() {
      return Map.copyOf(this.playerArenas);
   }

   public Map<UUID, String> getPlayerKitsSnapshot() {
      return Map.copyOf(this.playerKits);
   }

   private void updateFFAPlayerCount(String var1) {
      Set var2 = this.arenaPlayers.get(var1);
      if (var2 != null) {
         int var3 = var2.size();

         for (UUID var5 : var2) {
            Player var6 = Bukkit.getPlayer(var5);
            if (var6 != null && var6.isOnline()) {
               this.plugin.getScoreboardManager().setPlaceholder(var6, "ffa_players", String.valueOf(var3));
            }
         }
      }
   }

   private void updateHpIndicator(String var1, String var2) {
      if (this.plugin.getHPIndicatorManager() != null) {
         boolean var3 = this.plugin
            .getDataManager()
            .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
               var2
            )
            .getOrDefault(KitRule.HP_INDICATOR, KitRule.HP_INDICATOR.getDefaultValue());
         if (var3) {
            Set var4 = this.arenaPlayers.get(var1);
            if (var4 != null && !var4.isEmpty()) {
               this.plugin.getHPIndicatorManager().stopForGroup(var4);
               this.plugin.getHPIndicatorManager().startForGroup(var4, var1);
            }
         }
      }
   }

   private void startCountdown(Player var1) {
      UUID var2 = var1.getUniqueId();
      int var3 = this.cachedFfaCountdown;
      if (var3 <= 0) {
         this.invulnerablePlayers.remove(var2);
         this.applyFfaStartEffects(var1, var2);
      } else {
         this.invulnerablePlayers.add(var2);
         List var4 = this.cachedTitles;
         byte var5 = 20;
         int var6 = var4.size() * 20;

         for (int var7 = 0; var7 < var4.size() && var7 < var3; var7++) {
            Map var8 = (Map)var4.get(var7);
            long var9 = (long)(var7 * 20);
            Bukkit.getScheduler()
               .runTaskLater(
                  this.plugin,
                  () -> {
                     if (var1.isOnline() && this.invulnerablePlayers.contains(var2)) {
                        Object var4x = var8.get("title");
                        Object var5x = var8.get("subtitle");
                        String var6x = var4x != null ? var4x.toString() : "";
                        String var7x = var5x != null ? var5x.toString() : "";
                        TextComponent var8x = LegacyComponentSerializer.legacyAmpersand().deserialize(var6x);
                        TextComponent var9x = LegacyComponentSerializer.legacyAmpersand().deserialize(var7x);
                        var1.showTitle(Title.title(var8x, var9x, Times.times(Duration.ofMillis(0L), Duration.ofSeconds(1L), Duration.ofMillis(0L))));
                        if (this.resolvedCountdownSound != null) {
                           this.resolvedCountdownSound
                              .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var1
                              );
                        }
                     }
                  },
                  var9
               );
         }

         Bukkit.getScheduler()
            .runTaskLater(
               this.plugin,
               () -> {
                  if (var1.isOnline() && this.invulnerablePlayers.contains(var2)) {
                     List var3x = this.cachedFinalTitle;
                     if (!var3x.isEmpty()) {
                        Map var4x = (Map)var3x.get(0);
                        Object var5x = var4x.get("title");
                        Object var6x = var4x.get("subtitle");
                        String var7x = var5x != null ? var5x.toString() : "";
                        String var8x = var6x != null ? var6x.toString() : "";
                        TextComponent var9x = LegacyComponentSerializer.legacyAmpersand().deserialize(var7x);
                        TextComponent var10 = LegacyComponentSerializer.legacyAmpersand().deserialize(var8x);
                        var1.showTitle(Title.title(var9x, var10, Times.times(Duration.ofMillis(0L), Duration.ofSeconds(1L), Duration.ofMillis(500L))));
                        if (this.resolvedStartSound != null) {
                           this.resolvedStartSound
                              .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var1
                              );
                        }
                     }
                  }
               },
               (long)(var4.size() * 20)
            );
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            this.invulnerablePlayers.remove(var2);
            this.applyFfaStartEffects(var1, var2);
         }, (long)var6);
      }
   }

   private void applyFfaStartEffects(Player var1, UUID var2) {
      if (this.plugin.getPrePotionsManager() != null && var1.isOnline()) {
         String var3 = this.playerKits.get(var2);
         if (var3 != null) {
            this.plugin
               .getPrePotionsManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var3, var1
               );
         }
      }
   }

   public boolean isInvulnerable(UUID var1) {
      return this.invulnerablePlayers.contains(var1);
   }

   public void removeInvulnerability(UUID var1) {
      this.invulnerablePlayers.remove(var1);
   }

   public int getPlayerSessionKills(UUID var1) {
      return this.sessionKills.getOrDefault(var1, 0);
   }

   public int getPlayerSessionDeaths(UUID var1) {
      return this.sessionDeaths.getOrDefault(var1, 0);
   }

   public String getFFADuration(UUID var1) {
      Long var2 = this.joinTimes.get(var1);
      if (var2 == null) {
         return "0:00";
      } else {
         long var3 = System.currentTimeMillis() - var2;
         long var5 = var3 / 1000L;
         long var7 = var5 / 60L;
         var5 %= 60L;
         return var7 + ":" + (var5 < 10L ? "0" : "") + var5;
      }
   }

   public int getPlayersInKitFFA(String var1) {
      int var2 = 0;
      String var3 = var1.toLowerCase();

      for (Entry var5 : this.arenaPlayers.entrySet()) {
         org.lime.swiftCore.arena.d var6 = this.arenaManager
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               (String)var5.getKey()
            );
         if (var6 != null
            && var6.øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interfacesuper()
            && var6.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
               var3
            )) {
            var2 += ((Set)var5.getValue()).size();
         }
      }

      return var2;
   }

   private void applyKitHealth(Player var1, String var2) {
      boolean var3 = this.plugin
         .getDataManager()
         .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
            var2
         )
         .getOrDefault(KitRule.HEARTS, KitRule.HEARTS.getDefaultValue());
      AttributeInstance var4 = org.lime.swiftCore.b.b.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
         var1
      );
      if (var4 == null) {
         var1.setHealth(20.0);
         h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1
         );
      } else {
         if (var3) {
            double var5 = this.plugin.getConfig().getDouble("kit-rules.hearts." + var2, 10.0);
            if (var5 < 1.0) {
               var5 = 10.0;
            }

            double var7 = var5 * 2.0;
            var4.setBaseValue(var7);
            var1.setHealth(Math.min(var7, var4.getValue()));
            var1.setHealthScaled(true);
            var1.setHealthScale(var7);
         } else {
            var4.setBaseValue(20.0);
            var1.setHealth(Math.min(20.0, var4.getValue()));
            var1.setHealthScaled(false);
         }

         h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1
         );
      }
   }

   private void resetPlayerHealth(Player var1) {
      AttributeInstance var2 = org.lime.swiftCore.b.b.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
         var1
      );
      if (var2 != null) {
         var2.setBaseValue(20.0);
         var1.setHealth(Math.min(20.0, var2.getValue()));
      } else {
         var1.setHealth(20.0);
      }

      var1.setHealthScaled(false);
      h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         var1
      );
   }

   private void healPlayer(Player var1, String var2) {
      org.lime.swiftCore.b.b.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         var1
      );
      h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         var1
      );
   }

   private void sendPlayerToLobby(Player var1) {
      if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
         org.lime.swiftCore.v.t.c var2 = this.plugin
            .getLimboManager()
            .getConfigCache()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
         if (var2 != null
            && var2.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
               == org.lime.swiftCore.v.p.e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
            )
          {
            org.lime.swiftCore.v.j.b var3 = this.plugin.getLimboManager().getPlayerDataSync();
            if (var3 != null) {
               var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1.getUniqueId()
               );
               var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var1.getName()
               );
            }

            this.plugin
               .getLimboManager()
               .getTransferManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1
               );
            return;
         }
      }

      this.plugin
         .getLobbyManager()
         .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            var1
         );
   }

   public static record _b(
      String Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new,
      List<String> o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
   ) {
   }
}
