package org.lime.swiftCore.duel;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.lime.swiftCore.SwiftCore;
import org.lime.swiftCore.api.CustomKitAPI;
import org.lime.swiftCore.arena.ArenaType;
import org.lime.swiftCore.arena.i;
import org.lime.swiftCore.b.j;
import org.lime.swiftCore.b.k;
import org.lime.swiftCore.kit.ColorPartyKitGuard;
import org.lime.swiftCore.kit.KitRule;
import org.lime.swiftCore.kit.m;
import org.lime.swiftCore.kit.t;
import org.lime.swiftCore.ranked.RankedManager;
import org.lime.swiftCore.scoreboard.ScoreboardState;
import org.lime.swiftCore.tablist.TablistContext;

public class DuelManager {
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
   private static final Times COUNTDOWN_TIMES = Times.times(Duration.ofMillis(0L), Duration.ofSeconds(1L), Duration.ofMillis(0L));
   private static final Times FINAL_TITLE_TIMES = Times.times(Duration.ofMillis(0L), Duration.ofSeconds(1L), Duration.ofMillis(500L));
   private final SwiftCore plugin;
   private final i arenaManager;
   private final org.lime.swiftCore.ab.c statsManager;
   private final Map<UUID, List<e>> pendingRequests;
   private final Map<UUID, g> activeMatches;
   private final Map<UUID, DuelManager._b> rematchData;
   private final Set<UUID> acceptingPlayers;
   private final Set<UUID> pendingArenaPlayers = ConcurrentHashMap.newKeySet();
   private final c duelScoreActionBarManager;
   private k cachedCountdownSound;
   private k cachedStartSound;
   private k cachedWinSound;
   private k cachedLoseSound;
   private k cachedLeaveSound;
   private k cachedInactivityRevealSound;
   private String cachedBlueIcon;
   private String cachedBlueColor;
   private String cachedRedIcon;
   private String cachedRedColor;
   private int cachedMaxRounds;
   private int cachedUnrankedQueueDefaultRounds;
   private int cachedRankedQueueDefaultRounds;
   private Map<String, Integer> cachedUnrankedQueueRoundOverrides = Map.of();
   private Map<String, Integer> cachedRankedQueueRoundOverrides = Map.of();
   private int cachedCountdownTime;
   private int cachedNextRoundDelayTicks;
   private int cachedSpectatorTime;
   private int cachedMaxFightDuration;
   private boolean cachedKitColoredArmor;
   private boolean cachedRoundRewindEnabled;
   private int cachedRoundRewindDurationTicks;
   private double cachedRoundRewindArcHeight;
   private boolean cachedRoundRewindParticles;
   private Set<String> cachedRoundRewindDisabledKits;
   private boolean cachedRoundScoreTitleEnabled;
   private boolean cachedRoundScoreTitleShowFinal;
   private String cachedRoundScoreTitle;
   private String cachedRoundScoreSubtitle;
   private int cachedRoundScoreTitleDelayTicks;
   private Times cachedRoundScoreTitleTimes;
   private boolean cachedLeaveDelayEnabled;
   private int cachedLeaveDelayTicks;
   private String cachedLeaveTitle;
   private String cachedLeaveSubtitle;
   private Times cachedLeaveTitleTimes;
   private boolean cachedInactivityRevealEnabled;
   private long cachedInactivityRevealMs;
   private String cachedInactivityRevealMessage;
   private List<Map<?, ?>> cachedTitles;
   private List<Map<?, ?>> cachedFinalTitle;
   private long cachedRequestExpiryMs;
   private boolean cachedFirstTo;
   private List<String> cachedAutoGGMessages;
   private ItemStack cachedRematchItem;
   private int cachedRematchSlot;
   private int cachedRematchExpireTicks;
   private String cachedRematchMaterialName;
   private boolean cachedRematchEnabled;
   private final Map<UUID, BukkitTask> rematchExpireTasks = new ConcurrentHashMap<>();
   private final f matchFoundEffects;
   private BukkitTask requestCleanupTask;
   private BukkitTask inactivityRevealTask;
   private final Map<g, Long> lastEngagementTimes = new ConcurrentHashMap<>();
   private final Set<g> revealedInactiveMatches = ConcurrentHashMap.newKeySet();
   private boolean invSeeEnabled;
   private String invSeeClickText;
   private String invSeeHoverText;
   private String invSeeGuiTitle;
   private final Map<UUID, ItemStack[]> endMatchInventories = new ConcurrentHashMap<>();
   private final Map<UUID, ItemStack[]> endMatchArmor = new ConcurrentHashMap<>();

   public DuelManager(SwiftCore var1, i var2, org.lime.swiftCore.ab.c var3) {
      this.plugin = var1;
      this.arenaManager = var2;
      this.statsManager = var3;
      this.pendingRequests = new ConcurrentHashMap<>();
      this.activeMatches = new ConcurrentHashMap<>();
      this.rematchData = new ConcurrentHashMap<>();
      this.acceptingPlayers = ConcurrentHashMap.newKeySet();
      this.matchFoundEffects = new f(var1);
      this.duelScoreActionBarManager = new c(var1);
      this.cacheConfig();
      this.startRequestCleanupTask();
      this.startInactivityRevealTask();
   }

   public void cacheConfig() {
      this.cachedBlueIcon = this.plugin.getConfig().getString("team-settings.blue-team-icon", "§9[B] §9");
      this.cachedBlueColor = this.plugin.getConfig().getString("team-settings.blue-team-color", "§9");
      this.cachedRedIcon = this.plugin.getConfig().getString("team-settings.red-team-icon", "§c[R] §c");
      this.cachedRedColor = this.plugin.getConfig().getString("team-settings.red-team-color", "§c");
      this.cachedMaxRounds = this.plugin.getConfig().getInt("match.duel-max-rounds", 20);
      this.cacheQueueDefaultRounds();
      this.cachedCountdownTime = this.plugin.getConfig().getInt("match.countdown-time", 4);
      this.cachedNextRoundDelayTicks = Math.max(0, this.plugin.getConfig().getInt("match.next-round-delay-seconds", 3)) * 20;
      this.cachedSpectatorTime = this.plugin.getConfig().getInt("match.spectator-time", 5);
      this.cachedMaxFightDuration = this.plugin.getConfig().getInt("match.max-fight-duration", 30);
      this.cachedKitColoredArmor = this.plugin.getConfig().getBoolean("duel.kit-colored-armor", false);
      this.cachedRoundRewindEnabled = this.plugin.getConfig().getBoolean("match.round-rewind.enabled", true);
      this.cachedRoundRewindDurationTicks = Math.max(20, this.plugin.getConfig().getInt("match.round-rewind.duration-ticks", 60));
      this.cachedRoundRewindArcHeight = Math.max(0.0, this.plugin.getConfig().getDouble("match.round-rewind.arc-height", 4.0));
      this.cachedRoundRewindParticles = this.plugin.getConfig().getBoolean("match.round-rewind.particles", true);
      this.cachedRoundRewindDisabledKits = org.lime.swiftCore.kit.h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig().getStringList("match.round-rewind.disabled-kits")
      );
      this.cachedRoundScoreTitleEnabled = this.plugin.getConfig().getBoolean("match.round-score-title.enabled", true);
      this.cachedRoundScoreTitleShowFinal = this.plugin.getConfig().getBoolean("match.round-score-title.show-final-round", false);
      this.cachedRoundScoreTitle = this.plugin.getConfig().getString("match.round-score-title.title", "&eRound Score: &f%player1_wins% &7- &f%player2_wins%");
      this.cachedRoundScoreSubtitle = this.plugin.getConfig().getString("match.round-score-title.subtitle", "&f%player1% &8vs &f%player2%");
      this.cachedRoundScoreTitleDelayTicks = Math.max(0, this.plugin.getConfig().getInt("match.round-score-title.delay-ticks", 20));
      this.cachedRoundScoreTitleTimes = Times.times(
         Duration.ofMillis((long)Math.max(0, this.plugin.getConfig().getInt("match.round-score-title.fade-in-ticks", 0)) * 50L),
         Duration.ofMillis((long)Math.max(0, this.plugin.getConfig().getInt("match.round-score-title.stay-ticks", 30)) * 50L),
         Duration.ofMillis((long)Math.max(0, this.plugin.getConfig().getInt("match.round-score-title.fade-out-ticks", 10)) * 50L)
      );
      this.cachedLeaveDelayEnabled = this.plugin.getConfig().getBoolean("match.leave-delay.enabled", true);
      this.cachedLeaveDelayTicks = Math.max(0, this.plugin.getConfig().getInt("match.leave-delay.seconds", 3)) * 20;
      this.cachedLeaveTitle = this.plugin.getConfig().getString("match.leave-delay.title", "&c&lDEFEAT");
      this.cachedLeaveSubtitle = this.plugin.getConfig().getString("match.leave-delay.subtitle", "&7Teleporting to lobby...");
      this.cachedLeaveTitleTimes = Times.times(
         Duration.ofMillis((long)Math.max(0, this.plugin.getConfig().getInt("match.leave-delay.fade-in-ticks", 5)) * 50L),
         Duration.ofMillis((long)Math.max(0, this.plugin.getConfig().getInt("match.leave-delay.stay-ticks", 40)) * 50L),
         Duration.ofMillis((long)Math.max(0, this.plugin.getConfig().getInt("match.leave-delay.fade-out-ticks", 10)) * 50L)
      );
      this.cachedInactivityRevealEnabled = this.plugin.getConfig().getBoolean("match.inactivity-reveal.enabled", true);
      this.cachedInactivityRevealMs = (long)Math.max(1, this.plugin.getConfig().getInt("match.inactivity-reveal.seconds", 60)) * 1000L;
      this.cachedInactivityRevealMessage = this.plugin.getConfig().getString("match.inactivity-reveal.message", "&e%opponent% &7is at &f%x%&7, &f%y%&7, &f%z%");
      this.cachedTitles = this.plugin.getConfig().getMapList("match.titles");
      this.cachedFinalTitle = this.plugin.getConfig().getMapList("match.final-title");
      this.cachedRequestExpiryMs = (long)this.plugin.getConfig().getInt("match.duel-request-expiry", 60) * 1000L;
      this.cachedFirstTo = this.plugin.getConfig().getBoolean("match.first-to", false);
      this.invSeeEnabled = this.plugin.getConfig().getBoolean("match.inv-see.enabled", true);
      this.invSeeClickText = this.plugin.getConfig().getString("match.inv-see.click-text", "&#FFD54F⏵ [ &nᴠɪᴇᴡ ɪɴᴠ&#FFD54F ] ⏴");
      this.invSeeHoverText = this.plugin.getConfig().getString("match.inv-see.hover-text", "&#FFD54F\ud83d\udd0d &#FFFFF0Click to see %opponent%'s inventory");
      this.invSeeGuiTitle = this.plugin.getConfig().getString("match.inv-see.gui-title", "&#FFB800%opponent%'s Inventory");
      this.cacheAutoGGMessages();
      this.cacheSounds();
      this.cacheRematchItem();
   }

   private void cacheQueueDefaultRounds() {
      this.cachedUnrankedQueueDefaultRounds = this.clampRounds(this.plugin.getConfig().getInt("match.queue-default-rounds.unranked.default", 1));
      this.cachedRankedQueueDefaultRounds = this.clampRounds(
         this.plugin.getConfig().getInt("match.queue-default-rounds.ranked.default", this.plugin.getConfig().getInt("ranked.rounds", 1))
      );
      this.cachedUnrankedQueueRoundOverrides = this.loadQueueRoundOverrides("match.queue-default-rounds.unranked.kits");
      this.cachedRankedQueueRoundOverrides = this.loadQueueRoundOverrides("match.queue-default-rounds.ranked.kits");
   }

   private Map<String, Integer> loadQueueRoundOverrides(String var1) {
      ConfigurationSection var2 = this.plugin.getConfig().getConfigurationSection(var1);
      if (var2 == null) {
         return Map.of();
      } else {
         HashMap var3 = new HashMap();

         for (String var5 : var2.getKeys(false)) {
            var3.put(var5.toLowerCase(Locale.ROOT), this.clampRounds(var2.getInt(var5, 1)));
         }

         return Map.copyOf(var3);
      }
   }

   public int getQueueDefaultRounds(String var1, boolean var2) {
      String var3 = var1 == null ? "" : var1.toLowerCase(Locale.ROOT);
      String var4 = var3.startsWith("tier") ? var3.substring(4) : var3;
      Map var5 = var2 ? this.cachedRankedQueueRoundOverrides : this.cachedUnrankedQueueRoundOverrides;
      Integer var6 = (Integer)var5.get(var3);
      if (var6 == null) {
         var6 = (Integer)var5.get(var4);
      }

      if (var6 == null && var2) {
         var6 = (Integer)var5.get("tier" + var4);
      }

      return var6 != null ? this.clampRounds(var6) : (var2 ? this.cachedRankedQueueDefaultRounds : this.cachedUnrankedQueueDefaultRounds);
   }

   private int clampRounds(int var1) {
      return Math.max(1, Math.min(var1, Math.max(1, this.cachedMaxRounds)));
   }

   private void cacheAutoGGMessages() {
      try {
         File var1 = new File(this.plugin.getDataFolder(), "menus/playersettings.yml");
         if (var1.exists()) {
            YamlConfiguration var2 = YamlConfiguration.loadConfiguration(var1);
            this.cachedAutoGGMessages = var2.getStringList("auto_gg_messages");
         }
      } catch (Exception var3) {
      }

      if (this.cachedAutoGGMessages == null || this.cachedAutoGGMessages.isEmpty()) {
         this.cachedAutoGGMessages = Arrays.asList("gg", "good game", "well played");
      }
   }

   private void cacheSounds() {
      this.cachedCountdownSound = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig(), "match.sounds.countdown", "BLOCK_NOTE_BLOCK_PLING"
      );
      this.cachedStartSound = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig(), "match.sounds.start", "BLOCK_NOTE_BLOCK_CHIME"
      );
      this.cachedWinSound = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig(), "match.sounds.win", "ENTITY_PLAYER_LEVELUP"
      );
      this.cachedLoseSound = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig(), "match.sounds.lose", "ENTITY_VILLAGER_DEATH"
      );
      this.cachedLeaveSound = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig(), "match.leave-delay.sound", "ENTITY_VILLAGER_DEATH"
      );
      this.cachedInactivityRevealSound = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig(), "match.inactivity-reveal.sound", "BLOCK_NOTE_BLOCK_BELL"
      );
   }

   private void playDuelSound(Player var1, k var2, String var3) {
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
                           this.plugin.getLogger().info("[Duel Sound Debug] Played " + var3 + " sound " + var2 + " for " + var4x.getName());
                        }
                     } catch (Exception var6) {
                        this.plugin
                           .getLogger()
                           .warning("[Duel Sound] Failed to play " + var3 + " sound " + var2 + " for " + var4x.getName() + ": " + var6.getMessage());
                     }
                  }
               },
               2L
            );
      }
   }

   public void sendDuelRequest(Player var1, Player var2, String var3, int var4) {
      this.sendDuelRequest(var1, var2, var3, var4, null);
   }

   public void sendDuelRequest(Player var1, Player var2, String var3, int var4, String var5) {
      if (var1 != null && var2 != null && var3 != null && !var3.isBlank()) {
         if (this.isPlayerBusyForDuel(var1)) {
            var1.sendMessage(Component.text("You are already in a match!").color(NamedTextColor.RED));
         } else if (this.isPlayerBusyForDuel(var2)) {
            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1, "duel-player-busy", Map.of("player", var2.getName())
               );
         } else {
            if (!this.isCustomKit(var3)) {
               if (m.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                  this.plugin, var1, var3
               )) {
                  return;
               }

               if (ColorPartyKitGuard.blockDuelUse(this.plugin, var1, var3)) {
                  return;
               }

               if (!this.plugin
                  .getKitManager()
                  .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                     var1, var3
                  )) {
                  return;
               }
            } else if (this.customKitData(var1.getUniqueId(), var2.getUniqueId()) == null) {
               this.plugin
                  .getMessagesManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1, "duel-kit-not-found"
                  );
               return;
            }

            e var6 = new e(var1.getUniqueId(), var2.getUniqueId(), var3, var4, false, var5);
            List var7 = this.pendingRequests.computeIfAbsent(var2.getUniqueId(), var0 -> new CopyOnWriteArrayList<>());
            var7.removeIf(
               var1x -> var1x.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                     .equals(var1.getUniqueId())
            );
            var7.add(var6);
            String var8 = this.getKitDisplayName(var3, var1.getUniqueId(), var2.getUniqueId());
            Map var9 = Map.of("target", var2.getName(), "kit", var8, "rounds", String.valueOf(var4));
            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1, "duel-request-sent", var9
               );
            Map var10 = Map.of("sender", var1.getName(), "kit", var8, "rounds", String.valueOf(var4));
            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var2, "duel-request-received", var10
               );
            this.sendClickableAcceptMessage(var2, var1.getName());
         }
      }
   }

   public void sendRankedDuelRequest(Player var1, Player var2, String var3, int var4) {
      this.sendRankedDuelRequest(var1, var2, var3, var4, null);
   }

   public void sendRankedDuelRequest(Player var1, Player var2, String var3, int var4, String var5) {
      if (var1 != null && var2 != null && var3 != null && !var3.isBlank()) {
         if (this.isPlayerBusyForDuel(var1)) {
            var1.sendMessage(Component.text("You are already in a match!").color(NamedTextColor.RED));
         } else if (this.isPlayerBusyForDuel(var2)) {
            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1, "duel-player-busy", Map.of("player", var2.getName())
               );
         } else if (!m.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
            this.plugin, var1, var3
         )) {
            if (!ColorPartyKitGuard.blockDuelUse(this.plugin, var1, var3)) {
               if (this.plugin
                  .getKitManager()
                  .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                     var1, var3
                  )) {
                  e var6 = new e(var1.getUniqueId(), var2.getUniqueId(), var3, var4, true, var5);
                  List var7 = this.pendingRequests.computeIfAbsent(var2.getUniqueId(), var0 -> new CopyOnWriteArrayList<>());
                  var7.removeIf(
                     var1x -> var1x.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                           .equals(var1.getUniqueId())
                  );
                  var7.add(var6);
                  Map var8 = Map.of("target", var2.getName(), "kit", var3, "rounds", String.valueOf(var4));
                  this.plugin
                     .getMessagesManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var1, "duel-request-sent", var8
                     );
                  Map var9 = Map.of("sender", var1.getName(), "kit", var3, "rounds", String.valueOf(var4));
                  this.plugin
                     .getMessagesManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var2, "duel-request-received", var9
                     );
                  this.sendClickableAcceptMessage(var2, var1.getName());
               }
            }
         }
      }
   }

   public boolean acceptDuel(Player var1, Player var2) {
      if (!this.tryLockDuelAccept(var1.getUniqueId(), var2.getUniqueId())) {
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "duel-player-busy", Map.of("player", var2.getName())
            );
         return false;
      } else {
         boolean var3;
         try {
            var3 = this.acceptDuelLocked(var1, var2);
         } finally {
            this.unlockDuelAccept(var1.getUniqueId(), var2.getUniqueId());
         }

         return var3;
      }
   }

   public boolean acceptOpenChallenge(Player var1, Player var2, String var3, int var4) {
      if (!this.tryLockDuelAccept(var1.getUniqueId(), var2.getUniqueId())) {
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "duel-player-busy", Map.of("player", var2.getName())
            );
         return false;
      } else {
         boolean var5;
         try {
            var5 = this.acceptOpenChallengeLocked(var1, var2, var3, var4);
         } finally {
            this.unlockDuelAccept(var1.getUniqueId(), var2.getUniqueId());
         }

         return var5;
      }
   }

   private boolean acceptOpenChallengeLocked(Player var1, Player var2, String var3, int var4) {
      if (!var2.isOnline()) {
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "duel-player-offline"
            );
         return false;
      } else if (this.isPlayerBusyForDuel(var1)) {
         var1.sendMessage(Component.text("You are already in a match!").color(NamedTextColor.RED));
         return false;
      } else if (this.isPlayerBusyForDuel(var2)) {
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "duel-player-busy", Map.of("player", var2.getName())
            );
         return false;
      } else {
         this.clearRequestsInvolving(var1.getUniqueId(), var2.getUniqueId());
         this.forceCloseEditorIfEditing(var1);
         this.forceCloseEditorIfEditing(var2);
         if (this.plugin
            .getRandomKitManager()
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
               var3
            )) {
            String var5 = this.plugin
               .getRandomKitManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var3
               );
            if (var5 == null) {
               var1.sendMessage(Component.text("No kits available for random selection!").color(NamedTextColor.RED));
               var2.sendMessage(Component.text("No kits available for random selection!").color(NamedTextColor.RED));
               return false;
            }

            var1.sendMessage(
               ((TextComponent)Component.text("Random Kit Selected: ").color(NamedTextColor.GOLD))
                  .append(
                     LegacyComponentSerializer.legacySection()
                        .deserialize(
                           this.plugin
                              .getDataManager()
                              .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                 var5
                              )
                              .replace('&', '§')
                        )
                  )
            );
            var2.sendMessage(
               ((TextComponent)Component.text("Random Kit Selected: ").color(NamedTextColor.GOLD))
                  .append(
                     LegacyComponentSerializer.legacySection()
                        .deserialize(
                           this.plugin
                              .getDataManager()
                              .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                 var5
                              )
                              .replace('&', '§')
                        )
                  )
            );
            var3 = var5;
         }

         t var10 = null;
         ArenaType var6;
         if (this.isCustomKit(var3)) {
            CustomKitAPI.CustomKitData var7 = this.customKitData(var2.getUniqueId(), var1.getUniqueId());
            if (var7 == null) {
               this.plugin
                  .getMessagesManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1, "duel-kit-not-found"
                  );
               this.plugin
                  .getMessagesManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var2, "duel-kit-not-found"
                  );
               return false;
            }

            var6 = var7.arenaType();
         } else {
            var10 = this.plugin
               .getKitManager()
               .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                  var3
               );
            if (var10 == null) {
               this.plugin
                  .getMessagesManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1, "duel-kit-not-found"
                  );
               this.plugin
                  .getMessagesManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var2, "duel-kit-not-found"
                  );
               return false;
            }

            if (m.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
               this.plugin, var1, var3
            )) {
               m.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                  this.plugin, var2, var3
               );
               return false;
            }

            if (ColorPartyKitGuard.blockDuelUse(this.plugin, var1, var3)) {
               ColorPartyKitGuard.blockDuelUse(this.plugin, var2, var3);
               return false;
            }

            boolean var12 = this.plugin
               .getKitManager()
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                  var2, var3
               );
            boolean var8 = this.plugin
               .getKitManager()
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                  var1, var3
               );
            if (!var12 || !var8) {
               return false;
            }

            var6 = var10.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return();
         }

         this.plugin.getQueueManager().removeFromAllQueues(var1.getUniqueId());
         this.plugin.getQueueManager().removeFromAllQueues(var2.getUniqueId());
         this.plugin.getRankedManager().leaveQueue(var1.getUniqueId());
         this.plugin.getRankedManager().leaveQueue(var2.getUniqueId());
         if (this.shouldTransferCrossServer()) {
            return this.transferDuelCrossServer(var1, var2, var3, Math.max(1, var4), false);
         } else {
            org.lime.swiftCore.arena.d var13 = this.arenaManager
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var6, var3
               );
            if (var13 == null) {
               String var14 = var3;
               int var9 = Math.max(1, var4);
               if (!this.markArenaPending(var1.getUniqueId(), var2.getUniqueId())) {
                  return false;
               } else {
                  this.plugin
                     .getDynamicArenaManager()
                     .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                        var6, var3
                     )
                     .whenComplete(
                        (var6x, var7x) -> Bukkit.getScheduler()
                              .runTask(
                                 this.plugin,
                                 () -> {
                                    this.clearArenaPending(var1.getUniqueId(), var2.getUniqueId());
                                    Player var8x = Bukkit.getPlayer(var1.getUniqueId());
                                    Player var9x = Bukkit.getPlayer(var2.getUniqueId());
                                    if (var7x != null) {
                                       this.sendNoDuelArena(var8x, var9x, var6);
                                    } else if (!this.validWaitingDuelPlayers(var8x, var9x)) {
                                       this.plugin
                                          .getDynamicArenaManager()
                                          .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                             var6x
                                          );
                                    } else {
                                       this.completeAcceptedDuel(var8x, var9x, var14, var9, var6x, false, false);
                                    }
                                 }
                              )
                     );
                  return true;
               }
            } else {
               if (this.plugin.isDebug()) {
                  this.plugin
                     .getLogger()
                     .info(
                        "[Duel Debug] openChallenge: "
                           + var1.getName()
                           + " accepted from "
                           + var2.getName()
                           + " kit="
                           + var3
                           + " arena="
                           + var13.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                     );
               }

               this.completeAcceptedDuel(var1, var2, var3, Math.max(1, var4), var13, false, false);
               return true;
            }
         }
      }
   }

   private boolean acceptDuelLocked(Player var1, Player var2) {
      List var3 = this.pendingRequests.get(var1.getUniqueId());
      if (var3 != null && !var3.isEmpty()) {
         e var4 = null;

         for (int var5 = var3.size() - 1; var5 >= 0; var5--) {
            e var6 = (e)var3.get(var5);
            if (var6.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                  .equals(var2.getUniqueId())
               && !this.isRequestExpired(var6)) {
               var4 = var6;
               break;
            }
         }

         if (var4 == null) {
            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1, "duel-no-request"
               );
            return false;
         } else if (this.plugin.getBotDuelManager() != null && this.plugin.getBotDuelManager().isInBotDuel(var1.getUniqueId())) {
            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1, "bot-already-in-bot-duel"
               );
            return false;
         } else if (this.plugin.getBotDuelManager() != null && this.plugin.getBotDuelManager().isInBotDuel(var2.getUniqueId())) {
            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1, "duel-player-busy", Map.of("player", var2.getName())
               );
            return false;
         } else {
            List var11 = this.pendingRequests.get(var1.getUniqueId());
            if (var11 != null) {
               var11.remove(var4);
               if (var11.isEmpty()) {
                  this.pendingRequests.remove(var1.getUniqueId());
               }
            }

            if (!var2.isOnline()) {
               this.plugin
                  .getMessagesManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1, "duel-player-offline"
                  );
               return false;
            } else if (this.isPlayerBusyForDuel(var1)) {
               var1.sendMessage(Component.text("You are already in a match!").color(NamedTextColor.RED));
               return false;
            } else if (this.isPlayerBusyForDuel(var2)) {
               this.plugin
                  .getMessagesManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1, "duel-player-busy", Map.of("player", var2.getName())
                  );
               return false;
            } else {
               this.clearRequestsInvolving(var1.getUniqueId(), var2.getUniqueId());
               this.forceCloseEditorIfEditing(var1);
               this.forceCloseEditorIfEditing(var2);
               String var12 = var4.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void();
               if (this.plugin
                  .getRandomKitManager()
                  .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                     var12
                  )) {
                  String var7 = this.plugin
                     .getRandomKitManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var12
                     );
                  if (var7 == null) {
                     var1.sendMessage(Component.text("No kits available for random selection!").color(NamedTextColor.RED));
                     var2.sendMessage(Component.text("No kits available for random selection!").color(NamedTextColor.RED));
                     return false;
                  }

                  var1.sendMessage(
                     ((TextComponent)Component.text("Random Kit Selected: ").color(NamedTextColor.GOLD))
                        .append(
                           LegacyComponentSerializer.legacySection()
                              .deserialize(
                                 this.plugin
                                    .getDataManager()
                                    .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                       var7
                                    )
                                    .replace('&', '§')
                              )
                        )
                  );
                  var2.sendMessage(
                     ((TextComponent)Component.text("Random Kit Selected: ").color(NamedTextColor.GOLD))
                        .append(
                           LegacyComponentSerializer.legacySection()
                              .deserialize(
                                 this.plugin
                                    .getDataManager()
                                    .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                       var7
                                    )
                                    .replace('&', '§')
                              )
                        )
                  );
                  var12 = var7;
               }

               ArenaType var13;
               if (this.isCustomKit(var12)) {
                  CustomKitAPI.CustomKitData var8 = this.customKitData(var2.getUniqueId(), var1.getUniqueId());
                  if (var8 == null) {
                     this.plugin
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var1, "duel-kit-not-found"
                        );
                     this.plugin
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var2, "duel-kit-not-found"
                        );
                     return false;
                  }

                  var13 = var8.arenaType();
               } else {
                  t var14 = this.plugin
                     .getKitManager()
                     .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                        var12
                     );
                  if (var14 == null) {
                     this.plugin
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var1, "duel-kit-not-found"
                        );
                     this.plugin
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var2, "duel-kit-not-found"
                        );
                     return false;
                  }

                  if (m.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                     this.plugin, var1, var12
                  )) {
                     m.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                        this.plugin, var2, var12
                     );
                     return false;
                  }

                  if (ColorPartyKitGuard.blockDuelUse(this.plugin, var1, var12)) {
                     ColorPartyKitGuard.blockDuelUse(this.plugin, var2, var12);
                     return false;
                  }

                  boolean var9 = this.plugin
                     .getKitManager()
                     .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                        var2, var12
                     );
                  boolean var10 = this.plugin
                     .getKitManager()
                     .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                        var1, var12
                     );
                  if (!var9 || !var10) {
                     return false;
                  }

                  var13 = var14.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return();
               }

               this.plugin.getQueueManager().removeFromAllQueues(var1.getUniqueId());
               this.plugin.getQueueManager().removeFromAllQueues(var2.getUniqueId());
               this.plugin.getRankedManager().leaveQueue(var1.getUniqueId());
               this.plugin.getRankedManager().leaveQueue(var2.getUniqueId());
               if (this.shouldTransferCrossServer()) {
                  return this.transferDuelCrossServer(
                     var1,
                     var2,
                     var12,
                     var4.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                     var4.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                  );
               } else {
                  org.lime.swiftCore.arena.d var15 = this.getRequestedOrAvailableArena(var4, var13, var12);
                  if (var15 == null) {
                     if (var4.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                           != null
                        && !var4.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                           .isBlank()) {
                        if (this.plugin.isDebug()) {
                           this.plugin.getLogger().warning("[Duel Debug] acceptDuel: no arena available for kit=" + var12 + " type=" + var13);
                        }

                        Map var17 = Map.of("arena_type", var13.name());
                        this.plugin
                           .getMessagesManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var1, "duel-no-arenas", var17
                           );
                        this.plugin
                           .getMessagesManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var2, "duel-no-arenas", var17
                           );
                        return false;
                     } else {
                        String var16 = var12;
                        e var18 = var4;
                        if (!this.markArenaPending(var1.getUniqueId(), var2.getUniqueId())) {
                           return false;
                        } else {
                           this.plugin
                              .getDynamicArenaManager()
                              .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                                 var13, var12
                              )
                              .whenComplete(
                                 (var6x, var7x) -> Bukkit.getScheduler()
                                       .runTask(
                                          this.plugin,
                                          () -> {
                                             this.clearArenaPending(var1.getUniqueId(), var2.getUniqueId());
                                             Player var8x = Bukkit.getPlayer(var1.getUniqueId());
                                             Player var9x = Bukkit.getPlayer(var2.getUniqueId());
                                             if (var7x != null) {
                                                this.sendNoDuelArena(var8x, var9x, var13);
                                             } else if (!this.validWaitingDuelPlayers(var8x, var9x)) {
                                                this.plugin
                                                   .getDynamicArenaManager()
                                                   .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                                      var6x
                                                   );
                                             } else {
                                                this.completeAcceptedDuel(
                                                   var8x,
                                                   var9x,
                                                   var16,
                                                   var18.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                                                   var6x,
                                                   var18.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(),
                                                   false
                                                );
                                             }
                                          }
                                       )
                              );
                           return true;
                        }
                     }
                  } else {
                     if (this.plugin.isDebug()) {
                        this.plugin
                           .getLogger()
                           .info(
                              "[Duel Debug] acceptDuel: "
                                 + var1.getName()
                                 + " accepted from "
                                 + var2.getName()
                                 + " kit="
                                 + var12
                                 + " arena="
                                 + var15.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                           );
                     }

                     this.completeAcceptedDuel(
                        var1,
                        var2,
                        var12,
                        var4.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                        var15,
                        var4.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(),
                        false
                     );
                     return true;
                  }
               }
            }
         }
      } else {
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "duel-no-request"
            );
         return false;
      }
   }

   private org.lime.swiftCore.arena.d getRequestedOrAvailableArena(e var1, ArenaType var2, String var3) {
      String var4 = var1.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return();
      if (var4 != null && !var4.isBlank()) {
         org.lime.swiftCore.arena.d var5 = this.arenaManager
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var4
            );
         return var5 == null
               || var5.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                  != org.lime.swiftCore.arena.c.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
               || !var5.Õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000floatsuper()
               || var2 != null
                  && var5.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
                     != var2
               || var5.øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interfacesuper()
               || var5.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
               || !var5.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
                     var3
                  )
                  && var5.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper()
            ? null
            : var5;
      } else {
         return this.arenaManager
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var2, var3
            );
      }
   }

   private boolean markArenaPending(UUID var1, UUID var2) {
      if (!this.pendingArenaPlayers.add(var1)) {
         return false;
      } else if (!this.pendingArenaPlayers.add(var2)) {
         this.pendingArenaPlayers.remove(var1);
         return false;
      } else {
         return true;
      }
   }

   private void clearArenaPending(UUID var1, UUID var2) {
      this.pendingArenaPlayers.remove(var1);
      this.pendingArenaPlayers.remove(var2);
   }

   private boolean validWaitingDuelPlayers(Player var1, Player var2) {
      return var1 != null && var1.isOnline() && var2 != null && var2.isOnline() && !this.isPlayerBusyForDuel(var1) && !this.isPlayerBusyForDuel(var2);
   }

   private void sendNoDuelArena(Player var1, Player var2, ArenaType var3) {
      Map var4 = Map.of("arena_type", var3.name());
      if (var1 != null && var1.isOnline()) {
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "duel-no-arenas", var4
            );
      }

      if (var2 != null && var2.isOnline()) {
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var2, "duel-no-arenas", var4
            );
      }
   }

   private void completeAcceptedDuel(Player var1, Player var2, String var3, int var4, org.lime.swiftCore.arena.d var5, boolean var6, boolean var7) {
      if (var5.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
            != org.lime.swiftCore.arena.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
         && !this.arenaManager
            .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(
               var5
            )) {
         this.sendNoDuelArena(
            var1,
            var2,
            var5.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
         );
      } else {
         String var8 = this.getKitDisplayName(var3, var2.getUniqueId(), var1.getUniqueId());
         Map var9 = Map.of(
            "opponent",
            this.focusOpponentName(var1, var2),
            "kit",
            var8,
            "map",
            var5.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(),
            "total_rounds",
            String.valueOf(var4)
         );
         Map var10 = Map.of(
            "opponent",
            this.focusOpponentName(var2, var1),
            "kit",
            var8,
            "map",
            var5.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(),
            "total_rounds",
            String.valueOf(var4)
         );
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "duel-accepted", var9
            );
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var2, "duel-accepted", var10
            );
         this.plugin
            .getDataManager()
            .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
               var1.getUniqueId(), var3
            );
         this.plugin
            .getDataManager()
            .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
               var2.getUniqueId(), var3
            );
         this.startMatch(var2, var1, var3, var4, var5, var6, var7);
      }
   }

   private void startMatch(Player var1, Player var2, String var3, int var4, org.lime.swiftCore.arena.d var5) {
      this.startMatch(var1, var2, var3, var4, var5, false, false);
   }

   private void startMatch(Player var1, Player var2, String var3, int var4, org.lime.swiftCore.arena.d var5, boolean var6, boolean var7) {
      if (var4 > this.cachedMaxRounds) {
         var4 = this.cachedMaxRounds;
      }

      if (this.plugin.isDebug()) {
         this.plugin
            .getLogger()
            .info(
               "[Duel Debug] startMatch: "
                  + var1.getName()
                  + " vs "
                  + var2.getName()
                  + " kit="
                  + var3
                  + " arena="
                  + (
                     var5 != null
                        ? var5.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                        : "null"
                  )
                  + " rounds="
                  + var4
                  + " ranked="
                  + var6
                  + " queue="
                  + var7
            );
      }

      g var8 = new g(var1.getUniqueId(), var2.getUniqueId(), var3, var4, var5, var6, var7);
      var8.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
         this.cachedFirstTo
      );
      var8.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
         true
      );
      this.plugin.getArenaListener().cleanupTempRespawnSpectators(var1.getUniqueId(), var2.getUniqueId());
      this.plugin
         .getSpectatorManager()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1.getUniqueId()
         );
      this.plugin
         .getSpectatorManager()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var2.getUniqueId()
         );
      this.plugin.getArenaListener().addTeleportGracePeriod(var1.getUniqueId());
      this.plugin.getArenaListener().addTeleportGracePeriod(var2.getUniqueId());
      this.registerActiveMatch(var8);
      this.plugin.getKitRulesListener().addActivePlayer(var1.getUniqueId(), var3);
      this.plugin.getKitRulesListener().addActivePlayer(var2.getUniqueId(), var3);
      if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().getActiveMatchRegistry() != null) {
         this.plugin
            .getLimboManager()
            .getActiveMatchRegistry()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var8.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper(),
               List.of(
                  var8.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
                  var8.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
               ),
               var8.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                  .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(),
               var8.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
               "DUEL"
            );
      }

      boolean var9 = this.getKitRule(var3, KitRule.HP_INDICATOR);
      if (this.plugin.isDebug()) {
         this.plugin.getLogger().info("[HP Indicator Debug] unranked/startMatch kit=" + var3 + " enabled=" + var9);
      }

      if (this.plugin.getHPIndicatorManager() != null && var9) {
         this.plugin
            .getHPIndicatorManager()
            .startForMatch(
               var1.getUniqueId(),
               var2.getUniqueId(),
               var5.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
            );
      }

      if (var5.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
         != org.lime.swiftCore.arena.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
         )
       {
         this.arenaManager
            .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(
               var5
            );
      }

      this.arenaManager
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1.getUniqueId(),
            var5.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
         );
      this.arenaManager
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var2.getUniqueId(),
            var5.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
         );
      this.plugin
         .getChunkyIntegration()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var5
         );
      boolean var10 = this.isOnCrossServerArena();
      if (!var10) {
         this.matchFoundEffects
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, var2, var3
            );
      } else if (this.plugin.isDebug()) {
         this.plugin
            .getLogger()
            .info(
               "[CrossServer Debug] Starting match directly on arena server. Skipping match-found effects and lobby delay for "
                  + var1.getName()
                  + " vs "
                  + var2.getName()
                  + "."
            );
      }

      UUID var11 = var1.getUniqueId();
      UUID var12 = var2.getUniqueId();
      Bukkit.getScheduler()
         .runTaskLater(
            this.plugin,
            () -> {
               if (!var8.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                  )
                {
                  Player var7x = Bukkit.getPlayer(var11);
                  Player var8x = Bukkit.getPlayer(var12);
                  if (var7x != null && var7x.isOnline() && var8x != null && var8x.isOnline()) {
                     var7x.getInventory().clear();
                     var8x.getInventory().clear();
                     this.applyInitialDuelHealth(var7x, var3);
                     this.applyInitialDuelHealth(var8x, var3);
                     this.prepareArenaCombatant(var7x);
                     this.prepareArenaCombatant(var8x);
                     this.plugin
                        .getPlayerSettingsManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           List.of(var7x, var8x)
                        );
                     this.teleportPlayersToArenaForMatchStart(
                        var7x,
                        var8x,
                        var5,
                        var3,
                        () -> {
                           if (!var8.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                              )
                            {
                              Player var6xx = Bukkit.getPlayer(var11);
                              Player var7xx = Bukkit.getPlayer(var12);
                              if (var6xx != null && var6xx.isOnline() && var7xx != null && var7xx.isOnline()) {
                                 var8.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
                                    true
                                 );
                                 this.plugin
                                    .getPlayerSettingsManager()
                                    .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                       List.of(var6xx, var7xx)
                                    );
                                 if (this.plugin.getHPIndicatorManager() != null && this.plugin.getHPIndicatorManager().isActive(var11)) {
                                    this.plugin.getHPIndicatorManager().refreshForMatch(var11, var12);
                                 }

                                 if (this.getKitRule(var3, KitRule.BEDWARS)) {
                                    var5.õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper();
                                 }

                                 this.plugin
                                    .getSkyWarsLootManager()
                                    .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                       var5, var3
                                    );
                                 if (this.getKitRule(var3, KitRule.PARKOUR)) {
                                    this.plugin
                                       .getParkourManager()
                                       .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                          var11,
                                          var5.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                       );
                                    this.plugin
                                       .getParkourManager()
                                       .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                          var12,
                                          var5.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                       );
                                 }

                                 if (this.plugin.getPillarsOfFortuneManager() != null
                                    && this.plugin
                                       .getPillarsOfFortuneManager()
                                       .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                          var3
                                       )) {
                                    this.plugin
                                       .getPillarsOfFortuneManager()
                                       .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                          var8, List.of(var6xx, var7xx)
                                       );
                                 }

                                 this.updateScoreboards(var8);
                                 this.giveKitsWithRetry(
                                    var11,
                                    var12,
                                    var3,
                                    var5,
                                    var8,
                                    0,
                                    () -> {
                                       if (!var8.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                                          )
                                        {
                                          this.startCountdown(
                                             var6xx,
                                             var7xx,
                                             var8,
                                             true,
                                             () -> this.matchFoundEffects
                                                   .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                                      var6xx, var7xx, var5, var8, null
                                                   )
                                          );
                                       }
                                    }
                                 );
                              } else {
                                 this.endMatch(var8, null);
                              }
                           }
                        }
                     );
                     int var9x = this.getMaxFightDuration(var3);
                     Bukkit.getScheduler()
                        .runTaskLater(
                           this.plugin,
                           () -> {
                              if (this.activeMatches.containsKey(var11)
                                 && !var8.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                                 )
                               {
                                 Player var4xx = Bukkit.getPlayer(var11);
                                 Player var5xx = Bukkit.getPlayer(var12);
                                 if (var4xx != null) {
                                    var4xx.sendMessage(Component.text("Match ended due to time limit").color(NamedTextColor.YELLOW));
                                 }

                                 if (var5xx != null) {
                                    var5xx.sendMessage(Component.text("Match ended due to time limit").color(NamedTextColor.YELLOW));
                                 }

                                 this.endMatch(var8, null);
                              }
                           },
                           (long)(var9x * 60) * 20L
                        );
                  } else {
                     if (var7x != null) {
                        var7x.sendMessage(Component.text("Match cancelled - opponent disconnected").color(NamedTextColor.RED));
                     }

                     if (var8x != null) {
                        var8x.sendMessage(Component.text("Match cancelled - opponent disconnected").color(NamedTextColor.RED));
                     }

                     this.endMatch(var8, null);
                  }
               }
            },
            var10
               ? 0L
               : (long)this.matchFoundEffects
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
         );
   }

   private void prepareArenaCombatant(Player var1) {
      if (var1 != null && var1.isOnline()) {
         var1.clearTitle();
         var1.setGameMode(GameMode.SURVIVAL);
         var1.setGravity(true);
         var1.setAllowFlight(false);
         var1.setFlying(false);
         var1.setInvulnerable(false);
         var1.setGlowing(false);
         var1.setFireTicks(0);
         var1.setFallDistance(0.0F);
         var1.setCollidable(true);
         var1.setWalkSpeed(0.2F);
         var1.setFlySpeed(0.1F);
         var1.removePotionEffect(PotionEffectType.BLINDNESS);
      }
   }

   private void teleportPlayersToArena(Player var1, Player var2, org.lime.swiftCore.arena.d var3) {
      Location var4 = var3.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper();
      Location var5 = var3.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper();
      if (var1 != null && var1.isOnline() && var4 != null && var4.getWorld() != null) {
         this.teleportPlayerToLocation(var1, var4);
      } else if (var1 != null && var1.isOnline()) {
         var1.sendMessage(Component.text("Arena position 1 world is not loaded!").color(NamedTextColor.RED));
      }

      if (var2 != null && var2.isOnline() && var5 != null && var5.getWorld() != null) {
         this.teleportPlayerToLocation(var2, var5);
      } else if (var2 != null && var2.isOnline()) {
         var2.sendMessage(Component.text("Arena position 2 world is not loaded!").color(NamedTextColor.RED));
      }
   }

   private void teleportPlayersToArenaForMatchStart(Player var1, Player var2, org.lime.swiftCore.arena.d var3, String var4, Runnable var5) {
      if (this.plugin.getBallisticEntryManager() == null
         || !this.plugin
            .getBallisticEntryManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, var2, var3, var4, var5
            )) {
         if (this.plugin.getArenaElevatorManager() == null
            || !this.plugin
               .getArenaElevatorManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1, var2, var3, var4, var5
               )) {
            this.teleportPlayersToArena(var1, var2, var3);
            Bukkit.getScheduler().runTaskLater(this.plugin, var5, 3L);
         }
      }
   }

   private void teleportPlayerToLocation(Player var1, Location var2) {
      this.plugin.getArenaListener().addTeleportGracePeriod(var1.getUniqueId());
      if (var1.isInsideVehicle()) {
         var1.leaveVehicle();
      }

      if (!var1.getPassengers().isEmpty()) {
         for (Entity var4 : var1.getPassengers()) {
            var1.removePassenger(var4);
         }
      }

      Location var5 = new Location(var2.getWorld(), var2.getX(), var2.getY(), var2.getZ(), var2.getYaw(), var2.getPitch());
      var1.teleportAsync(var5, TeleportCause.PLUGIN);
   }

   private void giveKits(Player var1, Player var2, String var3) {
      if (this.isCustomKit(var3)) {
         this.giveCustomKits(var1, var2);
      } else {
         boolean var4 = this.plugin
            .getDataManager()
            .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
               var3
            )
            .getOrDefault(KitRule.BRIDGE, KitRule.BRIDGE.getDefaultValue());
         boolean var5 = this.plugin
            .getDataManager()
            .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
               var3
            )
            .getOrDefault(KitRule.BEDWARS, KitRule.BEDWARS.getDefaultValue());
         boolean var6 = this.cachedKitColoredArmor;
         if (!var4 && !var5 && !var6) {
            if (var1 != null && var1.isOnline()) {
               this.plugin
                  .getKitManager()
                  .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
                     var1, var3
                  );
            }

            if (var2 != null && var2.isOnline()) {
               this.plugin
                  .getKitManager()
                  .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
                     var2, var3
                  );
            }
         } else {
            if (var1 != null && var1.isOnline()) {
               this.plugin
                  .getKitManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1, var3, "blue"
                  );
            }

            if (var2 != null && var2.isOnline()) {
               this.plugin
                  .getKitManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var2, var3, "red"
                  );
            }
         }
      }
   }

   private void giveCustomKits(Player var1, Player var2) {
      if (this.plugin.getCustomKitAPI() != null) {
         CustomKitAPI.CustomKitData var3 = var1 != null ? this.plugin.getCustomKitAPI().getKit(var1.getUniqueId()) : null;
         CustomKitAPI.CustomKitData var4 = var2 != null ? this.plugin.getCustomKitAPI().getKit(var2.getUniqueId()) : null;
         CustomKitAPI.CustomKitData var5 = var3 != null ? var3 : var4;
         if (var1 != null && var1.isOnline()) {
            CustomKitAPI.CustomKitData var6 = var3 != null ? var3 : var5;
            if (var6 != null) {
               if (this.plugin.getSpawnItemsManager() != null) {
                  this.plugin.getSpawnItemsManager().clearTracking(var1);
               }

               var1.getInventory().setContents(var6.contents());
               var1.getInventory().setArmorContents(var6.armor());
               var1.updateInventory();
            }
         }

         if (var2 != null && var2.isOnline()) {
            CustomKitAPI.CustomKitData var7 = var4 != null ? var4 : var5;
            if (var7 != null) {
               if (this.plugin.getSpawnItemsManager() != null) {
                  this.plugin.getSpawnItemsManager().clearTracking(var2);
               }

               var2.getInventory().setContents(var7.contents());
               var2.getInventory().setArmorContents(var7.armor());
               var2.updateInventory();
            }
         }
      }
   }

   private boolean isCustomKit(String var1) {
      return var1 != null && var1.startsWith("customkit");
   }

   private CustomKitAPI.CustomKitData customKitData(UUID var1, UUID var2) {
      if (this.plugin.getCustomKitAPI() == null) {
         return null;
      } else {
         CustomKitAPI.CustomKitData var3 = this.plugin.getCustomKitAPI().getKit(var1);
         return var3 != null ? var3 : this.plugin.getCustomKitAPI().getKit(var2);
      }
   }

   private String getKitDisplayName(String var1, UUID var2, UUID var3) {
      if (this.isCustomKit(var1)) {
         CustomKitAPI.CustomKitData var4 = this.customKitData(var2, var3);
         return var4 != null && var4.displayName() != null && !var4.displayName().isBlank() ? var4.displayName() : "Custom Kit";
      } else {
         return this.plugin
            .getDataManager()
            .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
               var1
            );
      }
   }

   private void giveKitsWithRetry(UUID var1, UUID var2, String var3, org.lime.swiftCore.arena.d var4, g var5, int var6) {
      this.giveKitsWithRetry(var1, var2, var3, var4, var5, var6, null);
   }

   private void giveKitsWithRetry(UUID var1, UUID var2, String var3, org.lime.swiftCore.arena.d var4, g var5, int var6, Runnable var7) {
      int var8 = var6 == 0 ? 3 : 10;
      Bukkit.getScheduler()
         .runTaskLater(
            this.plugin,
            () -> {
               if (!var5.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                  )
                {
                  Player var8x = Bukkit.getPlayer(var1);
                  Player var9 = Bukkit.getPlayer(var2);
                  if (var8x != null && var8x.isOnline() && var9 != null && var9.isOnline()) {
                     World var13 = var4.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper()
                           != null
                        ? var4.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper()
                           .getWorld()
                        : null;
                     boolean var11 = var13 == null || var8x.getWorld().equals(var13);
                     boolean var12 = var9 == null || !var9.isOnline() || var13 == null || var9.getWorld().equals(var13);
                     if (var11 && var12) {
                        this.giveKits(var8x, var9, var3);
                        if (var7 != null) {
                           var7.run();
                        }
                     } else if (var6 < 5) {
                        this.giveKitsWithRetry(var1, var2, var3, var4, var5, var6 + 1, var7);
                     } else {
                        this.giveKits(var8x, var9, var3);
                        if (var7 != null) {
                           var7.run();
                        }
                     }
                  } else {
                     if (var6 < 5) {
                        this.giveKitsWithRetry(var1, var2, var3, var4, var5, var6 + 1, var7);
                     } else {
                        var5.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                           false
                        );
                        UUID var10 = var8x != null && var8x.isOnline() ? var1 : (var9 != null && var9.isOnline() ? var2 : null);
                        this.endMatch(var5, var10);
                     }
                  }
               }
            },
            (long)var8
         );
   }

   private int getMaxFightDuration(String var1) {
      boolean var2 = this.plugin
         .getDataManager()
         .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
            var1
         )
         .getOrDefault(KitRule.MAX_FIGHT_DURATION, KitRule.MAX_FIGHT_DURATION.getDefaultValue());
      return var2 ? this.plugin.getConfig().getInt("kit-rules.max-fight-duration." + var1, this.cachedMaxFightDuration) : this.cachedMaxFightDuration;
   }

   private void applyInitialDuelHealth(Player var1, String var2) {
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
         org.lime.swiftCore.b.h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
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

         org.lime.swiftCore.b.h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1
         );
      }
   }

   private void resetDuelRoundHealth(Player var1, String var2) {
      AttributeInstance var3 = org.lime.swiftCore.b.b.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
         var1
      );
      if (var3 == null) {
         org.lime.swiftCore.b.b.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1
         );
         org.lime.swiftCore.b.h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1
         );
      } else {
         boolean var4 = this.plugin
            .getDataManager()
            .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
               var2
            )
            .getOrDefault(KitRule.HEARTS, KitRule.HEARTS.getDefaultValue());
         if (var4) {
            double var5 = this.plugin.getConfig().getDouble("kit-rules.hearts." + var2, 10.0);
            if (var5 < 1.0) {
               var5 = 10.0;
            }

            double var7 = var5 * 2.0;
            var3.setBaseValue(var7);
            var1.setHealth(Math.min(var7, var3.getValue()));
            var1.setHealthScaled(true);
            var1.setHealthScale(var7);
         } else {
            var3.setBaseValue(20.0);
            var1.setHealth(Math.min(20.0, var3.getValue()));
            var1.setHealthScaled(false);
         }

         org.lime.swiftCore.b.h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1
         );
      }
   }

   private void resetDuelMaxHealth(Player var1) {
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
   }

   private void startCountdown(Player var1, Player var2, g var3, boolean var4) {
      this.startCountdown(var1, var2, var3, var4, null);
   }

   private void startCountdown(Player var1, Player var2, g var3, boolean var4, Runnable var5) {
      var3.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
         true
      );
      var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
         .clear();
      if (var5 != null) {
         var5.run();
      }

      int var6 = this.cachedCountdownTime;
      List var7 = this.cachedTitles;
      byte var8 = 20;
      long var9 = 0L;
      int var11 = Math.min(var7.size(), Math.max(0, var6));
      if (var11 <= 0) {
         if (!var3.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
            && var3.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper()
            )
          {
            this.finishCountdown(var3, var4);
         }
      } else {
         for (int var12 = 0; var12 < var11; var12++) {
            Map var13 = (Map)var7.get(var12);
            long var14 = 0L + (long)(var12 * 20);
            int var16 = Bukkit.getScheduler()
               .runTaskLater(
                  this.plugin,
                  () -> {
                     if (!var3.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                        && var3.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper()
                        )
                      {
                        Object var5x = var13.get("title");
                        Object var6x = var13.get("subtitle");
                        String var7x = var5x != null ? var5x.toString() : "";
                        String var8x = var6x != null ? var6x.toString() : "";
                        float var9x = this.cachedCountdownSound != null
                           ? this.cachedCountdownSound
                              .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                           : 1.0F;
                        Object var10 = var13.get("pitch");
                        if (var10 instanceof Number var11x) {
                           var9x = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var11x.floatValue()
                           );
                        } else if (var10 != null) {
                           try {
                              var9x = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 Float.parseFloat(var10.toString())
                              );
                           } catch (NumberFormatException var14x) {
                           }
                        }

                        k var15 = this.cachedCountdownSound == null
                           ? null
                           : new k(
                              this.cachedCountdownSound
                                 .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(),
                              this.cachedCountdownSound
                                 .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(),
                              var9x
                           );
                        TextComponent var12x = LegacyComponentSerializer.legacyAmpersand().deserialize(var7x);
                        TextComponent var13x = LegacyComponentSerializer.legacyAmpersand().deserialize(var8x);
                        if (var1 != null && var1.isOnline()) {
                           var1.showTitle(Title.title(var12x, var13x, COUNTDOWN_TIMES));
                           if (var15 != null) {
                              var15.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var1
                              );
                           }
                        }

                        if (var2 != null && var2.isOnline()) {
                           var2.showTitle(Title.title(var12x, var13x, COUNTDOWN_TIMES));
                           if (var15 != null) {
                              var15.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var2
                              );
                           }
                        }
                     }
                  },
                  var14
               )
               .getTaskId();
            var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var16
            );
         }

         int var17 = Bukkit.getScheduler()
            .runTaskLater(
               this.plugin,
               () -> {
                  if (!var3.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                     && var3.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper()
                     )
                   {
                     this.finishCountdown(var3, var4);
                  }
               },
               0L + (long)var11 * 20L
            )
            .getTaskId();
         var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var17
         );
      }
   }

   private void finishCountdown(g var1, boolean var2) {
      var1.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
         false
      );
      var1.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
         .clear();
      this.lastEngagementTimes.put(var1, System.currentTimeMillis());
      this.revealedInactiveMatches.remove(var1);
      Player var3 = Bukkit.getPlayer(
         var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
      );
      Player var4 = Bukkit.getPlayer(
         var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
      );
      this.matchFoundEffects
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1
         );
      this.prepareArenaCombatant(var3);
      this.prepareArenaCombatant(var4);
      if (var2 && this.plugin.getBallisticEntryManager() != null) {
         this.plugin
            .getBallisticEntryManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var1
            );
      }

      List var5 = this.cachedFinalTitle;
      if (!var5.isEmpty()) {
         Map var6 = (Map)var5.get(0);
         Object var7 = var6.get("title");
         Object var8 = var6.get("subtitle");
         String var9 = var7 != null ? var7.toString() : "";
         String var10 = var8 != null ? var8.toString() : "";
         TextComponent var11 = LegacyComponentSerializer.legacyAmpersand().deserialize(var9);
         TextComponent var12 = LegacyComponentSerializer.legacyAmpersand().deserialize(var10);
         if (var3 != null && var3.isOnline()) {
            var3.showTitle(Title.title(var11, var12, FINAL_TITLE_TIMES));
            this.playDuelSound(var3, this.cachedStartSound, "start");
         }

         if (var4 != null && var4.isOnline()) {
            var4.showTitle(Title.title(var11, var12, FINAL_TITLE_TIMES));
            this.playDuelSound(var4, this.cachedStartSound, "start");
         }
      }

      if (var2) {
         if (var3 != null && var3.isOnline()) {
            Map var13 = Map.of(
               "opponent",
               this.focusOpponentName(var3, var4),
               "kit",
               var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
               "map",
               var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                     != null
                  ? var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                     .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                  : "Unknown",
               "round",
               "1",
               "total_rounds",
               String.valueOf(
                  var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
               )
            );
            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var3, "match-started", var13
               );
         }

         if (var4 != null && var4.isOnline()) {
            Map var14 = Map.of(
               "opponent",
               this.focusOpponentName(var4, var3),
               "kit",
               var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
               "map",
               var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                     != null
                  ? var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                     .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                  : "Unknown",
               "round",
               "1",
               "total_rounds",
               String.valueOf(
                  var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
               )
            );
            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var4, "match-started", var14
               );
         }
      }

      if (this.plugin
         .getTntTagManager()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
         )) {
         this.plugin
            .getTntTagManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               List.of(
                  var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
                  var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
               )
            );
      }

      if (this.plugin
         .getFlowerCrownManager()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
         )) {
         this.plugin
            .getFlowerCrownManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               List.of(
                  var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
                  var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
               )
            );
      }

      if (this.plugin.getPrePotionsManager() != null) {
         this.plugin
            .getPrePotionsManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
               var3,
               var4
            );
      }

      if (this.plugin.getLavaRaiseManager() != null) {
         this.plugin.getLavaRaiseManager().startDuelRound(var1);
      }
   }

   public void handleFenceReadyUp(Player var1) {
      g var2 = this.getMatch(var1.getUniqueId());
      if (var2 != null
         && var2.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper()
         && !var2.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
         )
       {
         if (!var2.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
            var1.getUniqueId()
         )) {
            b var3 = this.matchFoundEffects
               .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class();
            if (var3.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
               && var3.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null()
               )
             {
               var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
                  var1.getUniqueId()
               );
               LegacyComponentSerializer var4 = LegacyComponentSerializer.builder().character('&').hexColors().useUnusualXRepeatedCharacterHexFormat().build();
               Player var5 = Bukkit.getPlayer(
                  var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
               );
               Player var6 = Bukkit.getPlayer(
                  var2.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
               );
               if (var2.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                  )
                {
                  TextComponent var7 = var4.deserialize(
                     var3.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if()
                  );
                  if (var5 != null && var5.isOnline()) {
                     var5.sendActionBar(var7);
                  }

                  if (var6 != null && var6.isOnline()) {
                     var6.sendActionBar(var7);
                  }

                  var2.Ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000ifsuper();
                  this.matchFoundEffects
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var2
                     );
                  boolean var8 = var2.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
                     == 1;
                  this.finishCountdown(var2, var8);
               } else {
                  var1.sendActionBar(
                     var4.deserialize(
                        var3.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
                     )
                  );
                  Player var9 = var1.getUniqueId()
                        .equals(
                           var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                        )
                     ? var6
                     : var5;
                  if (var9 != null && var9.isOnline()) {
                     var9.sendActionBar(
                        var4.deserialize(
                           var3.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                        )
                     );
                  }
               }
            }
         }
      }
   }

   public void handleBridgeGoal(g var1, UUID var2) {
      if (var1 != null
         && var2 != null
         && !var1.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
         && !var1.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper()
         && !var1.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
         && var1.õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper()
            == null) {
         this.sendInterRoundFeedback(var1, var2);
         this.endRound(var1, var2);
      }
   }

   private void sendInterRoundFeedback(g var1, UUID var2) {
      if (var1 != null && var2 != null) {
         UUID var3 = var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
            var2
         );
         Player var4 = Bukkit.getPlayer(var2);
         Player var5 = var3 != null ? Bukkit.getPlayer(var3) : null;
         boolean var6 = this.willFinishWithWin(var1, var2);
         if (var4 != null && var4.isOnline()) {
            HashMap var7 = new HashMap();
            var7.put("opponent", this.focusOpponentName(var4, var5));
            var7.put(
               "round",
               String.valueOf(
                  var1.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
               )
            );
            var7.put(
               "your_wins",
               String.valueOf(
                  var2.equals(
                        var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                     )
                     ? var1.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
                        + 1
                     : var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
                        + 1
               )
            );
            var7.put(
               "opponent_wins",
               String.valueOf(
                  var2.equals(
                        var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                     )
                     ? var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
                     : var1.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
               )
            );
            var7.put("round_winner", var4.getName());
            if (!var6) {
               this.plugin
                  .getMessagesManager()
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var4, "round-victory-title", var7
                  );
            }

            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var4, "round-won", var7
               );
         }

         if (var5 != null && var5.isOnline()) {
            HashMap var8 = new HashMap();
            var8.put("opponent", this.focusOpponentName(var5, var4));
            var8.put(
               "round",
               String.valueOf(
                  var1.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
               )
            );
            var8.put(
               "your_wins",
               String.valueOf(
                  var3.equals(
                        var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                     )
                     ? var1.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
                     : var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
               )
            );
            var8.put(
               "opponent_wins",
               String.valueOf(
                  var3.equals(
                        var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                     )
                     ? var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
                        + 1
                     : var1.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
                        + 1
               )
            );
            var8.put("round_winner", var4 != null ? var4.getName() : "Unknown");
            if (!var6) {
               this.plugin
                  .getMessagesManager()
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var5, "round-defeat-title", var8
                  );
            }

            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var5, "round-lost", var8
               );
            if (!var6) {
               this.playDuelSound(var5, this.cachedLoseSound, "round-loss");
            }
         }
      }
   }

   public void endRound(g var1, UUID var2) {
      if (!var1.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
         )
       {
         var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
            true
         );
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.recoverWedgedRound(var1), 600L);
         var1.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if(
            var2
         );
         var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
            var2
         );
         this.showRoundScoreTitle(var1, var2);
         if (var1.ÓO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000public()
            )
          {
            this.sendFinalMatchFeedback(var1, var2);
         }

         this.plugin
            .getArenaListener()
            .cleanupTempRespawnSpectators(
               var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
               var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
            );
         Player var3 = Bukkit.getPlayer(
            var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
         );
         Player var4 = Bukkit.getPlayer(
            var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
         );
         Player var5 = var2.equals(
               var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
            )
            ? var4
            : var3;
         Player var6 = var2.equals(
               var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
            )
            ? var3
            : var4;
         if (var3 != null && var4 != null) {
            if (this.invSeeEnabled) {
               this.captureInventorySnapshot(var6);
               if (!this.hasUsableInventorySnapshot(var5) || this.hasAnyInventoryItem(var5)) {
                  this.captureInventorySnapshot(var5);
               }
            }

            this.updateScoreboards(var1);
            if (var5 != null) {
               this.resetDuelRoundHealth(
                  var5,
                  var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
               );
               var5.getInventory().clear();
            }

            if (var6 != null) {
               this.resetDuelRoundHealth(
                  var6,
                  var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
               );
            }

            if (this.plugin.getLavaRaiseManager() != null) {
               this.plugin
                  .getLavaRaiseManager()
                  .stopRound(
                     var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                  );
            }

            if (var1.ÓO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000public()
               )
             {
               this.handleFinalRound(var1, var5, var3, var4);
            } else {
               var1.Òo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000nullsuper();
               if (var5 != null && var5.isOnline() && var6 != null && var6.isOnline()) {
                  this.plugin
                     .getSpectatorManager()
                     .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                        var5, var6, var1
                     );
               }

               if (var6 != null && var6.isOnline()) {
                  var6.setInvulnerable(true);
               }

               org.lime.swiftCore.arena.d var7 = var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper();
               String var8 = var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
               AtomicBoolean var9 = new AtomicBoolean(false);
               AtomicBoolean var10 = new AtomicBoolean(false);
               AtomicBoolean var11 = new AtomicBoolean(false);
               Runnable var12 = () -> {
                  if (!var1.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                     && this.activeMatches
                           .get(
                              var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                           )
                        == var1
                     && this.activeMatches
                           .get(
                              var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
                           )
                        == var1) {
                     Player var2x = Bukkit.getPlayer(
                        var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                     );
                     Player var3x = Bukkit.getPlayer(
                        var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
                     );
                     if (var2x != null && var2x.isOnline() && var3x != null && var3x.isOnline()) {
                        this.plugin
                           .getSpectatorManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var2x, var3x, var1
                           );
                        var2x.setInvulnerable(false);
                        var3x.setInvulnerable(false);
                        this.giveKitsWithRetry(
                           var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
                           var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper(),
                           var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                           var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper(),
                           var1,
                           0,
                           () -> {
                              if (!var1.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                                 && this.activeMatches
                                       .get(
                                          var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                                       )
                                    == var1) {
                                 this.startCountdown(
                                    var2x,
                                    var3x,
                                    var1,
                                    false,
                                    () -> this.matchFoundEffects
                                          .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                             var2x,
                                             var3x,
                                             var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper(),
                                             var1,
                                             null
                                          )
                                 );
                                 this.updateScoreboards(var1);
                                 var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                                    false
                                 );
                              }
                           }
                        );
                     } else {
                        this.endMatchForMissingRoundPlayer(var1, var2x, var3x);
                     }
                  }
               };
               Runnable var13 = () -> {
                  if (var9.get()
                     && var10.get()
                     && var11.compareAndSet(false, true)
                     && !var1.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                     && this.activeMatches
                           .get(
                              var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                           )
                        == var1
                     && this.activeMatches
                           .get(
                              var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
                           )
                        == var1) {
                     var12.run();
                  }
               };
               Runnable var14 = () -> {
                  if (!var1.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                     && this.activeMatches
                           .get(
                              var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                           )
                        == var1
                     && this.activeMatches
                           .get(
                              var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
                           )
                        == var1) {
                     Player var4x = Bukkit.getPlayer(
                        var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                     );
                     Player var5x = Bukkit.getPlayer(
                        var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
                     );
                     if (var4x != null && var4x.isOnline() && var5x != null && var5x.isOnline()) {
                        var4x.setGameMode(GameMode.SURVIVAL);
                        var5x.setGameMode(GameMode.SURVIVAL);
                        var4x.setAllowFlight(false);
                        var5x.setAllowFlight(false);
                        var4x.setFlying(false);
                        var5x.setFlying(false);
                        var4x.setInvulnerable(true);
                        var5x.setInvulnerable(true);
                        this.plugin
                           .getSpectatorManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var4x, var5x, var1
                           );
                        var4x.getInventory().clear();
                        var5x.getInventory().clear();
                        this.plugin
                           .getKitManager()
                           .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                              var4x
                           );
                        this.plugin
                           .getKitManager()
                           .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                              var5x
                           );
                        this.resetDuelRoundHealth(
                           var4x,
                           var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                        );
                        this.resetDuelRoundHealth(
                           var5x,
                           var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                        );
                        var1.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                           false
                        );
                        var1.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           false
                        );
                        this.plugin.getArenaListener().addTeleportGracePeriod(var4x.getUniqueId());
                        this.plugin.getArenaListener().addTeleportGracePeriod(var5x.getUniqueId());
                        this.returnPlayersToRoundSpawns(var1, var4x, var5x, () -> {
                           var9.set(true);
                           var13.run();
                        });
                     } else {
                        this.endMatchForMissingRoundPlayer(var1, var4x, var5x);
                     }
                  }
               };
               this.plugin
                  .getArenaResetManager()
                  .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                     var7,
                     () -> {
                        if (!var1.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                           )
                         {
                           this.plugin
                              .getSkyWarsLootManager()
                              .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var7, var8
                              );
                           if (this.getKitRule(
                              var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                              KitRule.BEDWARS
                           )) {
                              var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                                 .õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper();
                           }

                           var10.set(true);
                           var13.run();
                        }
                     }
                  );
               if (this.cachedNextRoundDelayTicks == 0) {
                  var14.run();
               } else {
                  Bukkit.getScheduler().runTaskLater(this.plugin, var14, (long)this.cachedNextRoundDelayTicks);
               }
            }
         } else {
            this.endMatch(var1, var2);
         }
      }
   }

   private void endMatchForMissingRoundPlayer(g var1, Player var2, Player var3) {
      if (var1 != null
         && !var1.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
         )
       {
         UUID var4 = var2 != null && var2.isOnline()
            ? var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
            : (
               var3 != null && var3.isOnline()
                  ? var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
                  : null
            );
         if (this.plugin.isDebug()) {
            this.plugin
               .getLogger()
               .warning(
                  "[Duel Debug] Ending match during between-round setup because a player disconnected. arena="
                     + (
                        var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                              != null
                           ? var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                              .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                           : "null"
                     )
               );
         }

         this.endMatch(var1, var4);
      }
   }

   private void recoverWedgedRound(g var1) {
      if (var1 != null
         && !var1.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
         && var1.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
         && this.activeMatches
               .get(
                  var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
               )
            == var1
         && this.activeMatches
               .get(
                  var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
               )
            == var1) {
         Player var2 = Bukkit.getPlayer(
            var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
         );
         Player var3 = Bukkit.getPlayer(
            var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
         );
         if (var2 != null && var2.isOnline() && var3 != null && var3.isOnline()) {
            this.plugin
               .getLogger()
               .warning(
                  "[Duel] Recovering wedged round setup for match "
                     + var1.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper()
               );
            this.plugin
               .getSpectatorManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var2, var3, var1
               );
            var2.setAllowFlight(false);
            var3.setAllowFlight(false);
            var2.setFlying(false);
            var3.setFlying(false);
            this.giveKits(
               var2,
               var3,
               var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
            );
            var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               false
            );
            if (!var1.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper()
               )
             {
               this.startCountdown(var2, var3, var1, false);
            }
         } else {
            UUID var4 = var2 != null && var2.isOnline()
               ? var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
               : (
                  var3 != null && var3.isOnline()
                     ? var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
                     : null
               );
            var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               false
            );
            this.endMatch(var1, var4);
         }
      }
   }

   private void showRoundScoreTitle(g var1, UUID var2) {
      if (this.cachedRoundScoreTitleEnabled && var1 != null && var2 != null) {
         boolean var3 = var1.ÓO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000public();
         if (!var3 || this.cachedRoundScoreTitleShowFinal) {
            UUID var4 = var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float();
            UUID var5 = var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper();
            int var6 = var1.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface();
            int var7 = var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper();
            int var8 = var1.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private();
            String var9 = this.playerName(var4);
            String var10 = this.playerName(var5);
            String var11 = var2.equals(var4) ? var9 : var10;
            Runnable var12 = () -> {
               if (var3
                  || !var1.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                     && this.activeMatches.get(var4) == var1
                     && this.activeMatches.get(var5) == var1) {
                  Player var11x = Bukkit.getPlayer(var4);
                  Player var12x = Bukkit.getPlayer(var5);
                  if (var11x != null && var11x.isOnline()) {
                     this.showRoundScoreTitleToPlayer(
                        var11x,
                        var4,
                        var9,
                        var10,
                        var6,
                        var7,
                        var11,
                        var8,
                        var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                     );
                  }

                  if (var12x != null && var12x.isOnline()) {
                     this.showRoundScoreTitleToPlayer(
                        var12x,
                        var4,
                        var9,
                        var10,
                        var6,
                        var7,
                        var11,
                        var8,
                        var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                     );
                  }
               }
            };
            if (this.cachedRoundScoreTitleDelayTicks == 0) {
               var12.run();
            } else {
               Bukkit.getScheduler().runTaskLater(this.plugin, var12, (long)this.cachedRoundScoreTitleDelayTicks);
            }
         }
      }
   }

   private void showRoundScoreTitleToPlayer(Player var1, UUID var2, String var3, String var4, int var5, int var6, String var7, int var8, int var9) {
      boolean var10 = var1.getUniqueId().equals(var2);
      HashMap var11 = new HashMap();
      var11.put("%player1%", var3);
      var11.put("%player2%", var4);
      var11.put("%player1_wins%", String.valueOf(var5));
      var11.put("%player2_wins%", String.valueOf(var6));
      var11.put("%your_wins%", String.valueOf(var10 ? var5 : var6));
      var11.put("%opponent_wins%", String.valueOf(var10 ? var6 : var5));
      var11.put("%winner%", var7);
      var11.put("%round%", String.valueOf(var8));
      var11.put("%total_rounds%", String.valueOf(var9));
      String var12 = this.replacePlaceholders(this.cachedRoundScoreTitle, var11);
      String var13 = this.replacePlaceholders(this.cachedRoundScoreSubtitle, var11);
      var1.showTitle(Title.title(this.parseColoredText(var12), this.parseColoredText(var13), this.cachedRoundScoreTitleTimes));
   }

   private String replacePlaceholders(String var1, Map<String, String> var2) {
      String var3 = var1 == null ? "" : var1;

      for (Entry var5 : var2.entrySet()) {
         var3 = var3.replace((CharSequence)var5.getKey(), (CharSequence)var5.getValue());
      }

      return var3;
   }

   private String playerName(UUID var1) {
      Player var2 = Bukkit.getPlayer(var1);
      if (var2 != null) {
         return var2.getName();
      } else {
         String var3 = Bukkit.getOfflinePlayer(var1).getName();
         return var3 != null ? var3 : var1.toString().substring(0, 8);
      }
   }

   private void returnPlayersToRoundSpawns(g var1, Player var2, Player var3, Runnable var4) {
      org.lime.swiftCore.arena.d var5 = var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper();
      boolean var6 = var5 != null
         && this.plugin.getPillarsOfFortuneManager() != null
         && this.plugin
            .getPillarsOfFortuneManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
            );
      Location var7 = var6
         ? this.plugin
            .getPillarsOfFortuneManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var5, 0
            )
         : (
            var5 != null
               ? var5.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper()
               : null
         );
      Location var8 = var6
         ? this.plugin
            .getPillarsOfFortuneManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var5, 1
            )
         : (
            var5 != null
               ? var5.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
               : null
         );
      if (var7 != null && var7.getWorld() != null && var8 != null && var8.getWorld() != null) {
         Location var9 = var7.clone();
         Location var10 = var8.clone();
         boolean var11 = this.cachedRoundRewindEnabled
            && !this.isRoundRewindDisabledForKit(
               var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
            )
            && var2.getWorld().equals(var9.getWorld())
            && var3.getWorld().equals(var10.getWorld());
         if (!var11) {
            CompletableFuture var12 = var2.teleportAsync(var9);
            CompletableFuture var13 = var3.teleportAsync(var10);
            CompletableFuture.allOf(var12, var13).whenComplete((var2x, var3x) -> Bukkit.getScheduler().runTask(this.plugin, var4));
         } else {
            this.animateRoundRewind(var1, var2, var3, var9, var10, var4);
         }
      } else {
         this.teleportPlayersToArena(var2, var3, var5);
         Bukkit.getScheduler().runTaskLater(this.plugin, var4, 3L);
      }
   }

   private boolean isRoundRewindDisabledForKit(String var1) {
      return var1 != null && this.cachedRoundRewindDisabledKits.contains(var1.toLowerCase(Locale.ROOT));
   }

   private void animateRoundRewind(final g var1, Player var2, Player var3, final Location var4, final Location var5, final Runnable var6) {
      Location var7 = var2.getLocation().clone();
      Location var8 = var3.getLocation().clone();
      final boolean var9 = var2.isOnGround();
      final boolean var10 = var3.isOnGround();
      final int var11 = this.chooseBallisticDuration(var7, var4, var9);
      final int var12 = this.chooseBallisticDuration(var8, var5, var10);
      final int var13 = Math.max(var11, var12);
      final int var14 = var13 - var11;
      final int var15 = var13 - var12;
      var2.setInvulnerable(true);
      var3.setInvulnerable(true);
      var2.setGravity(false);
      var3.setGravity(false);
      var2.setAllowFlight(true);
      var3.setAllowFlight(true);
      var2.setFlying(false);
      var3.setFlying(false);
      var2.setFallDistance(0.0F);
      var3.setFallDistance(0.0F);
      var2.setVelocity(new Vector(0, 0, 0));
      var3.setVelocity(new Vector(0, 0, 0));
      (new BukkitRunnable() {
            private int Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = 0;
            private boolean o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = false;
            private boolean Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = false;
            private boolean Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class = false;
            private boolean Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String = false;

            public void run() {
               Player var1x = Bukkit.getPlayer(
                  var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
               );
               Player var2 = Bukkit.getPlayer(
                  var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
               );
               if (!var1.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                  && var1x != null
                  && var1x.isOnline()
                  && var2 != null
                  && var2.isOnline()) {
                  this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new++;
                  var1x.setFallDistance(0.0F);
                  var2.setFallDistance(0.0F);
                  if (!this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                     && this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
                        > var14) {
                     var1x.setGravity(true);
                     var1x.setVelocity(DuelManager.this.calculateBallisticVelocity(var1x.getLocation(), var4, var11, var9));
                     this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = true;
                  }

                  if (!this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
                     && this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
                        > var15) {
                     var2.setGravity(true);
                     var2.setVelocity(DuelManager.this.calculateBallisticVelocity(var2.getLocation(), var5, var12, var10));
                     this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = true;
                  }

                  int var3 = Math.max(
                     0,
                     this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
                        - var14
                  );
                  int var4x = Math.max(
                     0,
                     this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
                        - var15
                  );
                  if (this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                     && var3 >= Math.max(2, var11 - 2)
                     && !this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class
                     && DuelManager.this.hasReachedRoundRewindTarget(var1x, var4)) {
                     DuelManager.this.stopRoundRewindMotion(var1x);
                     this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class = true;
                  }

                  if (this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
                     && var4x >= Math.max(2, var12 - 2)
                     && !this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
                     && DuelManager.this.hasReachedRoundRewindTarget(var2, var5)) {
                     DuelManager.this.stopRoundRewindMotion(var2);
                     this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String = true;
                  }

                  if (this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                     && var3 >= var11
                     && var3 < var11 + 5
                     && !this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class
                     )
                   {
                     DuelManager.this.guideRoundRewindLanding(var1x, var4);
                  }

                  if (this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
                     && var4x >= var12
                     && var4x < var12 + 5
                     && !this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
                     )
                   {
                     DuelManager.this.guideRoundRewindLanding(var2, var5);
                  }

                  if (this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
                     >= var13 + 40) {
                     if (!this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class
                        )
                      {
                        DuelManager.this.finishRoundRewindVelocity(var1x, var4);
                        this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class = true;
                     }

                     if (!this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
                        )
                      {
                        DuelManager.this.finishRoundRewindVelocity(var2, var5);
                        this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String = true;
                     }
                  }

                  if (DuelManager.this.cachedRoundRewindParticles
                     && this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
                           % 2
                        == 0) {
                     if (this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                        && !this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class
                        )
                      {
                        DuelManager.this.spawnRoundRewindParticles(var1x.getLocation());
                     }

                     if (this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
                        && !this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
                        )
                      {
                        DuelManager.this.spawnRoundRewindParticles(var2.getLocation());
                     }
                  }

                  if (this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class
                     && this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
                     )
                   {
                     this.cancel();
                     var6.run();
                  }
               } else {
                  if (var1x != null && var1x.isOnline()) {
                     DuelManager.this.finishRoundRewindVelocity(var1x);
                  }

                  if (var2 != null && var2.isOnline()) {
                     DuelManager.this.finishRoundRewindVelocity(var2);
                  }

                  this.cancel();
                  if (!var1.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                     )
                   {
                     var6.run();
                  }
               }
            }
         })
         .runTaskTimer(this.plugin, 0L, 1L);
   }

   private boolean hasReachedRoundRewindTarget(Player var1, Location var2) {
      if (!var1.getWorld().equals(var2.getWorld())) {
         return false;
      } else {
         Location var3 = var1.getLocation();
         double var4 = var3.getX() - var2.getX();
         double var6 = var3.getZ() - var2.getZ();
         return var4 * var4 + var6 * var6 <= 0.0625 && Math.abs(var3.getY() - var2.getY()) <= 0.35;
      }
   }

   private void guideRoundRewindLanding(Player var1, Location var2) {
      var1.setGravity(false);
      Vector var3 = var2.toVector().subtract(var1.getLocation().toVector());
      if (var3.lengthSquared() > 1.5625) {
         var3.normalize().multiply(1.25);
      }

      var1.setVelocity(var3);
      var1.setFallDistance(0.0F);
   }

   private int chooseBallisticDuration(Location var1, Location var2, boolean var3) {
      int var4 = Math.max(8, this.cachedRoundRewindDurationTicks);
      int var5 = 8;
      double var6 = Double.MAX_VALUE;

      for (int var8 = 8; var8 <= var4; var8++) {
         double var9 = this.calculateBallisticPeakOffset(var2.getY() - var1.getY(), var8);
         Vector var11 = this.calculateBallisticVelocity(var1, var2, var8, var3);
         double var12 = Math.hypot(var11.getX(), var11.getZ());
         double var14 = Math.max(0.0, var12 - 3.5) * 100.0;
         double var16 = Math.abs(var9 - this.cachedRoundRewindArcHeight) + var14;
         if (var16 < var6) {
            var6 = var16;
            var5 = var8;
         }
      }

      return var5;
   }

   private Vector calculateBallisticVelocity(Location var1, Location var2, int var3, boolean var4) {
      double var5 = 0.91;
      double var7 = 0.98;
      double var9 = 0.08;
      double var11;
      if (var3 <= 1) {
         var11 = 1.0;
      } else {
         double var13 = var4 ? 0.546 : 0.91;
         var11 = 1.0 + var13 * this.geometricSum(0.91, var3 - 1);
      }

      double var17 = this.geometricSum(0.98, var3);
      double var15 = 3.9199999999999964 * ((double)var3 - var17);
      return new Vector((var2.getX() - var1.getX()) / var11, (var2.getY() - var1.getY() + var15) / var17, (var2.getZ() - var1.getZ()) / var11);
   }

   private double calculateBallisticPeakOffset(double var1, int var3) {
      double var4 = 0.98;
      double var6 = 0.08;
      double var8 = this.geometricSum(0.98, var3);
      double var10 = 3.9199999999999964 * ((double)var3 - var8);
      double var12 = (var1 + var10) / var8;
      double var14 = 0.0;
      double var16 = 0.0;

      for (int var18 = 1; var18 <= var3; var18++) {
         var14 += var12;
         var16 = Math.max(var16, var14 - var1 * (double)var18 / (double)var3);
         var12 = (var12 - 0.08) * 0.98;
      }

      return var16;
   }

   private double geometricSum(double var1, int var3) {
      return (1.0 - Math.pow(var1, (double)var3)) / (1.0 - var1);
   }

   private void finishRoundRewindVelocity(Player var1, Location var2) {
      var1.teleport(var2, TeleportCause.PLUGIN);
      this.stopRoundRewindMotion(var1);
   }

   private void finishRoundRewindVelocity(Player var1) {
      this.stopRoundRewindMotion(var1);
      var1.setInvulnerable(false);
   }

   private void stopRoundRewindMotion(Player var1) {
      var1.setVelocity(new Vector(0, 0, 0));
      var1.setGravity(true);
      var1.setFlying(false);
      var1.setAllowFlight(false);
      var1.setFallDistance(0.0F);
   }

   private void spawnRoundRewindParticles(Location var1) {
      if (var1.getWorld() != null) {
         var1.getWorld().spawnParticle(Particle.PORTAL, var1, 3, 0.18, 0.25, 0.18, 0.01);
      }
   }

   private void handleFinalRound(g var1, Player var2, Player var3, Player var4) {
      int var5 = this.cachedSpectatorTime;
      this.plugin
         .getArenaListener()
         .cleanupTempRespawnSpectators(
            var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
            var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
         );
      Player var6 = var1.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
            != null
         ? Bukkit.getPlayer(
            var1.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
         )
         : null;
      if (var6 != null && var6.isOnline()) {
         var6.setInvulnerable(true);
      }

      if (var2 != null && var2.isOnline()) {
         var2.setVelocity(new Vector(0, 0, 0));
         var2.setFallDistance(0.0F);
         var2.setGameMode(GameMode.ADVENTURE);
         var2.setAllowFlight(true);
         var2.setFlying(true);
         var2.setInvulnerable(true);
         this.plugin
            .getSpectatorManager()
            .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
               var2
            );
         if (var6 != null && var6.isOnline()) {
            var6.hidePlayer(this.plugin, var2);
         }
      }

      Bukkit.getScheduler()
         .runTaskLater(
            this.plugin,
            () -> this.endMatch(
                  var1,
                  var1.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
               ),
            (long)var5 * 20L
         );
   }

   private void sendFinalMatchFeedback(g var1, UUID var2) {
      if (var1 != null
         && var2 != null
         && !var1.Óo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000intsuper()
         )
       {
         var1.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
            true
         );
         UUID var3 = var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
            var2
         );
         Player var4 = Bukkit.getPlayer(var2);
         Player var5 = var3 != null ? Bukkit.getPlayer(var3) : null;
         int var6 = var2.equals(
               var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
            )
            ? var1.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
            : var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper();
         int var7 = var2.equals(
               var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
            )
            ? var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
            : var1.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface();
         if (var4 != null && var4.isOnline()) {
            Map var8 = this.buildFinalFeedbackPlaceholders(var1, var4, var5, var6, var7);
            this.plugin
               .getMessagesManager()
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var4, "duel-win-title", var8
               );
            this.playDuelSoundNow(var4, this.cachedWinSound, "win");
         }

         if (var5 != null && var5.isOnline()) {
            Map var9 = this.buildFinalFeedbackPlaceholders(var1, var5, var4, var7, var6);
            this.plugin
               .getMessagesManager()
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var5, "duel-loss-title", var9
               );
            this.playDuelSoundNow(var5, this.cachedLoseSound, "lose");
         }
      }
   }

   private Map<String, String> buildFinalFeedbackPlaceholders(g var1, Player var2, Player var3, int var4, int var5) {
      HashMap var6 = new HashMap();
      var6.put("opponent", this.focusOpponentName(var2, var3));
      var6.put("your_wins", String.valueOf(var4));
      var6.put("opponent_wins", String.valueOf(var5));
      var6.put(
         "kit",
         var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
      );
      var6.put(
         "map",
         var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
               != null
            ? var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
               .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
            : "Unknown"
      );
      var6.put(
         "round",
         String.valueOf(
            var1.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
         )
      );
      var6.put(
         "total_rounds",
         String.valueOf(
            var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
         )
      );
      return var6;
   }

   private void playDuelSoundNow(Player var1, k var2, String var3) {
      if (var1 != null && var1.isOnline() && var2 != null) {
         try {
            var2.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1
            );
            if (this.plugin.isDebug()) {
               this.plugin.getLogger().info("[Duel Sound Debug] Played immediate " + var3 + " sound " + var2 + " for " + var1.getName());
            }
         } catch (Exception var5) {
            this.plugin
               .getLogger()
               .warning("[Duel Sound] Failed to play immediate " + var3 + " sound " + var2 + " for " + var1.getName() + ": " + var5.getMessage());
         }
      }
   }

   private void endMatch(g var1, UUID var2) {
      if (!var1.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
         )
       {
         if (var1.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
            != -1) {
            Bukkit.getScheduler()
               .cancelTask(
                  var1.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
               );
            var1.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               -1
            );
         }

         this.lastEngagementTimes.remove(var1);
         this.revealedInactiveMatches.remove(var1);
         this.duelScoreActionBarManager
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
               var1
            );
         if (this.plugin.getPillarsOfFortuneManager() != null) {
            this.plugin
               .getPillarsOfFortuneManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1
               );
         }

         if (this.plugin.getLavaRaiseManager() != null) {
            this.plugin.getLavaRaiseManager().cleanupDuel(var1);
         }

         var1.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if(
            true
         );
         this.recordKitMatchStat(var1);
         this.plugin
            .getArenaListener()
            .blockEnderPearlTeleportAfterMatch(
               var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
            );
         this.plugin
            .getArenaListener()
            .blockEnderPearlTeleportAfterMatch(
               var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
            );
         if (this.plugin.getArenaElevatorManager() != null) {
            this.plugin
               .getArenaElevatorManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1
               );
         }

         if (this.plugin.getBallisticEntryManager() != null) {
            this.plugin
               .getBallisticEntryManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1
               );
         }

         if (this.plugin.isDebug()) {
            String var3 = var2 != null ? (Bukkit.getPlayer(var2) != null ? Bukkit.getPlayer(var2).getName() : var2.toString()) : "draw";
            this.plugin
               .getLogger()
               .info(
                  "[Duel Debug] endMatch: winner="
                     + var3
                     + " kit="
                     + var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                     + " arena="
                     + (
                        var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                              != null
                           ? var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                              .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                           : "null"
                     )
                     + " round="
                     + var1.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
                     + "/"
                     + var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                     + " ranked="
                     + var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void()
               );
         }

         this.matchFoundEffects
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var1
            );
         this.plugin
            .getArenaListener()
            .cleanupTempRespawnSpectators(
               var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
               var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
            );
         this.plugin
            .getTntTagManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               List.of(
                  var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
                  var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
               )
            );
         boolean var24 = this.isOnCrossServerArena();
         Object var4 = var24 ? new HashMap() : Collections.emptyMap();
         Player var5 = Bukkit.getPlayer(
            var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
         );
         Player var6 = Bukkit.getPlayer(
            var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
         );
         Player var7 = var2 != null ? Bukkit.getPlayer(var2) : null;
         if (var2 != null) {
            UUID var8 = var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
               var2
            );
            this.statsManager
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                  var2
               );
            this.statsManager
               .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
                  var2,
                  var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
               );
            if (var8 != null) {
               this.statsManager
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var8
                  );
               this.statsManager
                  .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
                     var8,
                     var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                  );
               Player var9 = Bukkit.getPlayer(var2);
               Player var10 = Bukkit.getPlayer(var8);
               if (var9 != null && var10 != null && this.plugin.getMatchHistoryManager() != null) {
                  this.plugin
                     .getMatchHistoryManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var2,
                        var9.getName(),
                        var8,
                        var10.getName(),
                        var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                        var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                              != null
                           ? var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                              .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                           : "Unknown",
                        var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void()
                     );
                  if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().getMatchHistorySync() != null) {
                     this.plugin
                        .getLimboManager()
                        .getMatchHistorySync()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var2, var8
                        );
                  }
               }
            }

            if (var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void()
               && var8 != null) {
               int var26 = this.plugin.getRankedManager().getGlobalElo(var2);
               int var28 = this.plugin.getRankedManager().getGlobalElo(var8);
               RankedManager._e var11 = this.plugin
                  .getRankedManager()
                  .calculateKitRatingUpdate(
                     var2,
                     var8,
                     var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                  );
               int var12 = var11.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                  .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class();
               int var13 = var11.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                  .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class();
               this.statsManager
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var2,
                     var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                     var12
                  );
               if (this.plugin.getKitsRankManager() != null) {
                  this.plugin
                     .getKitsRankManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var2,
                        var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                        var12
                     );
               }

               this.statsManager
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var8,
                     var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                     var13
                  );
               if (this.plugin.getKitsRankManager() != null) {
                  this.plugin
                     .getKitsRankManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var8,
                        var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                        var13
                     );
               }

               RankedManager._e var14 = this.plugin.getRankedManager().applyRankedResult(var2, var8, var26, var28, false);
               int var15 = var11.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                  .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class();
               int var16 = var11.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                  .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class();
               Player var17 = Bukkit.getPlayer(var2);
               Player var18 = Bukkit.getPlayer(var8);
               if (this.plugin.getEloHistoryManager() != null) {
                  long var19 = System.currentTimeMillis();
                  String var21 = var17 != null ? var17.getName() : "Unknown";
                  String var22 = var18 != null ? var18.getName() : "Unknown";
                  this.plugin
                     .getEloHistoryManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var2,
                        var8,
                        var22,
                        var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                        "global",
                        var14.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                           .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(),
                        var14.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(),
                        var14.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                           .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(),
                        var19,
                        null
                     );
                  this.plugin
                     .getEloHistoryManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var8,
                        var2,
                        var21,
                        var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                        "global",
                        var14.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                           .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(),
                        var14.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(),
                        var14.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                           .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(),
                        var19,
                        null
                     );
                  this.plugin
                     .getEloHistoryManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var2,
                        var8,
                        var22,
                        var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                        "kit",
                        var11.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                           .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(),
                        var11.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(),
                        var11.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                           .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(),
                        var19,
                        null
                     );
                  this.plugin
                     .getEloHistoryManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var8,
                        var2,
                        var21,
                        var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                        "kit",
                        var11.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                           .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(),
                        var11.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(),
                        var11.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                           .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(),
                        var19,
                        null
                     );
               }

               if (var17 != null) {
                  int var39 = var11.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
                  String var20 = this.plugin.getKitsRankManager() != null
                     ? this.plugin
                        .getKitsRankManager()
                        .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                           var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                           var39
                        )
                     : this.plugin.getRankedManager().getTierNameByPoints(var39);
                  String var42 = this.plugin.getKitsRankManager() != null
                     ? this.plugin
                        .getKitsRankManager()
                        .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                           var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                           var11.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                              .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                        )
                     : this.plugin
                        .getRankedManager()
                        .getTierNameByPoints(
                           var11.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                              .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                        );
                  HashMap var44 = new HashMap();
                  var44.put("elo_change", String.valueOf(var15));
                  var44.put("points", String.valueOf(var39));
                  var44.put("tier", var20);
                  this.plugin.getRankedManager().setLastRankedResult(var2, var15, var39, var20);
                  if (!var24) {
                     this.plugin
                        .getRankedManager()
                        .notifyTierChange(
                           var17,
                           var11.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                              .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(),
                           var39,
                           var42,
                           var20
                        );
                  }

                  if (var24) {
                     Map var23 = var4.computeIfAbsent(var2, var0 -> new HashMap());
                     this.addPostMatchMessage(var23, "message", "ranked-elo-win", var44);
                     this.addPostMatchMessage(
                        var23,
                        "message",
                        "ranked-stats",
                        this.plugin
                           .getRankedManager()
                           .buildRankedStatsPlaceholders(
                              var17,
                              var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                           )
                     );
                  } else {
                     this.plugin
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var17, "ranked-elo-win", var44
                        );
                     this.plugin
                        .getRankedManager()
                        .sendRankedStats(
                           var17,
                           var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                        );
                  }
               }

               if (var18 != null) {
                  int var40 = var11.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
                  String var41 = this.plugin.getKitsRankManager() != null
                     ? this.plugin
                        .getKitsRankManager()
                        .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                           var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                           var40
                        )
                     : this.plugin.getRankedManager().getTierNameByPoints(var40);
                  String var43 = this.plugin.getKitsRankManager() != null
                     ? this.plugin
                        .getKitsRankManager()
                        .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                           var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                           var11.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                              .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                        )
                     : this.plugin
                        .getRankedManager()
                        .getTierNameByPoints(
                           var11.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                              .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                        );
                  HashMap var45 = new HashMap();
                  var45.put("elo_change", String.valueOf(Math.abs(var16)));
                  var45.put("points", String.valueOf(var40));
                  var45.put("tier", var41);
                  this.plugin.getRankedManager().setLastRankedResult(var8, Math.abs(var16), var40, var41);
                  if (!var24) {
                     this.plugin
                        .getRankedManager()
                        .notifyTierChange(
                           var18,
                           var11.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                              .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(),
                           var40,
                           var43,
                           var41
                        );
                  }

                  if (var24) {
                     Map var46 = var4.computeIfAbsent(var8, var0 -> new HashMap());
                     this.addPostMatchMessage(var46, "message", "ranked-elo-loss", var45);
                     this.addPostMatchMessage(
                        var46,
                        "message",
                        "ranked-stats",
                        this.plugin
                           .getRankedManager()
                           .buildRankedStatsPlaceholders(
                              var18,
                              var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                           )
                     );
                  } else {
                     this.plugin
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var18, "ranked-elo-loss", var45
                        );
                     this.plugin
                        .getRankedManager()
                        .sendRankedStats(
                           var18,
                           var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                        );
                  }
               }
            }
         }

         if (var7 != null) {
            UUID var25 = var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
               var2
            );
            Player var27 = var25 != null ? Bukkit.getPlayer(var25) : null;
            boolean var29 = var25 != null
               && var25.equals(
                  var1.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper()
               );
            int var30 = var2.equals(
                  var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
               )
               ? var1.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
               : var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper();
            int var31 = var2.equals(
                  var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
               )
               ? var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
               : var1.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface();
            HashMap var32 = new HashMap();
            String var33 = var27 != null ? var27.getName() : "Unknown";
            var32.put("opponent", this.focusOpponentName(var7, var27));
            var32.put("your_wins", String.valueOf(var30));
            var32.put("opponent_wins", String.valueOf(var31));
            var32.put(
               "kit",
               var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
            );
            var32.put(
               "map",
               var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                     != null
                  ? var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                     .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                  : "Unknown"
            );
            var32.put(
               "round",
               String.valueOf(
                  var1.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
               )
            );
            var32.put(
               "total_rounds",
               String.valueOf(
                  var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
               )
            );
            var32.put("inv_see", this.buildInvSeeComponent(var25, var33));
            if (var24) {
               Map var34 = var4.computeIfAbsent(var2, var0 -> new HashMap());
               if (!var1.Óo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000intsuper()
                  )
                {
                  this.addPostMatchMessage(var34, "title", "duel-win-title", var32);
               }

               this.addPostMatchMessage(var34, "message", "match-ended-winner", var32);
            } else {
               if (!var1.Óo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000intsuper()
                  )
                {
                  this.plugin
                     .getMessagesManager()
                     .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                        var7, "duel-win-title", var32
                     );
               }

               this.plugin
                  .getMessagesManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var7, "match-ended-winner", var32
                  );
            }

            if (!var1.Óo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000intsuper()
               )
             {
               this.playDuelSoundNow(var7, this.cachedWinSound, "win");
            }

            if (var27 != null) {
               HashMap var35 = new HashMap();
               var35.put("opponent", this.focusOpponentName(var27, var7));
               var35.put("your_wins", String.valueOf(var31));
               var35.put("opponent_wins", String.valueOf(var30));
               var35.put(
                  "kit",
                  var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
               );
               var35.put(
                  "map",
                  var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                        != null
                     ? var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                        .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                     : "Unknown"
               );
               var35.put(
                  "round",
                  String.valueOf(
                     var1.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
                  )
               );
               var35.put(
                  "total_rounds",
                  String.valueOf(
                     var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                  )
               );
               var35.put("inv_see", this.buildInvSeeComponent(var2, var7.getName()));
               if (var24) {
                  Map var37 = var4.computeIfAbsent(var25, var0 -> new HashMap());
                  if (!var1.Óo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000intsuper()
                     && !var29) {
                     this.addPostMatchMessage(var37, "title", "duel-loss-title", var35);
                  }

                  this.addPostMatchMessage(var37, "message", "match-ended-loser", var35);
               } else {
                  if (!var1.Óo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000intsuper()
                     && !var29) {
                     this.plugin
                        .getMessagesManager()
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                           var27, "duel-loss-title", var35
                        );
                  }

                  this.plugin
                     .getMessagesManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var27, "match-ended-loser", var35
                     );
               }

               if (!var1.Óo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000intsuper()
                  && !var29) {
                  this.playDuelSoundNow(var27, this.cachedLoseSound, "lose");
               }
            }

            boolean var36 = this.plugin
               .getPlayerSettingsManager()
               .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                  var7
               )
               .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class();
            if (this.plugin.isDebug()) {
               this.plugin
                  .getLogger()
                  .info(
                     "[Duel Debug] Auto-GG winner="
                        + var7.getName()
                        + " enabled="
                        + var36
                        + " cachedMsgs="
                        + (this.cachedAutoGGMessages != null ? this.cachedAutoGGMessages.size() : 0)
                  );
            }

            if (var36) {
               this.sendAutoGGMessage(var7);
            }

            if (var27 != null && var27.isOnline()) {
               boolean var38 = this.plugin
                  .getPlayerSettingsManager()
                  .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                     var27
                  )
                  .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class();
               if (this.plugin.isDebug()) {
                  this.plugin.getLogger().info("[Duel Debug] Auto-GG loser=" + var27.getName() + " enabled=" + var38);
               }

               if (var38) {
                  this.sendAutoGGMessage(var27);
               }
            }

            if (this.plugin.getEconomyManager() != null
               && this.plugin
                  .getEconomyManager()
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
               )
             {
               this.plugin
                  .getEconomyManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var7
                  );
               if (var27 != null && var27.isOnline()) {
                  this.plugin
                     .getEconomyManager()
                     .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                        var27
                     );
               }
            }

            if (this.plugin.getClanManager() != null) {
               this.plugin
                  .getClanManager()
                  .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
                     var2
                  );
            }
         }

         if (var5 != null) {
            this.plugin
               .getSpectatorManager()
               .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                  var5
               );
            this.plugin
               .getSpectatorManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var5.getUniqueId()
               );
            if (this.plugin
               .getSpectatorManager()
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                  var5.getUniqueId()
               )) {
               this.plugin
                  .getSpectatorManager()
                  .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                     var5
                  );
            }
         }

         if (var6 != null) {
            this.plugin
               .getSpectatorManager()
               .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                  var6
               );
            this.plugin
               .getSpectatorManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var6.getUniqueId()
               );
            if (this.plugin
               .getSpectatorManager()
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                  var6.getUniqueId()
               )) {
               this.plugin
                  .getSpectatorManager()
                  .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                     var6
                  );
            }
         }

         this.plugin
            .getSpectatorManager()
            .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
               var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
            );
         this.plugin
            .getSpectatorManager()
            .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
               var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
            );
         if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().getActiveMatchRegistry() != null) {
            this.plugin
               .getLimboManager()
               .getActiveMatchRegistry()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper(),
                  List.of(
                     var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
                     var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
                  )
               );
         }

         this.activeMatches
            .remove(
               var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
            );
         this.activeMatches
            .remove(
               var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
            );
         this.plugin
            .getKitRulesListener()
            .removeActivePlayer(
               var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
            );
         this.plugin
            .getKitRulesListener()
            .removeActivePlayer(
               var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
            );
         if (var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
               != null
            && var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
               .startsWith("customkit")
            && this.plugin.getCustomKitAPI() != null) {
            this.plugin
               .getCustomKitAPI()
               .removeKit(
                  var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
               );
            this.plugin
               .getCustomKitAPI()
               .removeKit(
                  var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
               );
         }

         if (this.plugin.getHPIndicatorManager() != null) {
            this.plugin
               .getHPIndicatorManager()
               .stopForMatch(
                  var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
                  var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
               );
         }

         if (this.plugin.getTablistManager() != null) {
            this.plugin
               .getTablistManager()
               .removeAllTeamGlow(
                  List.of(
                     var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
                     var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
                  )
               );
            this.plugin.getTablistManager().restoreFocusPair(var5, var6);
         }

         this.plugin
            .getQueueManager()
            .removeFightingPlayer(
               var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
            );
         this.plugin
            .getQueueManager()
            .removeFightingPlayer(
               var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
            );
         this.teleportPlayersToLobby(
            var1,
            (Map<UUID, Map<String, String>>)var4,
            () -> {
               this.arenaManager
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                  );
               this.arenaManager
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
                  );
               org.lime.swiftCore.arena.d var2x = var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper();
               if (var2x != null) {
                  if (var2x.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
                     == ArenaType.BUILD) {
                     if (var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                        > 1) {
                        this.plugin
                           .getArenaResetManager()
                           .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                              var2x
                           );
                     } else {
                        this.plugin
                           .getArenaResetManager()
                           .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                              var2x
                           );
                     }
                  } else {
                     this.plugin
                        .getArenaResetManager()
                        .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
                           var2x
                        );
                     if (var2x.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
                        && this.plugin.getDynamicArenaManager() != null) {
                        this.plugin
                           .getDynamicArenaManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var2x
                           );
                     } else {
                        this.arenaManager
                           .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
                              var2x
                           );
                     }
                  }
               }
            }
         );
         if (var5 != null) {
            if (!this.plugin
                  .getTournamentManager()
                  .ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if(
                     var5.getUniqueId()
                  )
               && !this.plugin
                  .getTournamentManager()
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var5.getUniqueId()
                  )
               && !this.plugin
                  .getEventManager()
                  .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                     var5.getUniqueId()
                  )) {
               this.plugin.getScoreboardManager().setState(var5, ScoreboardState.DEFAULT);
            }

            this.plugin.getScoreboardManager().setPlaceholder(var5, "in_fight", "false");
            this.plugin.getKitRulesListener().clearBoxingHits(var5.getUniqueId());
            this.plugin.getKitRulesListener().clearCooldowns(var5.getUniqueId());
            this.plugin
               .getParkourManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var5.getUniqueId()
               );
         }

         if (var6 != null) {
            if (!this.plugin
                  .getTournamentManager()
                  .ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if(
                     var6.getUniqueId()
                  )
               && !this.plugin
                  .getTournamentManager()
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var6.getUniqueId()
                  )
               && !this.plugin
                  .getEventManager()
                  .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                     var6.getUniqueId()
                  )) {
               this.plugin.getScoreboardManager().setState(var6, ScoreboardState.DEFAULT);
            }

            this.plugin.getScoreboardManager().setPlaceholder(var6, "in_fight", "false");
            this.plugin.getKitRulesListener().clearBoxingHits(var6.getUniqueId());
            this.plugin.getKitRulesListener().clearCooldowns(var6.getUniqueId());
            this.plugin
               .getParkourManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var6.getUniqueId()
               );
         }

         this.plugin.getKitRulesListener().clearDecayingBlocks();
      }
   }

   private void addPostMatchMessage(Map<String, String> var1, String var2, String var3, Map<String, String> var4) {
      int var5 = Integer.parseInt(var1.getOrDefault("post_message_count", "0"));
      String var6 = "post_message_" + var5;
      var1.put(var6 + "_type", var2);
      var1.put(var6 + "_key", var3);
      if (var4 != null) {
         for (Entry var8 : var4.entrySet()) {
            var1.put(var6 + "_placeholder_" + (String)var8.getKey(), var8.getValue() != null ? (String)var8.getValue() : "");
         }
      }

      var1.put("post_message_count", String.valueOf(var5 + 1));
   }

   public void captureInventorySnapshotBeforeDeath(Player var1) {
      if (this.invSeeEnabled) {
         this.captureInventorySnapshot(var1);
      }
   }

   private void captureInventorySnapshot(Player var1) {
      if (var1 != null && var1.isOnline()) {
         UUID var2 = var1.getUniqueId();
         ItemStack[] var3 = var1.getInventory().getStorageContents();
         ItemStack[] var4 = var1.getInventory().getArmorContents();
         ItemStack[] var5 = new ItemStack[var3.length];

         for (int var6 = 0; var6 < var3.length; var6++) {
            var5[var6] = var3[var6] != null ? var3[var6].clone() : null;
         }

         ItemStack[] var8 = new ItemStack[var4.length];

         for (int var7 = 0; var7 < var4.length; var7++) {
            var8[var7] = var4[var7] != null ? var4[var7].clone() : null;
         }

         this.endMatchInventories.put(var2, var5);
         this.endMatchArmor.put(var2, var8);
         if (this.plugin.getLimboManager() != null
            && this.plugin.getLimboManager().isCrossServer()
            && this.plugin.getLimboManager().getPostMatchInventoryStore() != null) {
            String var9 = var1.getName();
            Bukkit.getScheduler()
               .runTaskAsynchronously(
                  this.plugin,
                  () -> this.plugin
                        .getLimboManager()
                        .getPostMatchInventoryStore()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var2, var9, var5, var8
                        )
               );
         }
      }
   }

   private boolean hasUsableInventorySnapshot(Player var1) {
      return var1 != null && this.endMatchInventories.containsKey(var1.getUniqueId());
   }

   private boolean hasAnyInventoryItem(Player var1) {
      if (var1 != null && var1.isOnline()) {
         for (ItemStack var5 : var1.getInventory().getStorageContents()) {
            if (var5 != null && !var5.getType().isAir() && var5.getAmount() > 0) {
               return true;
            }
         }

         for (ItemStack var9 : var1.getInventory().getArmorContents()) {
            if (var9 != null && !var9.getType().isAir() && var9.getAmount() > 0) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private String buildInvSeeComponent(UUID var1, String var2) {
      if (this.invSeeEnabled && var1 != null) {
         String var3 = this.invSeeHoverText.replace("%opponent%", var2);
         String var4 = this.invSeeClickText;
         return "<click:run_command:/swiftcore invsee "
            + var1
            + "><hover:show_text:\""
            + this.escapeMiniMessageArgument(this.convertHex(var3))
            + "\">"
            + this.convertHex(var4)
            + "</hover></click>";
      } else {
         return "";
      }
   }

   private String escapeMiniMessageArgument(String var1) {
      return var1 != null && !var1.isEmpty() ? var1.replace("\\", "\\\\").replace("\"", "\\\"") : "";
   }

   private String convertHex(String var1) {
      Matcher var2 = HEX_PATTERN.matcher(var1);
      StringBuilder var3 = new StringBuilder();

      while (var2.find()) {
         var2.appendReplacement(var3, "<color:#" + var2.group(1) + ">");
      }

      var2.appendTail(var3);
      return var3.toString().replace("&l", "<bold>").replace("&n", "<underlined>").replace("&o", "<italic>").replace("&r", "<reset>");
   }

   public void openInvSeeGUI(Player var1, UUID var2) {
      if (this.invSeeEnabled) {
         ItemStack[] var3 = this.endMatchInventories.get(var2);
         if (var3 == null) {
            if (this.plugin.getLimboManager() != null
               && this.plugin.getLimboManager().isCrossServer()
               && this.plugin.getLimboManager().getPostMatchInventoryStore() != null) {
               Bukkit.getScheduler()
                  .runTaskAsynchronously(
                     this.plugin,
                     () -> {
                        String var3x = this.plugin
                           .getLimboManager()
                           .getPostMatchInventoryStore()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var2
                           );
                        Bukkit.getScheduler()
                           .runTask(
                              this.plugin,
                              () -> {
                                 if (var1.isOnline()) {
                                    org.lime.swiftCore.v.e.b._b var4 = this.plugin
                                       .getLimboManager()
                                       .getPostMatchInventoryStore()
                                       .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                          var3x
                                       );
                                    if (var4 == null) {
                                       var1.sendMessage(Component.text("No inventory data available for this player.").color(NamedTextColor.RED));
                                    } else {
                                       this.endMatchInventories
                                          .put(
                                             var2,
                                             var4.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                                          );
                                       this.endMatchArmor
                                          .put(
                                             var2,
                                             var4.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                                          );
                                       this.openInvSeeGUI(
                                          var1,
                                          var2,
                                          var4.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                                       );
                                    }
                                 }
                              }
                           );
                     }
                  );
            } else {
               var1.sendMessage(Component.text("No inventory data available for this player.").color(NamedTextColor.RED));
            }
         } else {
            this.openInvSeeGUI(var1, var2, null);
         }
      }
   }

   private void openInvSeeGUI(Player var1, UUID var2, String var3) {
      ItemStack[] var4 = this.endMatchInventories.get(var2);
      ItemStack[] var5 = this.endMatchArmor.get(var2);
      if (var4 != null) {
         String var6 = Bukkit.getOfflinePlayer(var2).getName();
         if ((var6 == null || var6.isBlank()) && var3 != null) {
            var6 = var3;
         }

         if (var6 == null) {
            var6 = "Unknown";
         }

         String var7 = this.invSeeGuiTitle.replace("%opponent%", var6);
         TextComponent var8 = LegacyComponentSerializer.legacyAmpersand().deserialize(var7);
         Inventory var9 = Bukkit.createInventory(null, 54, var8);

         for (int var10 = 0; var10 < Math.min(var4.length, 36); var10++) {
            if (var4[var10] != null) {
               var9.setItem(var10, var4[var10].clone());
            }
         }

         if (var5 != null) {
            if (var5.length > 3 && var5[3] != null) {
               var9.setItem(36, var5[3].clone());
            }

            if (var5.length > 2 && var5[2] != null) {
               var9.setItem(37, var5[2].clone());
            }

            if (var5.length > 1 && var5[1] != null) {
               var9.setItem(38, var5[1].clone());
            }

            if (var5.length > 0 && var5[0] != null) {
               var9.setItem(39, var5[0].clone());
            }
         }

         ItemStack var13 = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
         ItemMeta var11 = var13.getItemMeta();
         var11.displayName(Component.text(" "));
         var13.setItemMeta(var11);

         for (int var12 = 40; var12 < 45; var12++) {
            var9.setItem(var12, var13);
         }

         j.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1.getUniqueId(),
            j._b.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new,
            null
         );
         var1.openInventory(var9);
      }
   }

   public boolean isInvSeeEnabled() {
      return this.invSeeEnabled;
   }

   public Map<UUID, ItemStack[]> getEndMatchInventories() {
      return this.endMatchInventories;
   }

   public Map<UUID, ItemStack[]> getEndMatchArmor() {
      return this.endMatchArmor;
   }

   private void recordKitMatchStat(g var1) {
      if (this.plugin.getKitStatsManager() != null
         && var1 != null
         && var1.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
         )
       {
         org.lime.swiftCore.ab.b._c var2;
         if (var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void()
            )
          {
            var2 = org.lime.swiftCore.ab.b._c.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null;
         } else if (var1.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
            )
          {
            var2 = org.lime.swiftCore.ab.b._c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
         } else {
            var2 = org.lime.swiftCore.ab.b._c.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
         }

         this.plugin
            .getKitStatsManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
               this.getKitDisplayName(
                  var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                  var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
                  var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
               ),
               var2
            );
      }
   }

   private void transferPlayersToSourceLobbyAfterMatch(g var1, Player var2, Player var3, String var4, boolean var5, Map<UUID, Map<String, String>> var6) {
      if (this.plugin.isDebug()) {
         this.plugin
            .getLogger()
            .info(
               "[CrossServer Debug] Match "
                  + var1.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper()
                  + " ended on arena server. Skipping local lobby logic and transferring players directly back to their source lobby."
            );
      }

      this.prepareCrossServerReturn(var1, var2, var4, var5, var6);
      this.prepareCrossServerReturn(var1, var3, var4, var5, var6);
   }

   private void prepareCrossServerReturn(g var1, Player var2, String var3, boolean var4, Map<UUID, Map<String, String>> var5) {
      if (var2 != null && var2.isOnline()) {
         UUID var6 = var2.getUniqueId();
         boolean var7 = this.plugin
            .getTournamentManager()
            .ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if(
               var6
            );
         boolean var8 = this.plugin
            .getTournamentManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var6
            );
         boolean var9 = this.plugin
            .getEventManager()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var6
            );
         this.plugin.getArenaListener().cancelThrownPearl(var6);
         var2.clearTitle();
         var2.getInventory().clear();
         this.plugin
            .getKitManager()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var2
            );
         this.resetDuelMaxHealth(var2);
         var2.setFoodLevel(20);
         var2.setSaturation(5.0F);
         var2.setGameMode(GameMode.SURVIVAL);
         var2.setAllowFlight(true);
         var2.setFlying(true);
         var2.setInvulnerable(true);

         for (PotionEffect var11 : var2.getActivePotionEffects()) {
            var2.removePotionEffect(var11.getType());
         }

         var2.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, false, false, false));
         boolean var14 = var4
            && !var8
            && !var9
            && !var7
            && this.plugin
               .getPlayerSettingsManager()
               .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                  var2
               )
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
         if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
            HashMap var15 = new HashMap();
            var15.put("post_match", "true");
            var15.put("post_match_created_at", String.valueOf(System.currentTimeMillis()));
            Map var12 = var5 != null ? (Map)var5.get(var6) : null;
            if (var12 != null && !var12.isEmpty()) {
               var15.putAll(var12);
            }

            if (var14 && var3 != null && !var3.isBlank()) {
               var15.put("auto_requeue_kit", var3);
            }

            UUID var13 = var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
               var6
            );
            if (!var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void()
               && !var7
               && !var8
               && !var9
               && var13 != null
               && var3 != null
               && !var3.isBlank()) {
               var15.put("rematch_opponent", var13.toString());
               var15.put("rematch_kit", var3);
            }

            this.plugin
               .getLimboManager()
               .getPlayerDataSync()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var6, var15
               );
            this.plugin
               .getLimboManager()
               .getPlayerDataSync()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var2.getName(), var15
               );
            if (this.plugin.isDebug()) {
               this.plugin.getLogger().info("[CrossServer Debug] Saved post-match return state for " + var2.getName() + " autoRequeue=" + var14 + ".");
            }
         }

         if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().getTransferManager() != null) {
            this.plugin
               .getLimboManager()
               .getTransferManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var2
               );
         }
      }
   }

   private void teleportPlayersToLobby(g var1, Runnable var2) {
      this.teleportPlayersToLobby(var1, Collections.emptyMap(), var2);
   }

   private void teleportPlayersToLobby(g var1, Map<UUID, Map<String, String>> var2, Runnable var3) {
      Player var4 = Bukkit.getPlayer(
         var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
      );
      Player var5 = Bukkit.getPlayer(
         var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
      );
      String var6 = var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
      boolean var7 = var1.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class();
      if (this.isOnCrossServerArena()) {
         this.transferPlayersToSourceLobbyAfterMatch(var1, var4, var5, var6, var7, var2);
         if (var3 != null) {
            Bukkit.getScheduler().runTask(this.plugin, var3);
         }
      } else {
         Map var8 = Map.ofEntries(
            Map.entry("in_fight", "false"),
            Map.entry("kit", ""),
            Map.entry("opponent", ""),
            Map.entry("opponent_ping", ""),
            Map.entry("round", ""),
            Map.entry("current_round", ""),
            Map.entry("total_rounds", ""),
            Map.entry("own_wins", ""),
            Map.entry("your_wins", ""),
            Map.entry("opponent_wins", ""),
            Map.entry("team_icon", ""),
            Map.entry("team_color", ""),
            Map.entry("round_winner", "")
         );
         Bukkit.getScheduler()
            .runTask(
               this.plugin,
               () -> {
                  ArrayList var8x = new ArrayList();
                  if (var4 != null && var4.isOnline() && var5 != null && var5.isOnline()) {
                     var4.showPlayer(this.plugin, var5);
                     var5.showPlayer(this.plugin, var4);
                  }

                  if (var4 != null && var4.isOnline()) {
                     this.plugin
                        .getPlayerSettingsManager()
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                           var4
                        );
                  }

                  if (var5 != null && var5.isOnline()) {
                     this.plugin
                        .getPlayerSettingsManager()
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                           var5
                        );
                  }

                  if (var4 != null && var4.isOnline()) {
                     this.plugin.getArenaListener().cancelThrownPearl(var4.getUniqueId());
                     var4.getInventory().clear();
                     this.plugin
                        .getKitManager()
                        .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                           var4
                        );
                     this.resetDuelMaxHealth(var4);
                     var4.setFoodLevel(20);
                     var4.setSaturation(5.0F);
                     var4.setGameMode(GameMode.SURVIVAL);
                     var4.setAllowFlight(false);
                     var4.setFlying(false);
                     var4.setInvulnerable(false);
                     boolean var9 = this.plugin
                        .getTournamentManager()
                        .ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if(
                           var4.getUniqueId()
                        );
                     boolean var10 = this.plugin
                        .getTournamentManager()
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                           var4.getUniqueId()
                        );
                     boolean var11 = this.plugin
                        .getEventManager()
                        .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                           var4.getUniqueId()
                        );
                     if (!var9) {
                        var8x.add(this.sendPlayerToLobby(var4));
                     }

                     if (var9) {
                        var4.setAllowFlight(true);
                        var4.setFlying(true);
                        var4.setInvulnerable(true);
                        this.plugin.getSpawnItemsManager().giveSpawnItems(var4, "spectating", false, false);
                        this.plugin.getScoreboardManager().setState(var4, ScoreboardState.SPECTATING);
                     } else if (var10) {
                        this.plugin.getSpawnItemsManager().giveSpawnItems(var4, "tournament", false, false);
                        this.plugin.getScoreboardManager().setState(var4, ScoreboardState.TOURNAMENT_WAITING);
                     } else if (var11) {
                        this.plugin.getSpawnItemsManager().giveSpawnItems(var4, "event", false, false);
                        this.plugin.getScoreboardManager().setState(var4, ScoreboardState.EVENT_WAITING);
                     } else {
                        this.plugin.getSpawnItemsManager().giveSpawnItems(var4, "default", false, false);
                        this.plugin.getScoreboardManager().setState(var4, ScoreboardState.DEFAULT);
                     }

                     this.plugin.getScoreboardManager().setPlaceholders(var4, var8);
                     this.plugin.getScoreboardManager().updateScoreboard(var4);
                     if (this.plugin.getTablistManager() != null) {
                        this.plugin.getTablistManager().resetContext(var4);
                     }

                     boolean var12 = this.plugin
                        .getPlayerSettingsManager()
                        .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                           var4
                        )
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
                     if (this.plugin.isDebug()) {
                        this.plugin
                           .getLogger()
                           .info(
                              "[Duel Debug] Auto-Requeue p1="
                                 + var4.getName()
                                 + " wasQueueMatch="
                                 + var7
                                 + " inTournament="
                                 + var10
                                 + " inEvent="
                                 + var11
                                 + " isEliminatedSpec="
                                 + var9
                                 + " enabled="
                                 + var12
                           );
                     }

                     if (!var10 && !var11 && !var9 && var7 && var12) {
                        Bukkit.getScheduler()
                           .runTaskLater(
                              this.plugin,
                              () -> {
                                 if (!var4.isOnline()) {
                                    if (this.plugin.isDebug()) {
                                       this.plugin.getLogger().info("[Duel Debug] Auto-Requeue p1 aborted (offline)");
                                    }
                                 } else {
                                    boolean var3xx = this.isInMatch(var4.getUniqueId());
                                    boolean var4xx = this.plugin.getQueueManager().isInAnyQueue(var4.getUniqueId());
                                    if (!var3xx && !var4xx) {
                                       if (this.plugin.isDebug()) {
                                          this.plugin.getLogger().info("[Duel Debug] Auto-Requeue p1 enqueue kit=" + var6);
                                       }

                                       if (this.plugin.getQueueManager().addToQueue(var4.getUniqueId(), var6)) {
                                          this.plugin.getScoreboardManager().setState(var4, ScoreboardState.QUEUE);
                                          this.plugin
                                             .getScoreboardManager()
                                             .setPlaceholders(
                                                var4,
                                                Map.of(
                                                   "in_queue",
                                                   "true",
                                                   "in_queue_kitname",
                                                   this.plugin
                                                      .getDataManager()
                                                      .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                                         var6
                                                      )
                                                )
                                             );
                                          this.plugin.getSpawnItemsManager().giveSpawnItems(var4, "queue", false, false);
                                          this.plugin
                                             .getMessagesManager()
                                             .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                                var4,
                                                "auto-requeued",
                                                Map.of(
                                                   "kit",
                                                   this.plugin
                                                      .getDataManager()
                                                      .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                                         var6
                                                      )
                                                )
                                             );
                                       }
                                    } else {
                                       if (this.plugin.isDebug()) {
                                          this.plugin.getLogger().info("[Duel Debug] Auto-Requeue p1 skipped inMatch=" + var3xx + " inQueue=" + var4xx);
                                       }
                                    }
                                 }
                              },
                              40L
                           );
                     } else if (!var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void()
                        )
                      {
                        this.setRematchData(
                           var4.getUniqueId(),
                           var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper(),
                           var6
                        );
                        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                           if (var4.isOnline()) {
                              this.giveRematchItem(var4);
                           }
                        }, 5L);
                     } else {
                        this.clearRematchData(var4.getUniqueId());
                     }
                  }

                  if (var5 != null && var5.isOnline()) {
                     this.plugin.getArenaListener().cancelThrownPearl(var5.getUniqueId());
                     var5.getInventory().clear();
                     this.plugin
                        .getKitManager()
                        .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                           var5
                        );
                     this.resetDuelMaxHealth(var5);
                     var5.setFoodLevel(20);
                     var5.setSaturation(5.0F);
                     var5.setGameMode(GameMode.SURVIVAL);
                     var5.setAllowFlight(false);
                     var5.setFlying(false);
                     var5.setInvulnerable(false);
                     boolean var13 = this.plugin
                        .getTournamentManager()
                        .ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if(
                           var5.getUniqueId()
                        );
                     boolean var14 = this.plugin
                        .getTournamentManager()
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                           var5.getUniqueId()
                        );
                     boolean var15 = this.plugin
                        .getEventManager()
                        .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                           var5.getUniqueId()
                        );
                     if (!var13) {
                        var8x.add(this.sendPlayerToLobby(var5));
                     }

                     if (var13) {
                        var5.setAllowFlight(true);
                        var5.setFlying(true);
                        var5.setInvulnerable(true);
                        this.plugin.getSpawnItemsManager().giveSpawnItems(var5, "spectating", false, false);
                        this.plugin.getScoreboardManager().setState(var5, ScoreboardState.SPECTATING);
                     } else if (var14) {
                        this.plugin.getSpawnItemsManager().giveSpawnItems(var5, "tournament", false, false);
                        this.plugin.getScoreboardManager().setState(var5, ScoreboardState.TOURNAMENT_WAITING);
                     } else if (var15) {
                        this.plugin.getSpawnItemsManager().giveSpawnItems(var5, "event", false, false);
                        this.plugin.getScoreboardManager().setState(var5, ScoreboardState.EVENT_WAITING);
                     } else {
                        this.plugin.getSpawnItemsManager().giveSpawnItems(var5, "default", false, false);
                        this.plugin.getScoreboardManager().setState(var5, ScoreboardState.DEFAULT);
                     }

                     this.plugin.getScoreboardManager().setPlaceholders(var5, var8);
                     this.plugin.getScoreboardManager().updateScoreboard(var5);
                     if (this.plugin.getTablistManager() != null) {
                        this.plugin.getTablistManager().resetContext(var5);
                     }

                     boolean var16 = this.plugin
                        .getPlayerSettingsManager()
                        .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                           var5
                        )
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
                     if (this.plugin.isDebug()) {
                        this.plugin
                           .getLogger()
                           .info(
                              "[Duel Debug] Auto-Requeue p2="
                                 + var5.getName()
                                 + " wasQueueMatch="
                                 + var7
                                 + " inTournament="
                                 + var14
                                 + " inEvent="
                                 + var15
                                 + " isEliminatedSpec="
                                 + var13
                                 + " enabled="
                                 + var16
                           );
                     }

                     if (!var14 && !var15 && !var13 && var7 && var16) {
                        Bukkit.getScheduler()
                           .runTaskLater(
                              this.plugin,
                              () -> {
                                 if (!var5.isOnline()) {
                                    if (this.plugin.isDebug()) {
                                       this.plugin.getLogger().info("[Duel Debug] Auto-Requeue p2 aborted (offline)");
                                    }
                                 } else {
                                    boolean var3xx = this.isInMatch(var5.getUniqueId());
                                    boolean var4xx = this.plugin.getQueueManager().isInAnyQueue(var5.getUniqueId());
                                    if (!var3xx && !var4xx) {
                                       if (this.plugin.isDebug()) {
                                          this.plugin.getLogger().info("[Duel Debug] Auto-Requeue p2 enqueue kit=" + var6);
                                       }

                                       if (this.plugin.getQueueManager().addToQueue(var5.getUniqueId(), var6)) {
                                          this.plugin.getScoreboardManager().setState(var5, ScoreboardState.QUEUE);
                                          this.plugin
                                             .getScoreboardManager()
                                             .setPlaceholders(
                                                var5,
                                                Map.of(
                                                   "in_queue",
                                                   "true",
                                                   "in_queue_kitname",
                                                   this.plugin
                                                      .getDataManager()
                                                      .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                                         var6
                                                      )
                                                )
                                             );
                                          this.plugin.getSpawnItemsManager().giveSpawnItems(var5, "queue", false, false);
                                          this.plugin
                                             .getMessagesManager()
                                             .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                                var5,
                                                "auto-requeued",
                                                Map.of(
                                                   "kit",
                                                   this.plugin
                                                      .getDataManager()
                                                      .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                                         var6
                                                      )
                                                )
                                             );
                                       }
                                    } else {
                                       if (this.plugin.isDebug()) {
                                          this.plugin.getLogger().info("[Duel Debug] Auto-Requeue p2 skipped inMatch=" + var3xx + " inQueue=" + var4xx);
                                       }
                                    }
                                 }
                              },
                              40L
                           );
                     } else if (!var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void()
                        )
                      {
                        this.setRematchData(
                           var5.getUniqueId(),
                           var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
                           var6
                        );
                        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                           if (var5.isOnline()) {
                              this.giveRematchItem(var5);
                           }
                        }, 5L);
                     } else {
                        this.clearRematchData(var5.getUniqueId());
                     }
                  }

                  if (var3 != null) {
                     CompletableFuture.allOf(var8x.toArray(new CompletableFuture[0]))
                        .whenComplete((var2xx, var3xx) -> Bukkit.getScheduler().runTask(this.plugin, var3));
                  }
               }
            );
      }
   }

   private void cacheRematchItem() {
      this.cachedRematchItem = null;
      this.cachedRematchEnabled = false;

      try {
         YamlConfiguration var1 = YamlConfiguration.loadConfiguration(new File(this.plugin.getDataFolder(), "spawnitems.yml"));
         String var2 = "spawn-items.default.rematch";
         if (!var1.isConfigurationSection(var2)) {
            return;
         }

         this.cachedRematchEnabled = var1.getBoolean(var2 + ".enabled", true);
         if (!this.cachedRematchEnabled) {
            return;
         }

         String var3 = var2 + ".";
         String var4 = var1.getString(var3 + "material");
         if (var4 == null || var4.isEmpty()) {
            return;
         }

         this.cachedRematchMaterialName = var4.toUpperCase();
         this.cachedRematchSlot = var1.getInt(var3 + "slot", 4);
         this.cachedRematchExpireTicks = var1.getInt(var3 + "expire-time", 5) * 20;
         String var5 = var1.getString(var3 + "display-name", "");
         List var6 = var1.getStringList(var3 + "lore");
         boolean var7 = var1.getBoolean(var3 + "glow", true);
         ItemStack var8 = new ItemStack(Material.valueOf(this.cachedRematchMaterialName));
         ItemMeta var9 = var8.getItemMeta();
         if (var9 != null) {
            LegacyComponentSerializer var10 = LegacyComponentSerializer.legacyAmpersand();
            var9.displayName(
               var10.deserialize(
                     var5.replace("&#", "§x§")
                        .replaceAll("§x§([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])", "§x§$1§$2§$3§$4§$5§$6")
                  )
                  .decoration(TextDecoration.ITALIC, false)
            );
            if (!var6.isEmpty()) {
               ArrayList var11 = new ArrayList();

               for (String var13 : var6) {
                  var11.add(
                     var10.deserialize(
                           var13.replace("&#", "§x§")
                              .replaceAll("§x§([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])", "§x§$1§$2§$3§$4§$5§$6")
                        )
                        .decoration(TextDecoration.ITALIC, false)
                  );
               }

               var9.lore(var11);
            }

            if (var7) {
               var9.setEnchantmentGlintOverride(true);
            }

            var9.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
            var9.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
            var8.setItemMeta(var9);
         }

         this.cachedRematchItem = var8;
      } catch (Exception var14) {
         this.plugin.getLogger().warning("Failed to cache rematch item: " + var14.getMessage());
      }
   }

   public void reloadRematchItem() {
      this.cacheRematchItem();
   }

   public void tryGiveRematchItem(Player var1) {
      if (this.cachedRematchEnabled && this.cachedRematchItem != null && var1 != null) {
         if (this.getRematchData(var1.getUniqueId()) != null) {
            this.giveRematchItem(var1);
         }
      }
   }

   public int getRematchSlot() {
      return this.cachedRematchSlot;
   }

   public boolean isRematchItem(ItemStack var1, UUID var2) {
      return this.getRematchData(var2) != null && this.cachedRematchMaterialName != null
         ? var1 != null && var1.getType().name().equalsIgnoreCase(this.cachedRematchMaterialName)
         : false;
   }

   private void giveRematchItem(Player var1) {
      if (this.cachedRematchItem != null) {
         UUID var2 = var1.getUniqueId();
         this.cancelRematchExpireTask(var2);
         var1.getInventory().setItem(this.cachedRematchSlot, this.cachedRematchItem.clone());
         BukkitTask var3 = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            this.rematchExpireTasks.remove(var2);
            this.clearRematchData(var2);
         }, (long)this.cachedRematchExpireTicks);
         this.rematchExpireTasks.put(var2, var3);
      }
   }

   private void cancelRematchExpireTask(UUID var1) {
      BukkitTask var2 = this.rematchExpireTasks.remove(var1);
      if (var2 != null) {
         var2.cancel();
      }
   }

   private void removeRematchItemFromInventory(UUID var1) {
      Player var2 = Bukkit.getPlayer(var1);
      if (var2 != null && var2.isOnline() && this.cachedRematchMaterialName != null) {
         ItemStack var3 = var2.getInventory().getItem(this.cachedRematchSlot);
         if (var3 != null && var3.getType().name().equalsIgnoreCase(this.cachedRematchMaterialName)) {
            var2.getInventory().setItem(this.cachedRematchSlot, null);
         }
      }
   }

   private void updateScoreboards(g var1) {
      Player var2 = Bukkit.getPlayer(
         var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
      );
      Player var3 = Bukkit.getPlayer(
         var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
      );
      String var4 = this.cachedBlueIcon;
      String var5 = this.cachedBlueColor;
      String var6 = this.cachedRedIcon;
      String var7 = this.cachedRedColor;
      boolean var8 = this.plugin
         .getTournamentManager()
         .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
         );
      boolean var9 = this.plugin
         .getTournamentManager()
         .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
         );
      boolean var10 = this.plugin
         .getEventManager()
         .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
            var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
         );
      boolean var11 = this.plugin
         .getEventManager()
         .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
            var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
         );
      if (this.plugin.isDebug()) {
         this.plugin
            .getLogger()
            .info("[Duel Debug] updateScoreboards: p1Tournament=" + var8 + " p2Tournament=" + var9 + " p1Event=" + var10 + " p2Event=" + var11);
      }

      ScoreboardState var12 = this.plugin
         .getKitStateResolver()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
            ScoreboardState.DUEL
         );
      TablistContext var13 = this.plugin
         .getKitStateResolver()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
            TablistContext.DUEL
         );
      if (var2 != null) {
         if (var8) {
            this.plugin.getScoreboardManager().setState(var2, ScoreboardState.TOURNAMENT);
         } else if (var10) {
            this.plugin.getScoreboardManager().setState(var2, ScoreboardState.EVENT);
         } else {
            this.plugin.getScoreboardManager().setState(var2, var12);
         }

         this.plugin.getScoreboardManager().setPlaceholder(var2, "in_fight", "true");
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var2,
               "kit",
               this.plugin
                  .getDataManager()
                  .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                     var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                  )
            );
         this.plugin.getScoreboardManager().setPlaceholder(var2, "opponent", this.focusOpponentName(var2, var3));
         this.plugin.getScoreboardManager().setPlaceholder(var2, "opponent_ping", var3 != null ? String.valueOf(var3.getPing()) : "0");
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var2,
               "round",
               String.valueOf(
                  var1.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var2,
               "current_round",
               String.valueOf(
                  var1.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var2,
               "total_rounds",
               String.valueOf(
                  var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var2,
               "own_wins",
               String.valueOf(
                  var1.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var2,
               "your_wins",
               String.valueOf(
                  var1.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var2,
               "opponent_wins",
               String.valueOf(
                  var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var2,
               "is_bestof",
               var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                     > 1
                  ? "true"
                  : "false"
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var2,
               "duration",
               var1.Öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000forsuper()
            );
         this.plugin.getScoreboardManager().setPlaceholder(var2, "team_icon", "");
         this.plugin.getScoreboardManager().setPlaceholder(var2, "team_color", "");
         String var14 = "";
         if (var1.õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper()
            != null) {
            Player var15 = Bukkit.getPlayer(
               var1.õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper()
            );
            var14 = var15 != null ? var15.getName() : "Unknown";
         }

         this.plugin.getScoreboardManager().setPlaceholder(var2, "round_winner", var14);
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var2,
               "duel_kills",
               String.valueOf(
                  var1.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var2.getUniqueId()
                  )
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var2,
               "duel_deaths",
               String.valueOf(
                  var1.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                     var2.getUniqueId()
                  )
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var2,
               "opponent_kills",
               String.valueOf(
                  var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
                     var2.getUniqueId()
                  )
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var2,
               "opponent_deaths",
               String.valueOf(
                  var1.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var2.getUniqueId()
                  )
               )
            );
      }

      if (var3 != null) {
         if (var9) {
            this.plugin.getScoreboardManager().setState(var3, ScoreboardState.TOURNAMENT);
         } else if (var11) {
            this.plugin.getScoreboardManager().setState(var3, ScoreboardState.EVENT);
         } else {
            this.plugin.getScoreboardManager().setState(var3, var12);
         }

         this.plugin.getScoreboardManager().setPlaceholder(var3, "in_fight", "true");
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var3,
               "kit",
               this.plugin
                  .getDataManager()
                  .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                     var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                  )
            );
         this.plugin.getScoreboardManager().setPlaceholder(var3, "opponent", this.focusOpponentName(var3, var2));
         this.plugin.getScoreboardManager().setPlaceholder(var3, "opponent_ping", var2 != null ? String.valueOf(var2.getPing()) : "0");
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var3,
               "round",
               String.valueOf(
                  var1.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var3,
               "current_round",
               String.valueOf(
                  var1.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var3,
               "total_rounds",
               String.valueOf(
                  var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var3,
               "own_wins",
               String.valueOf(
                  var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var3,
               "your_wins",
               String.valueOf(
                  var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var3,
               "opponent_wins",
               String.valueOf(
                  var1.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var3,
               "is_bestof",
               var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                     > 1
                  ? "true"
                  : "false"
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var3,
               "duration",
               var1.Öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000forsuper()
            );
         this.plugin.getScoreboardManager().setPlaceholder(var3, "team_icon", "");
         this.plugin.getScoreboardManager().setPlaceholder(var3, "team_color", "");
         String var16 = "";
         if (var1.õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper()
            != null) {
            Player var17 = Bukkit.getPlayer(
               var1.õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper()
            );
            var16 = var17 != null ? var17.getName() : "Unknown";
         }

         this.plugin.getScoreboardManager().setPlaceholder(var3, "round_winner", var16);
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var3,
               "duel_kills",
               String.valueOf(
                  var1.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var3.getUniqueId()
                  )
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var3,
               "duel_deaths",
               String.valueOf(
                  var1.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                     var3.getUniqueId()
                  )
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var3,
               "opponent_kills",
               String.valueOf(
                  var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
                     var3.getUniqueId()
                  )
               )
            );
         this.plugin
            .getScoreboardManager()
            .setPlaceholder(
               var3,
               "opponent_deaths",
               String.valueOf(
                  var1.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var3.getUniqueId()
                  )
               )
            );
      }

      if (var2 != null && var3 != null && this.plugin.getTablistManager() != null) {
         this.plugin.getTablistManager().setupDuelContext(var2, var3, var1, var13);
      }
   }

   public g getMatch(UUID var1) {
      return this.activeMatches.get(var1);
   }

   public List<g> getActiveMatchesSnapshot() {
      Set var1 = Collections.newSetFromMap(new IdentityHashMap());
      var1.addAll(this.activeMatches.values());
      return new ArrayList<>(var1);
   }

   public void refreshMatchDisplay(UUID var1) {
      g var2 = this.getMatch(var1);
      if (var2 != null) {
         this.updateScoreboards(var2);
      }

      Player var3 = Bukkit.getPlayer(var1);
      if (var3 != null) {
         this.duelScoreActionBarManager
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var3
            );
      }
   }

   public void reloadDuelScoreActionBar() {
      this.duelScoreActionBarManager
         .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class();
   }

   public void onDuelScoreActionBarSettingChanged(Player var1, boolean var2) {
      this.duelScoreActionBarManager
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1, var2
         );
   }

   private void registerActiveMatch(g var1) {
      this.activeMatches
         .put(
            var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
            var1
         );
      this.activeMatches
         .put(
            var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper(),
            var1
         );
      this.duelScoreActionBarManager
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1
         );
      this.lastEngagementTimes.put(var1, System.currentTimeMillis());
   }

   public void recordOpponentEngagement(Player var1, Player var2) {
      if (var1 != null && var2 != null) {
         g var3 = this.getMatch(var2.getUniqueId());
         if (var3 != null
            && var3 == this.getMatch(var1.getUniqueId())
            && !var3.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
            && !var3.Õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000floatsuper()
            && !var3.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper()
            && !var3.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
            && var3.õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper()
               == null) {
            UUID var4 = var3.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
               var2.getUniqueId()
            );
            if (var4 != null && var4.equals(var1.getUniqueId())) {
               this.lastEngagementTimes.put(var3, System.currentTimeMillis());
               this.revealedInactiveMatches.remove(var3);
            }
         }
      }
   }

   private void startInactivityRevealTask() {
      this.inactivityRevealTask = Bukkit.getScheduler()
         .runTaskTimer(
            this.plugin,
            () -> {
               if (this.cachedInactivityRevealEnabled) {
                  long var1 = System.currentTimeMillis();

                  for (Entry var4 : this.lastEngagementTimes.entrySet()) {
                     g var5 = (g)var4.getKey();
                     if (var5 != null
                        && !var5.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                        && !var5.Õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000floatsuper()
                        && !var5.ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000whilesuper()
                        && !var5.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
                        && var5.õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper()
                           == null
                        && var1 - (Long)var4.getValue() >= this.cachedInactivityRevealMs
                        && this.revealedInactiveMatches.add(var5)) {
                        this.revealOpponentCoordinates(var5);
                     }
                  }
               }
            },
            20L,
            20L
         );
   }

   private void revealOpponentCoordinates(g var1) {
      Player var2 = Bukkit.getPlayer(
         var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
      );
      Player var3 = Bukkit.getPlayer(
         var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
      );
      if (var2 != null && var3 != null && var2.isOnline() && var3.isOnline() && var2.getWorld().equals(var3.getWorld())) {
         this.sendOpponentCoordinates(var2, var3);
         this.sendOpponentCoordinates(var3, var2);
      }
   }

   private void sendOpponentCoordinates(Player var1, Player var2) {
      Location var3 = var2.getLocation();
      HashMap var4 = new HashMap();
      var4.put("%opponent%", var2.getName());
      var4.put("%x%", String.valueOf(var3.getBlockX()));
      var4.put("%y%", String.valueOf(var3.getBlockY()));
      var4.put("%z%", String.valueOf(var3.getBlockZ()));
      var4.put("%world%", var3.getWorld().getName());
      var1.sendMessage(this.parseColoredText(this.replacePlaceholders(this.cachedInactivityRevealMessage, var4)));
      if (this.cachedInactivityRevealSound != null) {
         this.cachedInactivityRevealSound
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1
            );
      }
   }

   private String focusOpponentName(Player var1, Player var2) {
      return this.plugin
         .getPlayerSettingsManager()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1, var2
         );
   }

   public boolean isInMatch(UUID var1) {
      return this.activeMatches.containsKey(var1);
   }

   public void handlePlayerDeath(Player var1) {
      g var2 = this.getMatch(var1.getUniqueId());
      if (var2 != null) {
         UUID var3 = var2.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
            var1.getUniqueId()
         );
         Player var4 = Bukkit.getPlayer(var3);
         boolean var5 = this.willFinishWithWin(var2, var3);
         DamageCause var6 = var1.getLastDamageCause() != null ? var1.getLastDamageCause().getCause() : null;
         Component var7 = var4 != null
            ? this.plugin
               .getKillMessageManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var4, var1, var4, var6
               )
            : null;
         Component var8 = this.plugin
            .getKillMessageManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, var1, var4 != null ? var4 : var1, var6
            );
         if (var4 != null && var4.isOnline()) {
            HashMap var9 = new HashMap();
            var9.put("opponent", this.focusOpponentName(var4, var1));
            var9.put(
               "round",
               String.valueOf(
                  var2.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
               )
            );
            var9.put(
               "your_wins",
               String.valueOf(
                  var4.getUniqueId()
                        .equals(
                           var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                        )
                     ? var2.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
                        + 1
                     : var2.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
                        + 1
               )
            );
            var9.put(
               "opponent_wins",
               String.valueOf(
                  var4.getUniqueId()
                        .equals(
                           var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                        )
                     ? var2.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
                     : var2.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
               )
            );
            var9.put("round_winner", var4.getName());
            if (!var5) {
               this.plugin
                  .getMessagesManager()
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var4, "round-victory-title", var9
                  );
            }

            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var4, "round-won", var9
               );
            if (var7 != null) {
               var4.sendMessage(var7);
            }

            this.plugin
               .getKillEffectManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var4, var1
               );
            if (this.plugin.getKillSoundManager() != null) {
               this.plugin
                  .getKillSoundManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var4
                  );
            }
         }

         if (var8 != null) {
            var1.sendMessage(var8);
         }

         HashMap var11 = new HashMap();
         var11.put("opponent", this.focusOpponentName(var1, var4));
         var11.put(
            "round",
            String.valueOf(
               var2.ÔO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000private()
            )
         );
         var11.put(
            "your_wins",
            String.valueOf(
               var1.getUniqueId()
                     .equals(
                        var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                     )
                  ? var2.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
                  : var2.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
            )
         );
         var11.put(
            "opponent_wins",
            String.valueOf(
               var1.getUniqueId()
                     .equals(
                        var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                     )
                  ? var2.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
                     + 1
                  : var2.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
                     + 1
            )
         );
         var11.put("round_winner", var4 != null ? var4.getName() : "Unknown");
         if (!var5) {
            this.plugin
               .getMessagesManager()
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var1, "round-defeat-title", var11
               );
         }

         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "round-lost", var11
            );
         if (!var5) {
            this.playDuelSound(var1, this.cachedLoseSound, "round-loss");
         }

         this.statsManager
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1.getUniqueId(), var3, var1
            );
         this.statsManager
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var3, var1.getUniqueId(), var4
            );
         var2.ÒO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000while(
            var3
         );
         var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(
            var1.getUniqueId()
         );
         if (this.plugin.getEconomyManager() != null
            && this.plugin
               .getEconomyManager()
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
            )
          {
            if (var4 != null && var4.isOnline()) {
               this.plugin
                  .getEconomyManager()
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var4, var1
                  );
            }

            this.plugin
               .getEconomyManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1, var4
               );
         }

         if (this.plugin.getClanManager() != null) {
            this.plugin
               .getClanManager()
               .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
                  var3
               );
         }

         String var10 = var2.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
         this.statsManager
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var3, var10
            );
         this.statsManager
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var1.getUniqueId(), var10
            );
         this.endRound(var2, var3);
      }
   }

   private boolean willFinishWithWin(g var1, UUID var2) {
      if (var1 != null && var2 != null) {
         int var3;
         if (var2.equals(
            var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
         )) {
            var3 = var1.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
               + 1;
         } else {
            if (!var2.equals(
               var1.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
            )) {
               return false;
            }

            var3 = var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
               + 1;
         }

         return var3
            >= var1.OO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000for();
      } else {
         return false;
      }
   }

   public void handlePlayerQuit(Player var1) {
      UUID var2 = var1.getUniqueId();
      this.pendingRequests.remove(var2);

      for (List var4 : this.pendingRequests.values()) {
         var4.removeIf(
            var1x -> var1x.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                  .equals(var2)
         );
      }

      this.pendingRequests.entrySet().removeIf(var0 -> var0.getValue().isEmpty());
      this.clearRematchData(var2);
      g var5 = this.getMatch(var2);
      if (var5 != null) {
         UUID var6 = var5.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
            var2
         );
         this.endMatch(var5, var6);
      }
   }

   public void surrenderMatch(Player var1) {
      g var2 = this.getMatch(var1.getUniqueId());
      if (var2 != null
         && !var2.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
         && !var2.Õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000floatsuper()
         )
       {
         UUID var3 = var2.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
            var1.getUniqueId()
         );
         Player var4 = Bukkit.getPlayer(var3);
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "surrender-success"
            );
         if (var4 != null) {
            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var4, "opponent-surrendered", Map.of("player", var1.getName())
               );
         }

         if (this.cachedLeaveDelayEnabled && this.cachedLeaveDelayTicks > 0) {
            var2.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
               true
            );
            var2.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
               var1.getUniqueId()
            );
            var2.Ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000ifsuper();
            var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
               false
            );
            this.matchFoundEffects
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var2
               );
            var1.setInvulnerable(true);
            if (var4 != null && var4.isOnline()) {
               var4.setInvulnerable(true);
            }

            HashMap var5 = new HashMap();
            var5.put("%player%", var1.getName());
            var5.put("%opponent%", var4 != null ? var4.getName() : "Opponent");
            var5.put("%time%", String.valueOf(this.cachedLeaveDelayTicks / 20));
            Component var6 = this.parseColoredText(this.replacePlaceholders(this.cachedLeaveTitle, var5));
            Component var7 = this.parseColoredText(this.replacePlaceholders(this.cachedLeaveSubtitle, var5));
            var1.showTitle(Title.title(var6, var7, this.cachedLeaveTitleTimes));
            if (this.cachedLeaveSound != null) {
               this.cachedLeaveSound
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1
                  );
            }

            if (var4 != null && var4.isOnline()) {
               int var8 = var3.equals(
                     var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                  )
                  ? var2.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface()
                  : var2.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper();
               int var9 = var3.equals(
                     var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                  )
                  ? var2.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
                  : var2.ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface();
               Map var10 = this.buildFinalFeedbackPlaceholders(var2, var4, var1, var8, var9);
               this.plugin
                  .getMessagesManager()
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var4, "duel-win-title", var10
                  );
               this.playDuelSoundNow(var4, this.cachedWinSound, "surrender-win");
            }

            var2.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
               true
            );
            int var11 = Bukkit.getScheduler()
               .runTaskLater(
                  this.plugin,
                  () -> {
                     if (!var2.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                        && var2.Õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000floatsuper()
                        )
                      {
                        var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                           -1
                        );
                        this.endMatch(var2, var3);
                     }
                  },
                  (long)this.cachedLeaveDelayTicks
               )
               .getTaskId();
            var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var11
            );
         } else {
            this.endMatch(var2, var3);
         }
      }
   }

   public e getPendingRequest(UUID var1) {
      List var2 = this.pendingRequests.get(var1);
      return var2 != null && !var2.isEmpty() ? var2.stream().filter(var1x -> !this.isRequestExpired(var1x)).findFirst().orElse(null) : null;
   }

   public List<e> getPendingRequests(UUID var1) {
      List var2 = this.pendingRequests.get(var1);
      return var2 == null ? List.of() : var2.stream().filter(var1x -> !this.isRequestExpired(var1x)).toList();
   }

   public e getMostRecentRequest(UUID var1) {
      List var2 = this.getPendingRequests(var1);
      return var2.isEmpty() ? null : (e)var2.get(var2.size() - 1);
   }

   public List<String> getRequestSenderNames(UUID var1) {
      return this.getPendingRequests(var1)
         .stream()
         .map(
            var0 -> Bukkit.getOfflinePlayer(
                     var0.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                  )
                  .getName()
         )
         .filter(Objects::nonNull)
         .toList();
   }

   public void clearAllRequests(UUID var1) {
      this.pendingRequests.remove(var1);
   }

   private boolean tryLockDuelAccept(UUID var1, UUID var2) {
      UUID var3 = var1.compareTo(var2) <= 0 ? var1 : var2;
      UUID var4 = var3.equals(var1) ? var2 : var1;
      if (!this.acceptingPlayers.add(var3)) {
         return false;
      } else if (!this.acceptingPlayers.add(var4)) {
         this.acceptingPlayers.remove(var3);
         return false;
      } else {
         return true;
      }
   }

   private void unlockDuelAccept(UUID var1, UUID var2) {
      this.acceptingPlayers.remove(var1);
      this.acceptingPlayers.remove(var2);
   }

   private boolean isPlayerBusyForDuel(Player var1) {
      UUID var2 = var1.getUniqueId();
      return this.isInMatch(var2)
         || this.plugin.getSpectatorManager() != null
            && this.plugin
               .getSpectatorManager()
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                  var2
               )
         || this.plugin.getQueueManager().isInAnyQueue(var2)
         || this.plugin.getFFAManager().isInFFA(var2)
         || this.plugin.getPartyManager().isInParty(var2)
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
            )
         || this.plugin.getBotDuelManager() != null && this.plugin.getBotDuelManager().isInBotDuel(var2);
   }

   private void clearRequestsInvolving(UUID var1, UUID var2) {
      this.pendingRequests.remove(var1);
      this.pendingRequests.remove(var2);

      for (List var4 : this.pendingRequests.values()) {
         var4.removeIf(
            var2x -> var2x.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                     .equals(var1)
                  || var2x.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                     .equals(var2)
                  || var2x.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
                     .equals(var1)
                  || var2x.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
                     .equals(var2)
         );
      }

      this.pendingRequests.entrySet().removeIf(var0 -> var0.getValue().isEmpty());
   }

   private void sendClickableAcceptMessage(Player var1, String var2) {
      ConfigurationSection var3 = this.plugin
         .getMessagesManager()
         .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
         .getConfigurationSection("messages.duel-click-accept");
      String var4 = "&a&l[CLICK TO ACCEPT]";
      String var5 = "&7Click to accept duel from &e%sender%";
      String var6 = "duel accept %sender%";
      if (var3 != null) {
         var4 = var3.getString("text", var4);
         var5 = var3.getString("hover", var5);
         var6 = var3.getString("command", var6);
      }

      var4 = var4.replace("%sender%", var2);
      var5 = var5.replace("%sender%", var2);
      var6 = var6.replace("%sender%", var2);
      Component var7 = this.parseColoredText(var4).clickEvent(ClickEvent.runCommand("/" + var6)).hoverEvent(HoverEvent.showText(this.parseColoredText(var5)));
      var1.sendMessage(var7);
   }

   private Component parseColoredText(String var1) {
      if (!var1.contains("&#") && !var1.contains("<")) {
         return LegacyComponentSerializer.legacyAmpersand().deserialize(var1);
      } else {
         String var2 = this.convertHexColors(var1);
         var2 = this.convertLegacyCodes(var2);
         return MiniMessage.miniMessage().deserialize(var2);
      }
   }

   private String convertHexColors(String var1) {
      Matcher var2 = HEX_PATTERN.matcher(var1);
      StringBuilder var3 = new StringBuilder();

      while (var2.find()) {
         var2.appendReplacement(var3, "<color:#" + var2.group(1) + ">");
      }

      var2.appendTail(var3);
      return var3.toString();
   }

   private String convertLegacyCodes(String var1) {
      return var1.replace("&0", "<black>")
         .replace("§0", "<black>")
         .replace("&1", "<dark_blue>")
         .replace("§1", "<dark_blue>")
         .replace("&2", "<dark_green>")
         .replace("§2", "<dark_green>")
         .replace("&3", "<dark_aqua>")
         .replace("§3", "<dark_aqua>")
         .replace("&4", "<dark_red>")
         .replace("§4", "<dark_red>")
         .replace("&5", "<dark_purple>")
         .replace("§5", "<dark_purple>")
         .replace("&6", "<gold>")
         .replace("§6", "<gold>")
         .replace("&7", "<gray>")
         .replace("§7", "<gray>")
         .replace("&8", "<dark_gray>")
         .replace("§8", "<dark_gray>")
         .replace("&9", "<blue>")
         .replace("§9", "<blue>")
         .replace("&a", "<green>")
         .replace("&A", "<green>")
         .replace("§a", "<green>")
         .replace("§A", "<green>")
         .replace("&b", "<aqua>")
         .replace("&B", "<aqua>")
         .replace("§b", "<aqua>")
         .replace("§B", "<aqua>")
         .replace("&c", "<red>")
         .replace("&C", "<red>")
         .replace("§c", "<red>")
         .replace("§C", "<red>")
         .replace("&d", "<light_purple>")
         .replace("&D", "<light_purple>")
         .replace("§d", "<light_purple>")
         .replace("§D", "<light_purple>")
         .replace("&e", "<yellow>")
         .replace("&E", "<yellow>")
         .replace("§e", "<yellow>")
         .replace("§E", "<yellow>")
         .replace("&f", "<white>")
         .replace("&F", "<white>")
         .replace("§f", "<white>")
         .replace("§F", "<white>")
         .replace("&k", "<obfuscated>")
         .replace("&K", "<obfuscated>")
         .replace("§k", "<obfuscated>")
         .replace("§K", "<obfuscated>")
         .replace("&l", "<bold>")
         .replace("&L", "<bold>")
         .replace("§l", "<bold>")
         .replace("§L", "<bold>")
         .replace("&m", "<strikethrough>")
         .replace("&M", "<strikethrough>")
         .replace("§m", "<strikethrough>")
         .replace("§M", "<strikethrough>")
         .replace("&n", "<underlined>")
         .replace("&N", "<underlined>")
         .replace("§n", "<underlined>")
         .replace("§N", "<underlined>")
         .replace("&o", "<italic>")
         .replace("&O", "<italic>")
         .replace("§o", "<italic>")
         .replace("§O", "<italic>")
         .replace("&r", "<reset>")
         .replace("&R", "<reset>")
         .replace("§r", "<reset>")
         .replace("§R", "<reset>");
   }

   private void startRequestCleanupTask() {
      this.requestCleanupTask = Bukkit.getScheduler()
         .runTaskTimerAsynchronously(
            this.plugin,
            () -> {
               long var1 = System.currentTimeMillis();

               for (List var4 : this.pendingRequests.values()) {
                  var4.removeIf(
                     var3 -> var1
                              - var3.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
                           > this.cachedRequestExpiryMs
                  );
               }

               this.pendingRequests.entrySet().removeIf(var0 -> var0.getValue().isEmpty());
            },
            600L,
            600L
         );
   }

   public boolean isRequestExpired(e var1) {
      return System.currentTimeMillis()
            - var1.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
         > this.cachedRequestExpiryMs;
   }

   public void startQueueMatch(Player var1, Player var2, String var3, int var4, boolean var5) {
      this.startQueueMatch(var1, var2, var3, var4, var5, (org.lime.swiftCore.arena.d)null);
   }

   public void startQueueMatch(Player var1, Player var2, String var3, int var4, boolean var5, org.lime.swiftCore.arena.d var6) {
      if (this.plugin.isDebug()) {
         this.plugin
            .getLogger()
            .info("[Duel Debug] startQueueMatch: " + var1.getName() + " vs " + var2.getName() + " kit=" + var3 + " rounds=" + var4 + " ranked=" + var5);
      }

      if (var1.isOnline() && var2.isOnline()) {
         if (!this.isPlayerBusyForQueueStart(var1) && !this.isPlayerBusyForQueueStart(var2)) {
            if (this.plugin
               .getRandomKitManager()
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                  var3
               )) {
               String var7 = this.plugin
                  .getRandomKitManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var3
                  );
               if (var7 == null) {
                  var1.sendMessage(Component.text("No kits available for random selection!").color(NamedTextColor.RED));
                  var2.sendMessage(Component.text("No kits available for random selection!").color(NamedTextColor.RED));
                  this.plugin.getQueueManager().removeFightingPlayer(var1.getUniqueId());
                  this.plugin.getQueueManager().removeFightingPlayer(var2.getUniqueId());
                  this.plugin.getScoreboardManager().setState(var1, ScoreboardState.DEFAULT);
                  this.plugin.getScoreboardManager().setState(var2, ScoreboardState.DEFAULT);
                  this.plugin.getScoreboardManager().setPlaceholder(var1, "in_queue_kitname", "");
                  this.plugin.getScoreboardManager().setPlaceholder(var2, "in_queue_kitname", "");
                  this.plugin.getSpawnItemsManager().giveSpawnItems(var1, "default", false, false);
                  this.plugin.getSpawnItemsManager().giveSpawnItems(var2, "default", false, false);
                  this.releaseUnusedReservedArena(var6);
                  this.returnCrossServerQueuePlayersToLobby(var1, var2);
                  return;
               }

               var1.sendMessage(
                  ((TextComponent)Component.text("Random Kit Selected: ").color(NamedTextColor.GOLD))
                     .append(
                        LegacyComponentSerializer.legacySection()
                           .deserialize(
                              this.plugin
                                 .getDataManager()
                                 .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                    var7
                                 )
                                 .replace('&', '§')
                           )
                     )
               );
               var2.sendMessage(
                  ((TextComponent)Component.text("Random Kit Selected: ").color(NamedTextColor.GOLD))
                     .append(
                        LegacyComponentSerializer.legacySection()
                           .deserialize(
                              this.plugin
                                 .getDataManager()
                                 .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                    var7
                                 )
                                 .replace('&', '§')
                           )
                     )
               );
               var3 = var7;
            }

            t var13 = this.plugin
               .getKitManager()
               .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                  var3
               );
            if (var13 == null) {
               if (this.plugin.isDebug()) {
                  this.plugin.getLogger().warning("[Duel Debug] startQueueMatch: kit '" + var3 + "' not found!");
               }

               var1.sendMessage(Component.text("Kit not found!").color(NamedTextColor.RED));
               var2.sendMessage(Component.text("Kit not found!").color(NamedTextColor.RED));
               this.plugin.getQueueManager().removeFightingPlayer(var1.getUniqueId());
               this.plugin.getQueueManager().removeFightingPlayer(var2.getUniqueId());
               this.plugin.getScoreboardManager().setState(var1, ScoreboardState.DEFAULT);
               this.plugin.getScoreboardManager().setState(var2, ScoreboardState.DEFAULT);
               this.plugin.getScoreboardManager().setPlaceholder(var1, "in_queue_kitname", "");
               this.plugin.getScoreboardManager().setPlaceholder(var2, "in_queue_kitname", "");
               this.plugin.getSpawnItemsManager().giveSpawnItems(var1, "default", false, false);
               this.plugin.getSpawnItemsManager().giveSpawnItems(var2, "default", false, false);
               this.releaseUnusedReservedArena(var6);
               this.returnCrossServerQueuePlayersToLobby(var1, var2);
            } else {
               if (var4 <= 0) {
                  var4 = this.getQueueDefaultRounds(var3, var5);
               } else {
                  var4 = this.clampRounds(var4);
               }

               org.lime.swiftCore.arena.d var8 = var6 != null
                  ? var6
                  : this.arenaManager
                     .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                        var13.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(),
                        var3
                     );
               if (var8 == null) {
                  String var9 = var3;
                  int var10 = var4;
                  if (this.markArenaPending(var1.getUniqueId(), var2.getUniqueId())) {
                     this.plugin
                        .getDynamicArenaManager()
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                           var13.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(),
                           var3
                        )
                        .whenComplete(
                           (var8x, var9x) -> Bukkit.getScheduler()
                                 .runTask(
                                    this.plugin,
                                    () -> {
                                       this.clearArenaPending(var1.getUniqueId(), var2.getUniqueId());
                                       Player var10x = Bukkit.getPlayer(var1.getUniqueId());
                                       Player var11 = Bukkit.getPlayer(var2.getUniqueId());
                                       if (var9x != null) {
                                          this.handleNoQueueArena(
                                             var10x,
                                             var11,
                                             var13.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(),
                                             var6
                                          );
                                       } else if (var10x != null
                                          && var10x.isOnline()
                                          && var11 != null
                                          && var11.isOnline()
                                          && !this.isPlayerBusyForQueueStart(var10x)
                                          && !this.isPlayerBusyForQueueStart(var11)
                                          && this.plugin.getQueueManager().isFightingPlayer(var10x.getUniqueId())
                                          && this.plugin.getQueueManager().isFightingPlayer(var11.getUniqueId())) {
                                          this.startQueueMatch(var10x, var11, var9, var10, var5, var8x);
                                       } else {
                                          this.plugin
                                             .getDynamicArenaManager()
                                             .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                                var8x
                                             );
                                          this.cleanupCancelledQueueMatch(var1, var2, var6);
                                       }
                                    }
                                 )
                        );
                  }
               } else if (var8.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                     != org.lime.swiftCore.arena.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                  && !this.arenaManager
                     .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(
                        var8
                     )) {
                  this.handleNoQueueArena(
                     var1,
                     var2,
                     var13.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(),
                     var6
                  );
               } else {
                  this.plugin
                     .getDataManager()
                     .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
                        var1.getUniqueId(), var3
                     );
                  this.plugin
                     .getDataManager()
                     .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
                        var2.getUniqueId(), var3
                     );
                  this.startMatch(var1, var2, var3, var4, var8, var5, true);
               }
            }
         } else {
            if (this.plugin.isDebug()) {
               this.plugin.getLogger().warning("[Duel Debug] startQueueMatch cancelled: a player became busy before match start.");
            }

            this.cleanupCancelledQueueMatch(var1, var2, var6);
         }
      } else {
         if (this.plugin.isDebug()) {
            this.plugin.getLogger().warning("[Duel Debug] startQueueMatch cancelled: p1Online=" + var1.isOnline() + " p2Online=" + var2.isOnline());
         }

         this.plugin.getQueueManager().removeFightingPlayer(var1.getUniqueId());
         this.plugin.getQueueManager().removeFightingPlayer(var2.getUniqueId());
         this.releaseUnusedReservedArena(var6);
         this.returnCrossServerQueuePlayersToLobby(var1, var2);
      }
   }

   private void handleNoQueueArena(Player var1, Player var2, ArenaType var3, org.lime.swiftCore.arena.d var4) {
      if (var4 != null
         && var4.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
            == org.lime.swiftCore.arena.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
         )
       {
         this.arenaManager
            .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
               var4
            );
      }

      if (this.plugin.isDebug()) {
         this.plugin.getLogger().warning("[Duel Debug] startQueueMatch: no arena for type=" + var3);
      }

      Map var5 = Map.of("arena_type", var3.name());
      if (var1 != null && var1.isOnline()) {
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "duel-no-arenas", var5
            );
         this.plugin.getScoreboardManager().setState(var1, ScoreboardState.DEFAULT);
         this.plugin.getScoreboardManager().setPlaceholder(var1, "in_queue_kitname", "");
         this.plugin.getSpawnItemsManager().giveSpawnItems(var1, "default", false, false);
         this.plugin.getQueueManager().removeFightingPlayer(var1.getUniqueId());
      }

      if (var2 != null && var2.isOnline()) {
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var2, "duel-no-arenas", var5
            );
         this.plugin.getScoreboardManager().setState(var2, ScoreboardState.DEFAULT);
         this.plugin.getScoreboardManager().setPlaceholder(var2, "in_queue_kitname", "");
         this.plugin.getSpawnItemsManager().giveSpawnItems(var2, "default", false, false);
         this.plugin.getQueueManager().removeFightingPlayer(var2.getUniqueId());
      }

      this.returnCrossServerQueuePlayersToLobby(var1, var2);
   }

   public void startQueueMatch(Player var1, Player var2, String var3) {
      this.startQueueMatch(var1, var2, var3, 0, false);
   }

   private boolean isPlayerBusyForQueueStart(Player var1) {
      UUID var2 = var1.getUniqueId();
      return this.isInMatch(var2)
         || this.plugin.getSpectatorManager() != null
            && this.plugin
               .getSpectatorManager()
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                  var2
               )
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
            )
         || this.plugin.getBotDuelManager() != null && this.plugin.getBotDuelManager().isInBotDuel(var2);
   }

   private void cleanupCancelledQueueMatch(Player var1, Player var2, org.lime.swiftCore.arena.d var3) {
      if (var1 != null) {
         this.plugin.getQueueManager().removeFightingPlayer(var1.getUniqueId());
         this.matchFoundEffects
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var1.getUniqueId()
            );
      }

      if (var2 != null) {
         this.plugin.getQueueManager().removeFightingPlayer(var2.getUniqueId());
         this.matchFoundEffects
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var2.getUniqueId()
            );
      }

      this.releaseUnusedReservedArena(var3);
      this.returnCrossServerQueuePlayersToLobby(var1, var2);
   }

   private void releaseUnusedReservedArena(org.lime.swiftCore.arena.d var1) {
      if (var1 != null
         && var1.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
            == org.lime.swiftCore.arena.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
         )
       {
         if (var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
            && this.plugin.getDynamicArenaManager() != null) {
            this.plugin
               .getDynamicArenaManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1
               );
         } else {
            this.arenaManager
               .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
                  var1
               );
         }

         if (this.plugin.isDebug()) {
            this.plugin
               .getLogger()
               .info(
                  "[Arena Debug] Released unused reserved arena '"
                     + var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                     + "' after queue match cancellation."
               );
         }
      }
   }

   private void returnCrossServerQueuePlayersToLobby(Player var1, Player var2) {
      if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isMatchServer()) {
         org.lime.swiftCore.v.f.f var3 = this.plugin.getLimboManager().getMatchIntentHandler();
         org.lime.swiftCore.v.f.d var4 = this.plugin.getLimboManager().getTransferManager();
         if (var4 != null) {
            for (Player var6 : List.of(var1, var2)) {
               if (var6 != null && var6.isOnline()) {
                  if (var3 != null) {
                     var3.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                        var6
                     );
                  }

                  var4.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var6
                  );
               }
            }
         }
      }
   }

   public void startTournamentMatch(Player var1, Player var2, String var3) {
      this.startTournamentMatch(var1, var2, var3, null);
   }

   public void startTournamentMatch(Player var1, Player var2, String var3, org.lime.swiftCore.arena.d var4) {
      if (var1.isOnline() && var2.isOnline()) {
         t var5 = this.plugin
            .getKitManager()
            .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
               var3
            );
         if (var5 == null) {
            this.releaseUnusedReservedArena(var4);
         } else {
            org.lime.swiftCore.arena.d var6 = var4;
            if (var4 == null) {
               var6 = this.arenaManager
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var5.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                  );
               if (var6 == null) {
                  var6 = this.arenaManager
                     .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null();
               }

               if (var6 == null) {
                  var6 = this.arenaManager
                     .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                        var5.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(),
                        var3
                     );
               }
            }

            if (var6 == null) {
               if (this.markArenaPending(var1.getUniqueId(), var2.getUniqueId())) {
                  this.plugin
                     .getDynamicArenaManager()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var5.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(),
                        var3
                     )
                     .exceptionallyCompose(
                        var3x -> this.plugin
                              .getDynamicArenaManager()
                              .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                                 var5.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(),
                                 var3
                              )
                     )
                     .whenComplete(
                        (var4x, var5x) -> Bukkit.getScheduler()
                              .runTask(
                                 this.plugin,
                                 () -> {
                                    this.clearArenaPending(var1.getUniqueId(), var2.getUniqueId());
                                    Player var6x = Bukkit.getPlayer(var1.getUniqueId());
                                    Player var7 = Bukkit.getPlayer(var2.getUniqueId());
                                    if (var5x != null) {
                                       if (var6x != null && var6x.isOnline()) {
                                          this.plugin
                                             .getMessagesManager()
                                             .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                                var6x, "tournament-arena-not-free"
                                             );
                                       }

                                       if (var7 != null && var7.isOnline()) {
                                          this.plugin
                                             .getMessagesManager()
                                             .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                                var7, "tournament-arena-not-free"
                                             );
                                       }
                                    } else if (var6x != null
                                       && var6x.isOnline()
                                       && var7 != null
                                       && var7.isOnline()
                                       && !this.isInMatch(var6x.getUniqueId())
                                       && !this.isInMatch(var7.getUniqueId())) {
                                       this.startTournamentMatch(var6x, var7, var3, var4x);
                                    } else {
                                       this.plugin
                                          .getDynamicArenaManager()
                                          .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                             var4x
                                          );
                                    }
                                 }
                              )
                     );
               }
            } else {
               this.plugin
                  .getDataManager()
                  .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
                     var1.getUniqueId(), var3
                  );
               this.plugin
                  .getDataManager()
                  .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
                     var2.getUniqueId(), var3
                  );
               this.startMatch(var1, var2, var3, 1, var6, false, true);
            }
         }
      } else {
         this.releaseUnusedReservedArena(var4);
      }
   }

   @Deprecated
   private String selectRandomKit() {
      return this.plugin
         .getRandomKitManager()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            "randomkit"
         );
   }

   public void startRankedMatch(Player var1, Player var2, String var3, int var4, org.lime.swiftCore.arena.d var5) {
      g var6 = new g(var1.getUniqueId(), var2.getUniqueId(), var3, var4, var5, true);
      var6.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
         this.cachedFirstTo
      );
      var6.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
         true
      );
      this.plugin.getArenaListener().cleanupTempRespawnSpectators(var1.getUniqueId(), var2.getUniqueId());
      this.plugin
         .getSpectatorManager()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1.getUniqueId()
         );
      this.plugin
         .getSpectatorManager()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var2.getUniqueId()
         );
      this.plugin.getArenaListener().addTeleportGracePeriod(var1.getUniqueId());
      this.plugin.getArenaListener().addTeleportGracePeriod(var2.getUniqueId());
      this.registerActiveMatch(var6);
      this.plugin.getKitRulesListener().addActivePlayer(var1.getUniqueId(), var3);
      this.plugin.getKitRulesListener().addActivePlayer(var2.getUniqueId(), var3);
      if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().getActiveMatchRegistry() != null) {
         this.plugin
            .getLimboManager()
            .getActiveMatchRegistry()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var6.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper(),
               List.of(
                  var6.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
                  var6.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
               ),
               var6.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                  .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(),
               var6.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
               "RANKED"
            );
      }

      boolean var7 = this.getKitRule(var3, KitRule.HP_INDICATOR);
      if (this.plugin.isDebug()) {
         this.plugin
            .getLogger()
            .info(
               "[HP Indicator Debug] ranked/startRankedMatch kit="
                  + var3
                  + " normalKit="
                  + org.lime.swiftCore.kit.h.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                     var3
                  )
                  + " enabled="
                  + var7
            );
      }

      if (this.plugin.getHPIndicatorManager() != null && var7) {
         this.plugin
            .getHPIndicatorManager()
            .startForMatch(
               var1.getUniqueId(),
               var2.getUniqueId(),
               var5.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
            );
      }

      if (var5.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
         != org.lime.swiftCore.arena.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
         )
       {
         this.arenaManager
            .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(
               var5
            );
      }

      this.arenaManager
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1.getUniqueId(),
            var5.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
         );
      this.arenaManager
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var2.getUniqueId(),
            var5.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
         );
      this.plugin
         .getChunkyIntegration()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var5
         );
      if (!this.matchFoundEffects
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
         )
       {
         this.matchFoundEffects
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, var2, var3
            );
      }

      UUID var8 = var1.getUniqueId();
      UUID var9 = var2.getUniqueId();
      Runnable var11 = () -> {
         if (!var6.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
            )
          {
            Player var6x = Bukkit.getPlayer(var8);
            Player var7x = Bukkit.getPlayer(var9);
            if (var6x != null && var6x.isOnline() && var7x != null && var7x.isOnline()) {
               var6x.getInventory().clear();
               var7x.getInventory().clear();
               org.lime.swiftCore.b.b.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var6x
               );
               org.lime.swiftCore.b.b.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var7x
               );
               org.lime.swiftCore.b.h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var6x
               );
               org.lime.swiftCore.b.h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var7x
               );
               this.plugin
                  .getPlayerSettingsManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     List.of(var6x, var7x)
                  );
               this.teleportPlayersToArenaForMatchStart(
                  var6x,
                  var7x,
                  var5,
                  var3,
                  () -> {
                     if (!var6.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                        )
                      {
                        Player var6xx = Bukkit.getPlayer(var8);
                        Player var7xx = Bukkit.getPlayer(var9);
                        if (var6xx != null && var6xx.isOnline() && var7xx != null && var7xx.isOnline()) {
                           var6.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
                              true
                           );
                           this.plugin
                              .getPlayerSettingsManager()
                              .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 List.of(var6xx, var7xx)
                              );
                           if (this.plugin.getHPIndicatorManager() != null && this.plugin.getHPIndicatorManager().isActive(var8)) {
                              this.plugin.getHPIndicatorManager().refreshForMatch(var8, var9);
                           }

                           if (this.getKitRule(var3, KitRule.BEDWARS)) {
                              var5.õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper();
                           }

                           this.giveKitsWithRetry(var8, var9, var3, var5, var6, 0);
                           this.updateScoreboards(var6);
                           this.startCountdown(
                              var6xx,
                              var7xx,
                              var6,
                              true,
                              () -> this.matchFoundEffects
                                    .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                       var6xx, var7xx, var5, var6, null
                                    )
                           );
                        } else {
                           this.endMatch(var6, null);
                        }
                     }
                  }
               );
            } else {
               if (var6x != null) {
                  var6x.sendMessage(Component.text("Match cancelled - opponent disconnected").color(NamedTextColor.RED));
               }

               if (var7x != null) {
                  var7x.sendMessage(Component.text("Match cancelled - opponent disconnected").color(NamedTextColor.RED));
               }

               this.endMatch(var6, null);
            }
         }
      };
      if (this.matchFoundEffects
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
         )
       {
         var11.run();
      } else {
         Bukkit.getScheduler()
            .runTaskLater(
               this.plugin,
               var11,
               (long)this.matchFoundEffects
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
            );
      }
   }

   public void startCustomKitMatch(Player var1, Player var2, ArenaType var3, ItemStack[] var4, ItemStack[] var5, ItemStack[] var6, ItemStack[] var7) {
      this.startCustomKitMatch(var1, var2, var3, var4, var5, var6, var7, null, 1);
   }

   public void startCustomKitMatch(
      Player var1, Player var2, ArenaType var3, ItemStack[] var4, ItemStack[] var5, ItemStack[] var6, ItemStack[] var7, Map<KitRule, Boolean> var8, int var9
   ) {
      this.startCustomKitMatch(var1, var2, var3, var4, var5, var6, var7, var8, var9, null);
   }

   private void startCustomKitMatch(
      Player var1,
      Player var2,
      ArenaType var3,
      ItemStack[] var4,
      ItemStack[] var5,
      ItemStack[] var6,
      ItemStack[] var7,
      Map<KitRule, Boolean> var8,
      int var9,
      org.lime.swiftCore.arena.d var10
   ) {
      if (var1.isOnline() && var2.isOnline()) {
         org.lime.swiftCore.arena.d var11 = var10 != null
            ? var10
            : this.arenaManager
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var3, null
               );
         if (var11 == null) {
            if (this.markArenaPending(var1.getUniqueId(), var2.getUniqueId())) {
               this.plugin
                  .getDynamicArenaManager()
                  .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                     var3, "customkit"
                  )
                  .whenComplete(
                     (var10x, var11x) -> Bukkit.getScheduler()
                           .runTask(
                              this.plugin,
                              () -> {
                                 this.clearArenaPending(var1.getUniqueId(), var2.getUniqueId());
                                 Player var12x = Bukkit.getPlayer(var1.getUniqueId());
                                 Player var13x = Bukkit.getPlayer(var2.getUniqueId());
                                 if (var11x != null) {
                                    if (var12x != null && var12x.isOnline()) {
                                       var12x.sendMessage(Component.text("No " + var3.name() + " arenas available!").color(NamedTextColor.RED));
                                    }

                                    if (var13x != null && var13x.isOnline()) {
                                       var13x.sendMessage(Component.text("No " + var3.name() + " arenas available!").color(NamedTextColor.RED));
                                    }
                                 } else if (!this.validWaitingDuelPlayers(var12x, var13x)) {
                                    this.plugin
                                       .getDynamicArenaManager()
                                       .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                          var10x
                                       );
                                 } else {
                                    this.startCustomKitMatch(var12x, var13x, var3, var4, var5, var6, var7, var8, var9, var10x);
                                 }
                              }
                           )
                  );
            }
         } else {
            String var12 = "customkit";
            var9 = Math.max(1, Math.min(var9, this.cachedMaxRounds));
            g var13 = new g(var1.getUniqueId(), var2.getUniqueId(), var12, var9, var11, false, true);
            var13.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               this.cachedFirstTo
            );
            var13.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
               true
            );
            this.plugin.getArenaListener().cleanupTempRespawnSpectators(var1.getUniqueId(), var2.getUniqueId());
            this.plugin
               .getSpectatorManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1.getUniqueId()
               );
            this.plugin
               .getSpectatorManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var2.getUniqueId()
               );
            this.plugin.getArenaListener().addTeleportGracePeriod(var1.getUniqueId());
            this.plugin.getArenaListener().addTeleportGracePeriod(var2.getUniqueId());
            this.registerActiveMatch(var13);
            this.plugin.getKitRulesListener().addActivePlayer(var1.getUniqueId(), var12);
            this.plugin.getKitRulesListener().addActivePlayer(var2.getUniqueId(), var12);
            if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().getActiveMatchRegistry() != null) {
               this.plugin
                  .getLimboManager()
                  .getActiveMatchRegistry()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var13.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper(),
                     List.of(
                        var13.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
                        var13.Øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000dosuper()
                     ),
                     var13.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                        .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(),
                     var13.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
                     "CUSTOM"
                  );
            }

            if (var11.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                  != org.lime.swiftCore.arena.c.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
               && !this.arenaManager
                  .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(
                     var11
                  )) {
               var1.sendMessage(Component.text("No " + var3.name() + " arenas available!").color(NamedTextColor.RED));
               var2.sendMessage(Component.text("No " + var3.name() + " arenas available!").color(NamedTextColor.RED));
            } else {
               this.arenaManager
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1.getUniqueId(),
                     var11.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                  );
               this.arenaManager
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var2.getUniqueId(),
                     var11.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                  );
               this.plugin
                  .getChunkyIntegration()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var11
                  );
               this.matchFoundEffects
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1, var2, var12
                  );
               UUID var14 = var1.getUniqueId();
               UUID var15 = var2.getUniqueId();
               ItemStack[] var16 = var4 != null ? (ItemStack[])var4.clone() : new ItemStack[41];
               ItemStack[] var17 = var5 != null ? (ItemStack[])var5.clone() : new ItemStack[4];
               ItemStack[] var18 = var6 != null ? (ItemStack[])var6.clone() : new ItemStack[41];
               ItemStack[] var19 = var7 != null ? (ItemStack[])var7.clone() : new ItemStack[4];
               Bukkit.getScheduler()
                  .runTaskLater(
                     this.plugin,
                     () -> {
                        if (!var13.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                           )
                         {
                           Player var10x = Bukkit.getPlayer(var14);
                           Player var11x = Bukkit.getPlayer(var15);
                           if (var10x != null && var10x.isOnline() && var11x != null && var11x.isOnline()) {
                              var10x.getInventory().clear();
                              var11x.getInventory().clear();
                              org.lime.swiftCore.b.b.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var10x
                              );
                              org.lime.swiftCore.b.h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var10x
                              );
                              org.lime.swiftCore.b.b.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var11x
                              );
                              org.lime.swiftCore.b.h.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var11x
                              );
                              this.teleportPlayersToArenaForMatchStart(
                                 var10x,
                                 var11x,
                                 var11,
                                 var12,
                                 () -> {
                                    if (!var13.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                                       )
                                     {
                                       Player var9xx = Bukkit.getPlayer(var14);
                                       Player var10xx = Bukkit.getPlayer(var15);
                                       if (var9xx != null && var9xx.isOnline() && var10xx != null && var10xx.isOnline()) {
                                          var13.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
                                             true
                                          );
                                          Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                                             Player var6xxx = Bukkit.getPlayer(var14);
                                             Player var7xxx = Bukkit.getPlayer(var15);
                                             if (var6xxx != null && var6xxx.isOnline()) {
                                                var6xxx.getInventory().setContents(var16);
                                                var6xxx.getInventory().setArmorContents(var17);
                                                var6xxx.updateInventory();
                                             }

                                             if (var7xxx != null && var7xxx.isOnline()) {
                                                var7xxx.getInventory().setContents(var18);
                                                var7xxx.getInventory().setArmorContents(var19);
                                                var7xxx.updateInventory();
                                             }
                                          }, 3L);
                                          this.updateScoreboards(var13);
                                          this.startCountdown(
                                             var9xx,
                                             var10xx,
                                             var13,
                                             true,
                                             () -> this.matchFoundEffects
                                                   .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                                      var9xx, var10xx, var11, var13, null
                                                   )
                                          );
                                       } else {
                                          this.endMatch(var13, null);
                                       }
                                    }
                                 }
                              );
                              int var12x = this.cachedMaxFightDuration;
                              Bukkit.getScheduler()
                                 .runTaskLater(
                                    this.plugin,
                                    () -> {
                                       if (this.activeMatches.containsKey(var14)
                                          && !var13.ÖO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000thissuper()
                                          )
                                        {
                                          Player var4xx = Bukkit.getPlayer(var14);
                                          Player var5xx = Bukkit.getPlayer(var15);
                                          if (var4xx != null) {
                                             var4xx.sendMessage(Component.text("Match ended due to time limit").color(NamedTextColor.YELLOW));
                                          }

                                          if (var5xx != null) {
                                             var5xx.sendMessage(Component.text("Match ended due to time limit").color(NamedTextColor.YELLOW));
                                          }

                                          this.endMatch(var13, null);
                                       }
                                    },
                                    (long)(var12x * 60) * 20L
                                 );
                           } else {
                              if (var10x != null) {
                                 var10x.sendMessage(Component.text("Match cancelled - opponent disconnected").color(NamedTextColor.RED));
                              }

                              if (var11x != null) {
                                 var11x.sendMessage(Component.text("Match cancelled - opponent disconnected").color(NamedTextColor.RED));
                              }

                              this.endMatch(var13, null);
                           }
                        }
                     },
                     (long)this.matchFoundEffects
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                  );
            }
         }
      }
   }

   public int getActiveFightCount() {
      return this.activeMatches.size();
   }

   public int getActivePlayersInKit(String var1) {
      if (var1 != null && !var1.isBlank()) {
         String var2 = var1.toLowerCase(Locale.ROOT);
         String var3 = var2.startsWith("tier") ? var2.substring(4) : var2;
         String var4 = "tier" + var3;
         HashSet var5 = new HashSet();
         byte var6 = 0;

         for (g var8 : this.activeMatches.values()) {
            String var9 = var8.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
               .toLowerCase(Locale.ROOT);
            if ((var9.equals(var3) || var9.equals(var4)) && var5.add(var8)) {
               var6 += 2;
            }
         }

         return var6;
      } else {
         return 0;
      }
   }

   public int getRankedFightingCount(String var1) {
      String var2 = var1.toLowerCase();
      HashSet var3 = new HashSet();
      byte var4 = 0;

      for (g var6 : this.activeMatches.values()) {
         if (var6.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void()
            && var6.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
               .equalsIgnoreCase(var2)
            && var3.add(var6)) {
            var4 += 2;
         }
      }

      return var4;
   }

   public f getMatchFoundEffects() {
      return this.matchFoundEffects;
   }

   public boolean isFirstToEnabled() {
      return this.cachedFirstTo;
   }

   public void shutdown() {
      if (this.requestCleanupTask != null) {
         this.requestCleanupTask.cancel();
      }

      if (this.inactivityRevealTask != null) {
         this.inactivityRevealTask.cancel();
      }

      this.lastEngagementTimes.clear();
      this.revealedInactiveMatches.clear();
      this.duelScoreActionBarManager
         .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
      this.matchFoundEffects
         .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void();
   }

   private void sendAutoGGMessage(Player var1) {
      if (var1 != null && var1.isOnline()) {
         if (this.cachedAutoGGMessages != null && !this.cachedAutoGGMessages.isEmpty()) {
            String var2 = this.cachedAutoGGMessages.get(ThreadLocalRandom.current().nextInt(this.cachedAutoGGMessages.size()));
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
               if (var1.isOnline()) {
                  if (this.plugin.isDebug()) {
                     this.plugin.getLogger().info("[Duel Debug] Auto-GG dispatch player=" + var1.getName() + " msg=\"" + var2 + "\"");
                  }

                  this.dispatchAutoGG(var1, var2);
               }
            }, 5L);
         } else {
            if (this.plugin.isDebug()) {
               this.plugin.getLogger().warning("[Duel Debug] Auto-GG has no cached messages; player=" + var1.getName());
            }
         }
      }
   }

   private void dispatchAutoGG(Player var1, String var2) {
      boolean var3 = false;

      try {
         var1.chat(var2);
         var3 = true;
         if (this.plugin.isDebug()) {
            this.plugin.getLogger().info("[Duel Debug] Auto-GG player.chat() invoked for " + var1.getName());
         }
      } catch (Throwable var8) {
         if (this.plugin.isDebug()) {
            this.plugin
               .getLogger()
               .warning("[Duel Debug] Auto-GG player.chat() threw for " + var1.getName() + ": " + var8.getMessage() + " — using broadcast fallback");
         }
      }

      if (!var3) {
         try {
            Component var4 = var1.displayName().append(Component.text(": ")).append(Component.text(var2));

            for (Player var6 : Bukkit.getOnlinePlayers()) {
               var6.sendMessage(var4);
            }

            Bukkit.getConsoleSender().sendMessage(var4);
         } catch (Throwable var7) {
            this.plugin.getLogger().warning("[Duel Debug] Auto-GG fallback broadcast failed for " + var1.getName() + ": " + var7.getMessage());
         }
      }
   }

   public void setRematchData(UUID var1, UUID var2, String var3) {
      this.rematchData.put(var1, new DuelManager._b(var2, var3, System.currentTimeMillis()));
   }

   public DuelManager._b getRematchData(UUID var1) {
      return this.rematchData.get(var1);
   }

   public void clearRematchData(UUID var1) {
      this.cancelRematchExpireTask(var1);
      this.rematchData.remove(var1);
      this.removeRematchItemFromInventory(var1);
   }

   private boolean getKitRule(String var1, KitRule var2) {
      if (var1 != null && var1.startsWith("customkit") && this.plugin.getCustomKitAPI() != null) {
         for (UUID var4 : this.activeMatches.keySet()) {
            CustomKitAPI.CustomKitData var5 = this.plugin.getCustomKitAPI().getKit(var4);
            if (var5 != null && var5.rules() != null) {
               return var5.rules().getOrDefault(var2, var2.getDefaultValue());
            }
         }
      }

      return this.plugin
         .getDataManager()
         .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
            var1
         )
         .getOrDefault(var2, var2.getDefaultValue());
   }

   private boolean isOnCrossServerArena() {
      if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
         org.lime.swiftCore.v.t.c var1 = this.plugin
            .getLimboManager()
            .getConfigCache()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
         return var1 != null
            && var1.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
               == org.lime.swiftCore.v.p.e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
      } else {
         return false;
      }
   }

   private CompletableFuture<Boolean> sendPlayerToLobby(Player var1) {
      CompletableFuture var2 = new CompletableFuture();
      this.sendPlayerToLobby(var1, 0, var2);
      return var2;
   }

   private void sendPlayerToLobby(Player var1, int var2, CompletableFuture<Boolean> var3) {
      if (var1 == null || !var1.isOnline()) {
         var3.complete(false);
      } else if (this.isOnCrossServerArena()) {
         org.lime.swiftCore.v.f.g var4 = this.plugin
            .getLimboManager()
            .getTransferManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1
            );
         if (var4
            == org.lime.swiftCore.v.f.g.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
            )
          {
            var3.complete(true);
         } else {
            this.retryLobbyReturn(var1, var2, var3);
         }
      } else {
         this.plugin
            .getLobbyManager()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var1
            )
            .whenComplete((var4x, var5) -> {
               if (var5 == null && Boolean.TRUE.equals(var4x)) {
                  var3.complete(true);
               } else {
                  Bukkit.getScheduler().runTask(this.plugin, () -> this.retryLobbyReturn(var1, var2, var3));
               }
            });
      }
   }

   private void retryLobbyReturn(Player var1, int var2, CompletableFuture<Boolean> var3) {
      if (var2 < 2 && var1.isOnline()) {
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.sendPlayerToLobby(var1, var2 + 1, var3), 20L);
      } else {
         this.plugin.getLogger().warning("Could not return " + var1.getName() + " to a lobby after " + (var2 + 1) + " attempts.");
         var3.complete(false);
      }
   }

   private void forceCloseEditorIfEditing(Player var1) {
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
         this.plugin.getScoreboardManager().setState(var1, ScoreboardState.DEFAULT);
         this.plugin.getScoreboardManager().setPlaceholder(var1, "editing_kit", "false");
         this.plugin.getScoreboardManager().setPlaceholder(var1, "kit", "");
      }
   }

   private boolean shouldTransferCrossServer() {
      if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
         org.lime.swiftCore.v.t.c var1 = this.plugin
            .getLimboManager()
            .getConfigCache()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
         return var1 != null
            && var1.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
               == org.lime.swiftCore.v.p.e.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class;
      } else {
         return false;
      }
   }

   private boolean transferDuelCrossServer(Player var1, Player var2, String var3, int var4, boolean var5) {
      org.lime.swiftCore.v.p.b var6 = this.plugin
         .getLimboManager()
         .getServerRegistry()
         .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      if (var6 == null) {
         var1.sendMessage(Component.text("No arena servers available!").color(NamedTextColor.RED));
         var2.sendMessage(Component.text("No arena servers available!").color(NamedTextColor.RED));
         return false;
      } else {
         Map var7 = Map.of("opponent", this.focusOpponentName(var1, var2), "kit", var3);
         Map var8 = Map.of("opponent", this.focusOpponentName(var2, var1), "kit", var3);
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "duel-accepted", var7
            );
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var2, "duel-accepted", var8
            );
         this.matchFoundEffects
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, var2, var3
            );
         int var9 = this.matchFoundEffects
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
         String var10 = var6.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String();
         UUID var11 = var1.getUniqueId();
         UUID var12 = var2.getUniqueId();
         Bukkit.getScheduler()
            .runTaskLater(
               this.plugin,
               () -> {
                  Player var7x = Bukkit.getPlayer(var11);
                  Player var8x = Bukkit.getPlayer(var12);
                  this.matchFoundEffects
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var7x, var8x
                     );
                  if (var7x != null && var7x.isOnline() && var8x != null && var8x.isOnline()) {
                     org.lime.swiftCore.v.t.c var9x = this.plugin
                        .getLimboManager()
                        .getConfigCache()
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
                     org.lime.swiftCore.v.f.b var10x = new org.lime.swiftCore.v.f.b(
                        org.lime.swiftCore.v.f.b.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(),
                        org.lime.swiftCore.v.f.c.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return,
                        var3,
                        List.of(var11, var12),
                        null,
                        null,
                        var4,
                        var5,
                        var9x.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(),
                        null,
                        null
                     );
                     this.plugin
                        .getLimboManager()
                        .getMatchIntentManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var10x
                        );
                     this.plugin
                        .getLimboManager()
                        .getMatchIntentManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var11,
                           var7x.getName(),
                           var10x.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                        );
                     this.plugin
                        .getLimboManager()
                        .getMatchIntentManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var12,
                           var8x.getName(),
                           var10x.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                        );
                     this.plugin
                        .getLimboManager()
                        .getMatchIntentManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var11,
                           var9x.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
                        );
                     this.plugin
                        .getLimboManager()
                        .getMatchIntentManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var12,
                           var9x.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
                        );
                     this.plugin
                        .getLimboManager()
                        .getTransferManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var7x, var10
                        );
                     this.plugin
                        .getLimboManager()
                        .getTransferManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var8x, var10
                        );
                  } else {
                     if (var7x != null && var7x.isOnline()) {
                        var7x.sendMessage(Component.text("Match cancelled - opponent disconnected.").color(NamedTextColor.RED));
                     }

                     if (var8x != null && var8x.isOnline()) {
                        var8x.sendMessage(Component.text("Match cancelled - opponent disconnected.").color(NamedTextColor.RED));
                     }
                  }
               },
               (long)var9
            );
         return true;
      }
   }

   public static record _b(
      UUID o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super,
      String Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new,
      long Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
   ) {
      public UUID Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new() {
         return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
      }

      public String Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object() {
         return this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
      }

      public long o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super() {
         return this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object;
      }
   }
}
