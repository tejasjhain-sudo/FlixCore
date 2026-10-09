package org.lime.swiftCore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.lime.swiftCore.ab.b;
import org.lime.swiftCore.ab.d;
import org.lime.swiftCore.api.ApiServer;
import org.lime.swiftCore.api.CustomKitAPI;
import org.lime.swiftCore.api.SwiftCoreApi;
import org.lime.swiftCore.api.SwiftCoreApiProvider;
import org.lime.swiftCore.arena.ArenaType;
import org.lime.swiftCore.arena.h;
import org.lime.swiftCore.arena.k;
import org.lime.swiftCore.b.l;
import org.lime.swiftCore.bb.lb;
import org.lime.swiftCore.bb.mb;
import org.lime.swiftCore.bb.nb;
import org.lime.swiftCore.bb.ob;
import org.lime.swiftCore.bb.pb;
import org.lime.swiftCore.bb.qb;
import org.lime.swiftCore.bb.rb;
import org.lime.swiftCore.bb.sb;
import org.lime.swiftCore.bb.w;
import org.lime.swiftCore.bb.y;
import org.lime.swiftCore.duel.DuelManager;
import org.lime.swiftCore.e.ab;
import org.lime.swiftCore.e.bb;
import org.lime.swiftCore.e.cb;
import org.lime.swiftCore.e.db;
import org.lime.swiftCore.e.eb;
import org.lime.swiftCore.e.fb;
import org.lime.swiftCore.e.gb;
import org.lime.swiftCore.e.hb;
import org.lime.swiftCore.e.ib;
import org.lime.swiftCore.e.jb;
import org.lime.swiftCore.e.kb;
import org.lime.swiftCore.e.p;
import org.lime.swiftCore.e.t;
import org.lime.swiftCore.e.u;
import org.lime.swiftCore.e.z;
import org.lime.swiftCore.ffa.FFAManager;
import org.lime.swiftCore.kit.QueueManager;
import org.lime.swiftCore.kit.e;
import org.lime.swiftCore.kit.i;
import org.lime.swiftCore.kit.j;
import org.lime.swiftCore.kit.n;
import org.lime.swiftCore.kit.o;
import org.lime.swiftCore.kit.q;
import org.lime.swiftCore.kit.r;
import org.lime.swiftCore.kit.s;
import org.lime.swiftCore.kit.v;
import org.lime.swiftCore.kit.x;
import org.lime.swiftCore.libs.authguard.sdk.AuthGuard;
import org.lime.swiftCore.libs.authguard.sdk.VerificationResult;
import org.lime.swiftCore.libs.lightcore.api.StartupMessage;
import org.lime.swiftCore.party.PartyGameManager;
import org.lime.swiftCore.party.PartyManager;
import org.lime.swiftCore.ranked.RankedManager;
import org.lime.swiftCore.scoreboard.ScoreboardManager;
import org.lime.swiftCore.scoreboard.ScoreboardState;
import org.lime.swiftCore.spawn.SpawnItemsManager;
import org.lime.swiftCore.tablist.TablistManager;
import org.lime.swiftCore.x.f;
import org.lime.swiftCore.x.g;
import org.lime.swiftCore.x.m;
import org.lime.swiftCore.y.c;

public final class SwiftCore extends JavaPlugin {
   private c databaseManager;
   private b kitStatsManager;
   private d kitStatsGUI;
   private org.lime.swiftCore.t.b dataManager;
   private e kitManager;
   private i multiQueueManager;
   private QueueManager queueManager;
   private q queueMatchTimeTracker;
   private s kitEditor;
   private j customItemsManager;
   private f editorGUI;
   private ScoreboardManager scoreboardManager;
   private BukkitTask arenaAutoResetTask;
   private BukkitTask guiUpdaterTask;
   private BukkitTask faweCleanupTask;
   private org.lime.swiftCore.arena.i arenaManager;
   private org.lime.swiftCore.k.b fenceWandManager;
   private h arenaCopyPasteService;
   private org.lime.swiftCore.arena.b dynamicArenaManager;
   private org.lime.swiftCore.arena.j arenaElevatorManager;
   private k ballisticEntryManager;
   private DuelManager duelManager;
   private org.lime.swiftCore.duel.h botDuelManager;
   private g duelGUI;
   private org.lime.swiftCore.x.c queueGUI;
   private org.lime.swiftCore.x.b teamQueueGUI;
   private org.lime.swiftCore.arena.e arenaResetManager;
   private org.lime.swiftCore.ab.c statsManager;
   private org.lime.swiftCore.ab.f killStreakManager;
   private l messagesManager;
   private SpawnItemsManager spawnItemsManager;
   private org.lime.swiftCore.spawn.b lobbyManager;
   private org.lime.swiftCore.spawn.c lobbyEffectManager;
   private ob kitRulesListener;
   private n hpIndicatorManager;
   private org.lime.swiftCore.x.l kitRuleGUI;
   private FFAManager ffaManager;
   private org.lime.swiftCore.ffa.b ffaGUI;
   private org.lime.swiftCore.ffa.d ffaCombatManager;
   private org.lime.swiftCore.ffa.c randomFFAInventoryManager;
   private org.lime.swiftCore.h.b spectatorManager;
   private PartyManager partyManager;
   private PartyGameManager partyGameManager;
   private org.lime.swiftCore.fb.d teamQueueManager;
   private org.lime.swiftCore.party.b.b partySettingsGUI;
   private org.lime.swiftCore.party.b.e partyManageGUI;
   private org.lime.swiftCore.party.b.d partyInfoGUI;
   private org.lime.swiftCore.x.k partyDisbandGUI;
   private org.lime.swiftCore.cb.b playerSettingsManager;
   private org.lime.swiftCore.cb.d playerSettingsGUI;
   private BukkitTask partyBroadcastTask;
   private boolean cachedPartyBroadcastEnabled;
   private int cachedPartyBroadcastIntervalSeconds;
   private w arenaListener;
   private y editorListener;
   private lb lobbyDuelInteractListener;
   private RankedManager rankedManager;
   private org.lime.swiftCore.ranked.d eloHistoryManager;
   private org.lime.swiftCore.ranked.b kitsRankManager;
   private org.lime.swiftCore.b.i killMessageManager;
   private org.lime.swiftCore.n.c killEffectManager;
   private org.lime.swiftCore.n.b killEffectGUI;
   private org.lime.swiftCore.d.b announcementMessageManager;
   private org.lime.swiftCore.d.c joinMessageGUI;
   private org.lime.swiftCore.d.c leaveMessageGUI;
   private org.lime.swiftCore.d.d announcementMessageService;
   private org.lime.swiftCore.j.c killSoundManager;
   private org.lime.swiftCore.j.b killSoundGUI;
   private org.lime.swiftCore.x.i modeSelectionGUI;
   private org.lime.swiftCore.x.h botDifficultyGUI;
   private org.lime.swiftCore.x.n rankedKitGUI;
   private org.lime.swiftCore.x.e rankedQueueGUI;
   private v randomKitManager;
   private org.lime.swiftCore.kit.b tntTagManager;
   private r flowerCrownManager;
   private TablistManager tablistManager;
   private org.lime.swiftCore.s.b chunkyIntegration;
   private org.lime.swiftCore.kit.k armorTrimManager;
   private org.lime.swiftCore.x.j armorTrimGUI;
   private x shieldDesignManager;
   private org.lime.swiftCore.x.d shieldEditorGUI;
   private org.lime.swiftCore.kit.c prePotionsManager;
   private org.lime.swiftCore.v.c limboManager;
   private org.lime.swiftCore.kit.l kitStateResolver;
   private m duelRequestGUI;
   private org.lime.swiftCore.r.b skyWarsLootManager;
   private o tntSumoManager;
   private org.lime.swiftCore.h.e spectatorPlayersGUI;
   private org.lime.swiftCore.h.d spectatePlayersGUI;
   private org.lime.swiftCore.party.b.f partyListGUIConfig;
   private org.lime.swiftCore.z.b parkourManager;
   private org.lime.swiftCore.f.c signQueueManager;
   private Object economy;
   private org.lime.swiftCore.l.b economyManager;
   private ApiServer apiServer;
   private CustomKitAPI customKitAPI;
   private org.lime.swiftCore.c.b deathAnimationManager;
   private org.lime.swiftCore.c.c deathAnimationGUI;
   private org.lime.swiftCore.i.b trailEffectManager;
   private org.lime.swiftCore.i.c trailEffectGUI;
   private org.lime.swiftCore.eb.b tournamentManager;
   private org.lime.swiftCore.hb.c eventManager;
   private org.lime.swiftCore.g.d clanManager;
   private org.lime.swiftCore.g.b.b clanGUI;
   private org.lime.swiftCore.party.b.c openPartyGUI;
   private org.lime.swiftCore.u.d matchHistoryManager;
   private org.lime.swiftCore.u.b matchHistoryGUI;
   private org.lime.swiftCore.x.b.c menuConfigRegistry;
   private org.lime.swiftCore.db.d friendManager;
   private org.lime.swiftCore.w.b openChallengeManager;
   private SwiftCoreApi swiftCoreApi;
   private org.lime.swiftCore.q.b pillarsOfFortuneManager;
   private org.lime.swiftCore.p.b lavaRaiseManager;
   private Set<String> disabledWorldsCache = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
   private boolean debug;

   public void onEnable() {
      this.saveDefaultConfig();
      this.saveMenuResources();
      String var1;
      if (!"%%__MyProduct_License__%%".contains("%%__")) {
         var1 = "%%__MyProduct_License__%%";
         this.saveLicenseKeyToConfig(var1);
         this.getLogger().info("License key auto-injected by BuiltByBit");
      } else {
         var1 = this.extractLicenseKey();
      }

      VerificationResult var2 = AuthGuard.verify(this, var1, "6", "SwiftCore", "http://103.180.237.87");
      if (!var2.isValid()) {
         String var3 = var2.getMessage();
         if (var3 == null || !var3.contains("connect") && !var3.contains("timeout") && !var3.contains("unreachable") && !var3.contains("Connection")) {
            this.getLogger().severe("License validation failed: " + (var3 != null ? var3 : "Invalid license"));
            this.getServer().getPluginManager().disablePlugin(this);
            return;
         }

         this.getLogger().warning("License server unreachable - running in offline mode");
      }

      if (!this.validateConfigYaml()) {
         this.getLogger().severe("config.yml has YAML syntax errors (tabs, bad indentation, etc.)!");
         this.getLogger().severe("Please fix the config.yml and restart. The plugin will not load with a broken config.");
         this.getServer().getPluginManager().disablePlugin(this);
      } else {
         this.reloadDisabledWorlds();
         StartupMessage.printWithAscii("SwiftCore", "&#FFD700");
         this.getLogger()
            .info(
               org.lime.swiftCore.m.b.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
            );
         this.logVersionCompat();
         this.initializePlugin();
      }
   }

   private String extractLicenseKey() {
      File var1 = new File(this.getDataFolder(), "config.yml");
      if (!var1.exists()) {
         return "YOUR_LICENSE_KEY_HERE";
      } else {
         try {
            String var6;
            try (BufferedReader var2 = new BufferedReader(new FileReader(var1))) {
               String var4;
               do {
                  String var3;
                  if ((var3 = var2.readLine()) == null) {
                     return "YOUR_LICENSE_KEY_HERE";
                  }

                  var4 = var3.trim();
               } while (!var4.startsWith("license-key:"));

               String var5 = var4.substring("license-key:".length()).trim();
               if (var5.startsWith("\"") && var5.endsWith("\"")) {
                  var5 = var5.substring(1, var5.length() - 1);
               } else if (var5.startsWith("'") && var5.endsWith("'")) {
                  var5 = var5.substring(1, var5.length() - 1);
               }

               var6 = var5.isEmpty() ? "YOUR_LICENSE_KEY_HERE" : var5;
            }

            return var6;
         } catch (Exception var9) {
            this.getLogger().warning("Could not read license key from config file: " + var9.getMessage());
            return "YOUR_LICENSE_KEY_HERE";
         }
      }
   }

   private void saveLicenseKeyToConfig(String var1) {
      File var2 = new File(this.getDataFolder(), "config.yml");
      if (var2.exists()) {
         try {
            List var3 = Files.readAllLines(var2.toPath());
            boolean var4 = false;

            for (int var5 = 0; var5 < var3.size(); var5++) {
               String var6 = ((String)var3.get(var5)).trim();
               if (var6.startsWith("license-key:")) {
                  var3.set(var5, "license-key: \"" + var1 + "\"");
                  var4 = true;
                  break;
               }
            }

            if (!var4) {
               var3.add(0, "license-key: \"" + var1 + "\"");
            }

            Files.write(var2.toPath(), var3);
         } catch (Exception var7) {
            this.getLogger().warning("Could not save license key to config: " + var7.getMessage());
         }
      }
   }

   private boolean validateConfigYaml() {
      File var1 = new File(this.getDataFolder(), "config.yml");
      if (!var1.exists()) {
         return true;
      } else {
         try {
            YamlConfiguration var2 = new YamlConfiguration();
            var2.load(var1);
            return true;
         } catch (InvalidConfigurationException var3) {
            this.getLogger().severe("YAML Error: " + var3.getMessage());
            return false;
         } catch (IOException var4) {
            this.getLogger().severe("Could not read config.yml: " + var4.getMessage());
            return false;
         }
      }
   }

   private void initializePlugin() {
      this.databaseManager = new c(this);
      org.lime.swiftCore.v.t.c var1 = org.lime.swiftCore.v.t.b.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this
      );
      CompletableFuture var2 = var1.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
         ? this.databaseManager
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, this.getDataFolder()
            )
         : this.databaseManager
            .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return();
      var2.thenRun(
            () -> this.getServer()
                  .getScheduler()
                  .runTask(
                     this,
                     () -> {
                        this.dataManager = new org.lime.swiftCore.t.b(this, this.databaseManager);
                        this.menuConfigRegistry = new org.lime.swiftCore.x.b.c(this);
                        this.statsManager = new org.lime.swiftCore.ab.c(this, this.databaseManager);
                        this.statsManager
                           .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
                        this.kitStatsManager = new b(this, this.databaseManager);
                        this.kitStatsGUI = new d(this);
                        this.killStreakManager = new org.lime.swiftCore.ab.f(this);
                        this.messagesManager = new l(this);
                        this.killMessageManager = new org.lime.swiftCore.b.i(this);
                        this.spawnItemsManager = new SpawnItemsManager(this);
                        this.lobbyManager = new org.lime.swiftCore.spawn.b(this);
                        this.kitRuleGUI = new org.lime.swiftCore.x.l(this);
                        this.multiQueueManager = new i(this);
                        this.queueManager = new QueueManager(this);
                        this.queueMatchTimeTracker = new q(this);
                        this.kitEditor = new s();
                        this.kitManager = new e(this, this.dataManager);
                        this.customItemsManager = new j(this);
                        this.randomKitManager = new v(this);
                        this.editorGUI = new f(this);
                        this.queueGUI = new org.lime.swiftCore.x.c(this);
                        this.scoreboardManager = new ScoreboardManager(this);
                        this.tablistManager = new TablistManager(this);
                        this.arenaResetManager = new org.lime.swiftCore.arena.e(this);
                        this.arenaElevatorManager = new org.lime.swiftCore.arena.j(this);
                        this.ballisticEntryManager = new k(this);
                        this.arenaManager = new org.lime.swiftCore.arena.i(this);
                        this.fenceWandManager = new org.lime.swiftCore.k.b(this);
                        this.arenaCopyPasteService = new h(this, this.arenaManager);
                        this.dynamicArenaManager = new org.lime.swiftCore.arena.b(this, this.arenaManager, this.arenaCopyPasteService);
                        this.arenaElevatorManager
                           .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String();
                        this.arenaResetManager
                           .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String();
                        this.chunkyIntegration = new org.lime.swiftCore.s.b(this);
                        this.chunkyIntegration
                           .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class();
                        this.duelManager = new DuelManager(this, this.arenaManager, this.statsManager);
                        this.botDuelManager = new org.lime.swiftCore.duel.h(this, this.arenaManager);
                        this.duelGUI = new g(this);
                        this.duelRequestGUI = new m(this);
                        this.skyWarsLootManager = new org.lime.swiftCore.r.b(this);
                        this.ffaManager = new FFAManager(this, this.arenaManager, this.kitManager);
                        this.ffaGUI = new org.lime.swiftCore.ffa.b(this);
                        this.ffaCombatManager = new org.lime.swiftCore.ffa.d(this);
                        this.randomFFAInventoryManager = new org.lime.swiftCore.ffa.c(this, this.databaseManager);
                        this.spectatorManager = new org.lime.swiftCore.h.b(this, this.arenaManager);
                        this.spectatorPlayersGUI = new org.lime.swiftCore.h.e(this);
                        this.spectatePlayersGUI = new org.lime.swiftCore.h.d(this);
                        this.partyManager = new PartyManager(this);
                        this.partyGameManager = new PartyGameManager(this, this.partyManager);
                        this.teamQueueManager = new org.lime.swiftCore.fb.d(this);
                        this.teamQueueGUI = new org.lime.swiftCore.x.b(this);
                        this.setupTeamQueueEffects();
                        this.partySettingsGUI = new org.lime.swiftCore.party.b.b(this, this.partyManager);
                        this.partyManageGUI = new org.lime.swiftCore.party.b.e(this, this.partyManager);
                        this.partyInfoGUI = new org.lime.swiftCore.party.b.d(this, this.partyManager);
                        this.partyDisbandGUI = new org.lime.swiftCore.x.k(this);
                        this.partyListGUIConfig = new org.lime.swiftCore.party.b.f(this);
                        this.playerSettingsManager = new org.lime.swiftCore.cb.b(this);
                        this.playerSettingsGUI = new org.lime.swiftCore.cb.d(this, this.playerSettingsManager);
                        this.rankedManager = new RankedManager(this, this.databaseManager);
                        this.eloHistoryManager = new org.lime.swiftCore.ranked.d(this);
                        this.kitsRankManager = new org.lime.swiftCore.ranked.b(this);
                        this.kitsRankManager
                           .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
                        this.killEffectManager = new org.lime.swiftCore.n.c(this);
                        this.killEffectGUI = new org.lime.swiftCore.n.b(this, this.killEffectManager);
                        this.announcementMessageManager = new org.lime.swiftCore.d.b(this);
                        this.joinMessageGUI = new org.lime.swiftCore.d.c(
                           this,
                           this.announcementMessageManager,
                           org.lime.swiftCore.d.b._b.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                        );
                        this.leaveMessageGUI = new org.lime.swiftCore.d.c(
                           this,
                           this.announcementMessageManager,
                           org.lime.swiftCore.d.b._b.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
                        );
                        this.announcementMessageService = new org.lime.swiftCore.d.d(this, this.announcementMessageManager);
                        this.killSoundManager = new org.lime.swiftCore.j.c(this);
                        this.killSoundGUI = new org.lime.swiftCore.j.b(this, this.killSoundManager);
                        this.modeSelectionGUI = new org.lime.swiftCore.x.i(this);
                        this.botDifficultyGUI = new org.lime.swiftCore.x.h(this);
                        this.rankedKitGUI = new org.lime.swiftCore.x.n(this);
                        this.rankedQueueGUI = new org.lime.swiftCore.x.e(this);
                        this.tntTagManager = new org.lime.swiftCore.kit.b(this);
                        this.tntSumoManager = new o(this);
                        this.flowerCrownManager = new r(this);
                        this.armorTrimManager = new org.lime.swiftCore.kit.k(this);
                        this.armorTrimGUI = new org.lime.swiftCore.x.j(this);
                        this.shieldDesignManager = new x(this);
                        this.shieldEditorGUI = new org.lime.swiftCore.x.d(this, this.shieldDesignManager);
                        this.prePotionsManager = new org.lime.swiftCore.kit.c(this);
                        this.limboManager = new org.lime.swiftCore.v.c(this);
                        this.limboManager.initialize();
                        this.kitStateResolver = new org.lime.swiftCore.kit.l(this);
                        this.parkourManager = new org.lime.swiftCore.z.b(this);
                        this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.z.c(this), this);
                        this.arenaListener = new w(this, this.arenaManager, this.duelManager, this.duelGUI);
                        this.signQueueManager = new org.lime.swiftCore.f.c(this);
                        this.customKitAPI = new CustomKitAPI();
                        this.deathAnimationManager = new org.lime.swiftCore.c.b(this);
                        this.deathAnimationGUI = new org.lime.swiftCore.c.c(this, this.deathAnimationManager);
                        this.trailEffectManager = new org.lime.swiftCore.i.b(this);
                        this.trailEffectGUI = new org.lime.swiftCore.i.c(this, this.trailEffectManager);
                        this.tournamentManager = new org.lime.swiftCore.eb.b(this);
                        this.eventManager = new org.lime.swiftCore.hb.c(this);
                        if (this.isClansEnabled()) {
                           this.clanManager = new org.lime.swiftCore.g.d(this);
                           this.clanGUI = new org.lime.swiftCore.g.b.b(this);
                        } else {
                           this.unregisterClanCommand();
                           this.getLogger().info("Clan system is completely disabled.");
                        }

                        this.openPartyGUI = new org.lime.swiftCore.party.b.c(this);
                        this.matchHistoryManager = new org.lime.swiftCore.u.d(this);
                        this.matchHistoryGUI = new org.lime.swiftCore.u.b(this);
                        this.friendManager = new org.lime.swiftCore.db.d(this, this.databaseManager);
                        this.friendManager
                           .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
                        this.openChallengeManager = new org.lime.swiftCore.w.b(this);
                        this.lobbyEffectManager = new org.lime.swiftCore.spawn.c(this);
                        this.pillarsOfFortuneManager = new org.lime.swiftCore.q.b(this);
                        this.lavaRaiseManager = new org.lime.swiftCore.p.b(this);
                        this.setupQueueMatchmaking();
                        this.kitManager
                           .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
                        this.getLogger()
                           .info(
                              "Loaded "
                                 + this.kitManager
                                    .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
                                    .size()
                                 + " kits"
                           );
                        this.registerCommands();
                        this.registerListeners();
                        this.registerPlaceholders();
                        this.startScoreboard();
                        this.startTablist();
                        this.startArenaAutoReset();
                        this.startPartyBroadcast();
                        this.startGUIUpdater();
                        this.startFaweCleanup();
                        this.setupEconomy();
                        this.economyManager = new org.lime.swiftCore.l.b(this);
                        this.signQueueManager
                           .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void();
                        this.swiftCoreApi = new SwiftCoreApiProvider(this);
                        this.getServer().getServicesManager().register(SwiftCoreApi.class, this.swiftCoreApi, this, ServicePriority.Normal);
                        this.handleAlreadyOnlinePlayers();
                        this.apiServer = new ApiServer(this);
                        this.apiServer.start();
                        this.getLogger().info("SwiftCore has been enabled!");
                     }
                  )
         )
         .exceptionally(var1x -> {
            this.getServer().getScheduler().runTask(this, () -> {
               this.getLogger().severe("Failed to initialize database: " + var1x.getMessage());
               this.getServer().getPluginManager().disablePlugin(this);
            });
            return null;
         });
   }

   public void onDisable() {
      for (Player var2 : Bukkit.getOnlinePlayers()) {
         var2.setGlowing(false);
         if (this.spectatorManager != null
            && this.spectatorManager
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                  var2.getUniqueId()
               )) {
            var2.setGameMode(GameMode.SURVIVAL);
            var2.setAllowFlight(false);
            var2.setFlying(false);
            var2.setInvulnerable(false);
            var2.removePotionEffect(PotionEffectType.INVISIBILITY);
            var2.getInventory().clear();
         }
      }

      if (this.tablistManager != null) {
         this.tablistManager.stopUpdater();
      }

      if (this.botDuelManager != null) {
         try {
            this.botDuelManager.shutdown();
         } catch (Throwable var4) {
         }
      }

      if (this.scoreboardManager != null) {
         this.scoreboardManager.shutdown();
      }

      if (this.fenceWandManager != null) {
         this.fenceWandManager
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
      }

      if (this.lobbyEffectManager != null) {
         this.lobbyEffectManager
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
      }

      if (this.pillarsOfFortuneManager != null) {
         this.pillarsOfFortuneManager
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      }

      if (this.arenaAutoResetTask != null) {
         this.arenaAutoResetTask.cancel();
      }

      if (this.arenaManager != null) {
         this.arenaManager
            .ÕO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interface();
      }

      if (this.partyBroadcastTask != null) {
         this.partyBroadcastTask.cancel();
      }

      if (this.guiUpdaterTask != null) {
         this.guiUpdaterTask.cancel();
      }

      if (this.faweCleanupTask != null) {
         this.faweCleanupTask.cancel();
      }

      if (this.queueManager != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
         new org.lime.swiftCore.o.c(this, this.queueManager).unregister();
         new org.lime.swiftCore.o.b(this).unregister();
      }

      if (this.signQueueManager != null) {
         this.signQueueManager
            .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class();
      }

      if (this.statsManager != null) {
         this.statsManager
            .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return();
      }

      if (this.rankedManager != null) {
         this.rankedManager.saveData();
      }

      if (this.kitsRankManager != null) {
         this.kitsRankManager
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
         this.kitsRankManager
            .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null();
      }

      if (this.duelManager != null) {
         this.duelManager.shutdown();
      }

      if (this.ballisticEntryManager != null) {
         this.ballisticEntryManager
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
      }

      if (this.arenaElevatorManager != null) {
         this.arenaElevatorManager
            .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void();
      }

      if (this.ffaCombatManager != null) {
         this.ffaCombatManager
            .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class();
      }

      if (this.trailEffectManager != null) {
         this.trailEffectManager
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
      }

      if (this.tournamentManager != null) {
         this.tournamentManager
            .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float();
      }

      if (this.eventManager != null) {
         this.eventManager
            .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return();
      }

      if (this.clanManager != null) {
         this.clanManager
            .ÒO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000while();
      }

      if (this.friendManager != null) {
         this.friendManager
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
      }

      if (this.hpIndicatorManager != null) {
         this.hpIndicatorManager.shutdown();
      }

      if (this.apiServer != null) {
         this.apiServer.stop();
      }

      if (this.swiftCoreApi != null) {
         this.getServer().getServicesManager().unregister(SwiftCoreApi.class, this.swiftCoreApi);
         this.swiftCoreApi = null;
      }

      if (this.databaseManager != null) {
         this.databaseManager
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      }

      if (this.limboManager != null) {
         this.limboManager.shutdown();
      }

      if (this.lavaRaiseManager != null) {
         this.lavaRaiseManager.shutdown();
      }

      if (this.dynamicArenaManager != null) {
         this.dynamicArenaManager
            .OO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000for();
      }

      if (this.arenaResetManager != null && this.arenaManager != null) {
         int var5 = 0;

         for (org.lime.swiftCore.arena.d var3 : this.arenaManager
            .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()) {
            if (var3.ôÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000nullnew()
                  > 0
               && var3.Õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000floatsuper()
               && var3.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
                  == ArenaType.BUILD) {
               this.arenaResetManager
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var3, true
                  );
               var5++;
            }
         }

         if (var5 > 0) {
            this.getLogger().info("Reset " + var5 + " arenas with auto-reset enabled on shutdown");
         }

         this.arenaResetManager
            .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void();
      }

      this.cleanupFaweData();
      this.getLogger().info("SwiftCore has been disabled!");
   }

   private void cleanupFaweData() {
      File var1 = this.getDataFolder().getParentFile();
      File var2 = new File(var1, "FastAsyncWorldEdit");
      if (var2.exists()) {
         File var3 = new File(var2, "clipboard");
         int var4 = 0;
         if (var3.exists()) {
            var4 += this.deleteDirectoryContents(var3);
         }

         if (var4 > 0) {
            this.getLogger().info("Cleaned up " + var4 + " FastAsyncWorldEdit clipboard files");
         }
      }
   }

   private int deleteDirectoryContents(File var1) {
      int var2 = 0;
      File[] var3 = var1.listFiles();
      if (var3 != null) {
         for (File var7 : var3) {
            if (var7.isDirectory()) {
               var2 += this.deleteDirectoryContents(var7);
               var7.delete();
            } else if (var7.delete()) {
               var2++;
            }
         }
      }

      return var2;
   }

   private void saveMenuResources() {
      File var1 = new File(this.getDataFolder(), "menus");
      if (!var1.exists()) {
         var1.mkdirs();
      }

      File var2 = new File(this.getDataFolder(), "database");
      if (!var2.exists()) {
         var2.mkdirs();
      }

      File var3 = new File(this.getDataFolder(), "miscellaneous");
      if (!var3.exists()) {
         var3.mkdirs();
      }

      String[] var4 = new String[]{
         "messages.yml",
         "spawnitems.yml",
         "scoreboard.yml",
         "database/skywars-loot.yml",
         "miscellaneous/miscellaneous.yml",
         "miscellaneous/spritequeue.yml",
         "miscellaneous/lobbyeffect.yml",
         "miscellaneous/pillaroffortune.yml",
         "miscellaneous/entry.yml",
         "miscellaneous/dynamicarena.yml",
         "miscellaneous/multiqueue.yml",
         "miscellaneous/teamqueue.yml",
         "menus/arenaselect.yml",
         "menus/duelgui.yml",
         "menus/unrankedqueue.yml",
         "menus/teamqueue-size.yml",
         "menus/teamqueue-mode.yml",
         "menus/teamqueue-kits.yml",
         "menus/rounds.yml",
         "menus/kiteditor.yml",
         "menus/kit-custom-items.yml",
         "menus/kit-custom-items-admin.yml",
         "menus/partymanage.yml",
         "menus/partyinfo.yml",
         "menus/partysettings.yml",
         "menus/partyffa.yml",
         "menus/partysplit.yml",
         "menus/partysplitteams.yml",
         "menus/partyvs.yml",
         "menus/partylist.yml",
         "menus/playersettings.yml",
         "menus/partydisband.yml",
         "menus/killeffect.yml",
         "menus/joinmessage.yml",
         "menus/leavemessage.yml",
         "menus/killsound.yml",
         "menus/rankedkits.yml",
         "menus/rankedqueue.yml",
         "menus/armortrimselect.yml",
         "menus/armortrimpattern.yml",
         "menus/armortrimmaterial.yml",
         "menus/duelrequests.yml",
         "menus/spectatorplayers.yml",
         "menus/spectate.yml",
         "menus/partyvsparty.yml",
         "menus/deathanimation.yml",
         "menus/traileffect.yml",
         "menus/openparties.yml",
         "menus/matchhistory.yml",
         "menus/botdifficulty.yml",
         "menus/shieldeditor.yml",
         "menus/shieldcolors.yml",
         "menus/shieldpatterns.yml",
         "menus/kitstats.yml",
         "menus/ffa.yml",
         "menus/clanhub.yml",
         "menus/clanmembers.yml",
         "menus/clansettings.yml",
         "menus/clanrelations.yml"
      };

      for (String var8 : var4) {
         File var9 = new File(this.getDataFolder(), var8);
         if (!var9.exists()) {
            this.saveResource(var8, false);
         }
      }
   }

   private void registerCommands() {
      org.lime.swiftCore.e.v var1 = new org.lime.swiftCore.e.v(this, this.kitManager, this.kitEditor);
      org.lime.swiftCore.e.x var2 = new org.lime.swiftCore.e.x(this, this.kitManager, this.queueManager);
      eb var3 = new eb(this, this.queueManager);
      org.lime.swiftCore.e.q var4 = new org.lime.swiftCore.e.q(this, this.arenaManager);
      z var5 = new z(this, this.duelManager, this.duelGUI);
      org.lime.swiftCore.e.c var6 = new org.lime.swiftCore.e.c(this);
      org.lime.swiftCore.e.j var7 = new org.lime.swiftCore.e.j(this);
      org.lime.swiftCore.e.w var8 = new org.lime.swiftCore.e.w(this, this.arenaManager, this.ffaManager);
      org.lime.swiftCore.e.s var9 = new org.lime.swiftCore.e.s(this, this.ffaManager);
      ab var10 = new ab(this, this.spectatorManager);
      jb var11 = new jb(this, this.spectatorManager);
      org.lime.swiftCore.e.k var12 = new org.lime.swiftCore.e.k(this, this.partyManager);
      org.lime.swiftCore.e.i var13 = new org.lime.swiftCore.e.i(this, this.playerSettingsManager);
      org.lime.swiftCore.e.b var14 = new org.lime.swiftCore.e.b(this, this.rankedManager);
      db var15 = new db(this);
      org.lime.swiftCore.e.f var16 = new org.lime.swiftCore.e.f(this, this.killEffectManager, this.killEffectGUI);
      hb var17 = new hb(this.joinMessageGUI);
      hb var18 = new hb(this.leaveMessageGUI);
      org.lime.swiftCore.e.e var19 = new org.lime.swiftCore.e.e(this, this.killSoundGUI);
      bb var20 = new bb(this, this.kitManager, this.queueManager);
      org.lime.swiftCore.e.n var21 = new org.lime.swiftCore.e.n(this);
      t var22 = new t(this);
      kb var23 = new kb(this);
      org.lime.swiftCore.e.d var24 = new org.lime.swiftCore.e.d(this, this.fenceWandManager);
      org.lime.swiftCore.e.l var25 = new org.lime.swiftCore.e.l(this);
      this.getCommand("kit").setExecutor(var1);
      this.getCommand("kit").setTabCompleter(var1);
      this.getCommand("queue").setExecutor(var2);
      this.getCommand("queue").setTabCompleter(var2);
      this.getCommand("leavequeue").setExecutor(var3);
      this.getCommand("arena").setExecutor(var4);
      this.getCommand("arena").setTabCompleter(var4);
      this.getCommand("duel").setExecutor(var5);
      this.getCommand("duel").setTabCompleter(var5);
      this.getCommand("swiftcore").setExecutor(var6);
      this.getCommand("swiftcore").setTabCompleter(var6);
      this.getCommand("setlobby").setExecutor(var7);
      this.getCommand("ffa").setExecutor(var8);
      this.getCommand("ffa").setTabCompleter(var8);
      this.getCommand("leaveffa").setExecutor(var9);
      this.getCommand("spectator").setExecutor(var10);
      this.getCommand("spectator").setTabCompleter(var10);
      this.getCommand("spectate").setExecutor(var11);
      this.getCommand("spectate").setTabCompleter(var11);
      this.getCommand("p").setExecutor(var12);
      this.getCommand("p").setTabCompleter(var12);
      this.getCommand("party").setExecutor(var12);
      this.getCommand("party").setTabCompleter(var12);
      this.getCommand("settings").setExecutor(var13);
      this.getCommand("settings").setTabCompleter(var13);
      this.getCommand("ranked").setExecutor(var14);
      this.getCommand("ranked").setTabCompleter(var14);
      this.getCommand("surrender").setExecutor(var15);
      this.getCommand("killeffect").setExecutor(var16);
      this.getCommand("joinmessage").setExecutor(var17);
      this.getCommand("leavemessage").setExecutor(var18);
      this.getCommand("killsound").setExecutor(var19);
      this.getCommand("unranked").setExecutor(var20);
      this.getCommand("unranked").setTabCompleter(var20);
      this.getCommand("teamqueue").setExecutor(var21);
      this.getCommand("parkour").setExecutor(var22);
      this.getCommand("parkour").setTabCompleter(var22);
      this.getCommand("kitstats").setExecutor(var23);
      this.getCommand("kitstats").setTabCompleter(var23);
      this.getCommand("fwand").setExecutor(var24);
      this.getCommand("fwand").setTabCompleter(var24);
      this.getCommand("cwand").setExecutor(var25);
      this.getCommand("cwand").setTabCompleter(var25);
      ib var26 = new ib(this);
      this.getCommand("spawnitems").setExecutor(var26);
      this.getCommand("spawnitems").setTabCompleter(var26);
      cb var27 = new cb(this);
      this.getCommand("migrate").setExecutor(var27);
      this.getCommand("migrate").setTabCompleter(var27);
      u var28 = new u(this);
      this.getCommand("deathanimation").setExecutor(var28);
      org.lime.swiftCore.e.h var29 = new org.lime.swiftCore.e.h(this);
      this.getCommand("traileffect").setExecutor(var29);
      org.lime.swiftCore.e.y var30 = new org.lime.swiftCore.e.y(this);
      this.getCommand("tournament").setExecutor(var30);
      this.getCommand("tournament").setTabCompleter(var30);
      fb var31 = new fb(this);
      this.getCommand("event").setExecutor(var31);
      this.getCommand("event").setTabCompleter(var31);
      if (this.isClansEnabled()) {
         gb var32 = new gb(this);
         this.getCommand("clan").setExecutor(var32);
         this.getCommand("clan").setTabCompleter(var32);
      }

      org.lime.swiftCore.e.r var37 = new org.lime.swiftCore.e.r(this);
      this.getCommand("day").setExecutor(var37);
      this.getCommand("night").setExecutor(var37);
      this.getCommand("sunset").setExecutor(var37);
      p var33 = new p(this);
      this.getCommand("openparty").setExecutor(var33);
      org.lime.swiftCore.e.g var34 = new org.lime.swiftCore.e.g(this);
      this.getCommand("matchhistory").setExecutor(var34);
      this.getCommand("matchhistory").setTabCompleter(var34);
      org.lime.swiftCore.db.b var35 = new org.lime.swiftCore.db.b(this, this.friendManager);
      this.getCommand("friend").setExecutor(var35);
      this.getCommand("friend").setTabCompleter(var35);
      if (this.getCommand("bot") != null) {
         org.lime.swiftCore.e.m var36 = new org.lime.swiftCore.e.m(this);
         this.getCommand("bot").setExecutor(var36);
         this.getCommand("bot").setTabCompleter(var36);
      }

      org.lime.swiftCore.e.o var38 = new org.lime.swiftCore.e.o(this);
      this.getCommand("openchallenge").setExecutor(var38);
      this.getCommand("openchallenge").setTabCompleter(var38);
   }

   private void registerListeners() {
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.gb(this.menuConfigRegistry), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.s(this), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.o(this, this.kitEditor, this.kitManager), this);
      this.editorListener = new y(this, this.kitEditor, this.kitManager);
      this.getServer().getPluginManager().registerEvents(this.editorListener, this);
      if (this.shieldEditorGUI != null && this.shieldDesignManager != null) {
         this.getServer().getPluginManager().registerEvents(new qb(this, this.shieldEditorGUI, this.shieldDesignManager), this);
      }

      this.getServer().getPluginManager().registerEvents(new pb(this, this.scoreboardManager), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.k.c(this.fenceWandManager), this);
      this.getServer().getPluginManager().registerEvents(this.arenaListener, this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.c(this, this.kitEditor), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.cb(this, this.kitEditor), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.z(this, this.spawnItemsManager), this);
      this.getServer().getPluginManager().registerEvents(new mb(this, this.kitRuleGUI), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.f(this, this.customItemsManager), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.x(this, this.queueGUI), this);
      this.getServer().getPluginManager().registerEvents(new rb(this.teamQueueGUI), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.t(this.kitStatsGUI), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.k(this.ffaGUI), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.v(this, this.queueGUI, this.queueManager, this.messagesManager), this);
      this.getServer().getPluginManager().registerEvents(new nb(this, this.rankedQueueGUI), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.i(this, this.spectatorManager), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.q(this, this.spectatorPlayersGUI), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.j(this.spectatePlayersGUI), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.party.c.d(this, this.partyManager), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.party.c.c(this.partyManager, this.partyGameManager), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.party.c.b(this, this.partyManager), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.l(this, this.playerSettingsManager), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.u(this, this.playerSettingsManager), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.fb(this), this);
      this.kitRulesListener = new ob(this);
      this.getServer().getPluginManager().registerEvents(this.kitRulesListener, this);
      this.kitRulesListener.startSumoWaterCheckTask();
      this.hpIndicatorManager = new n(this);
      this.getServer().getPluginManager().registerEvents(this.hpIndicatorManager, this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.eb(this, this.killEffectManager), this);
      this.getServer()
         .getPluginManager()
         .registerEvents(new org.lime.swiftCore.bb.r(this, this.announcementMessageManager, this.joinMessageGUI, this.leaveMessageGUI), this);
      this.getServer().getPluginManager().registerEvents(this.announcementMessageService, this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.kb(this, this.killSoundManager, this.killSoundGUI), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.db(this, this.modeSelectionGUI, this.rankedKitGUI), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.n(this.botDifficultyGUI), this);
      this.getServer().getPluginManager().registerEvents(new sb(this), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.p(this), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.b(this), this);
      this.lobbyDuelInteractListener = new lb(this);
      this.getServer().getPluginManager().registerEvents(this.lobbyDuelInteractListener, this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.e(this, this.signQueueManager), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.h(this, this.deathAnimationManager), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.ib(this, this.trailEffectManager), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.jb(this, this.openPartyGUI), this);
      if (this.isClansEnabled()) {
         this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.hb(this), this);
         this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.g.c(this), this);
         this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.g.b.c(this, this.clanGUI), this);
      }

      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.bb(this), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.ab(this), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.m(), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.db.c(this, this.friendManager), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.db.f(this), this);
      this.getServer().getPluginManager().registerEvents(new org.lime.swiftCore.bb.d(this), this);
   }

   private void registerPlaceholders() {
      if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
         new org.lime.swiftCore.o.c(this, this.queueManager).register();
         new org.lime.swiftCore.o.b(this).register();
         this.getLogger().info("PlaceholderAPI hooked successfully!");
      } else {
         this.getLogger().warning("PlaceholderAPI not found! Placeholders will not work.");
      }
   }

   private void handleAlreadyOnlinePlayers() {
      for (Player var2 : Bukkit.getOnlinePlayers()) {
         if (this.friendManager != null) {
            this.friendManager
               .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                  var2.getUniqueId()
               );
         }

         if (this.isWorldDisabled(var2.getWorld())
            && this.lobbyManager
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
            )
          {
            this.lobbyManager
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var2
               );
         }

         var2.getInventory().clear();
         org.lime.swiftCore.b.b.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var2
         );
         var2.setFoodLevel(20);
         var2.setSaturation(5.0F);
         var2.setGlowing(false);
         this.scoreboardManager.setState(var2, ScoreboardState.DEFAULT);
         this.scoreboardManager
            .setPlaceholders(
               var2, Map.of("in_fight", "false", "in_party", "false", "kit", "", "opponent", "", "team_icon", "", "team_color", "", "in_queue_kitname", "")
            );
         if (!this.isWorldDisabled(var2.getWorld())
            && this.lobbyManager
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
            )
          {
            this.lobbyManager
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var2
               );
         }

         this.spawnItemsManager.giveSpawnItems(var2, "default", false, false);
         if (this.scoreboardManager.isEnabled()) {
            this.scoreboardManager.createScoreboard(var2);
         }

         if (this.tablistManager != null && this.tablistManager.isEnabled()) {
            this.tablistManager.createTablist(var2);
            this.tablistManager.onPlayerJoin(var2);
         }

         this.statsManager
            .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
               var2.getUniqueId()
            );
         this.rankedManager.preloadRankedDataAsync(var2.getUniqueId());
         if (this.kitsRankManager != null) {
            this.kitsRankManager
               .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(
                  var2.getUniqueId()
               );
         }

         this.getLogger().info("Handled already-online player: " + var2.getName());
      }
   }

   private void startScoreboard() {
      if (this.scoreboardManager.isEnabled()) {
         this.scoreboardManager.startRefreshTask();
         this.getLogger().info("Scoreboard system enabled!");
      }
   }

   private void startTablist() {
      if (this.tablistManager.isEnabled()) {
         this.tablistManager.startUpdater();
         this.getLogger().info("Tablist system enabled!");
      }
   }

   private void startArenaAutoReset() {
      org.lime.swiftCore.arena.g var1 = new org.lime.swiftCore.arena.g(this, this.arenaManager);
      int var2 = this.getConfig().getInt("arena.auto-reset-check-interval", 1200);
      this.arenaAutoResetTask = var1.runTaskTimerAsynchronously(this, (long)var2, (long)var2);
      this.getLogger().info("Arena auto-reset system started! (async, every " + var2 / 20 + "s)");
   }

   private void startPartyBroadcast() {
      this.cachePartyBroadcastConfig();
      if (!this.cachedPartyBroadcastEnabled) {
         this.getLogger().info("Party broadcast system disabled in config.");
      } else {
         int var1 = this.cachedPartyBroadcastIntervalSeconds * 20;
         org.lime.swiftCore.party.c var2 = new org.lime.swiftCore.party.c(this, this.partyManager);
         this.partyBroadcastTask = var2.runTaskTimerAsynchronously(this, (long)var1, (long)var1);
         this.getLogger().info("Party broadcast system started! (async, every " + this.cachedPartyBroadcastIntervalSeconds + "s)");
      }
   }

   private void cachePartyBroadcastConfig() {
      this.cachedPartyBroadcastEnabled = this.getConfig().getBoolean("party.broadcast.enabled", true);
      this.cachedPartyBroadcastIntervalSeconds = Math.max(
         10, this.getConfig().getInt("party.broadcast.interval-seconds", this.getConfig().getInt("party.broadcast-interval", 60))
      );
   }

   private void startFaweCleanup() {
      long var1 = (long)this.getConfig().getInt("fawe-cleanup-interval-minutes", 10) * 60L * 20L;
      this.faweCleanupTask = this.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> this.cleanupFaweData(), var1, var1);
      this.getLogger().info("FAWE clipboard cleanup scheduled every " + var1 / 1200L + " minutes");
   }

   private void startGUIUpdater() {
      this.guiUpdaterTask = this.getServer()
         .getScheduler()
         .runTaskTimerAsynchronously(
            this,
            () -> {
               if (this.queueGUI != null) {
                  this.queueGUI
                     .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
               }

               if (this.teamQueueGUI != null) {
                  this.teamQueueGUI
                     .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
               }
            },
            200L,
            200L
         );
      this.getLogger().info("GUI updater system started!");
   }

   public c getDatabaseManager() {
      return this.databaseManager;
   }

   public org.lime.swiftCore.t.b getDataManager() {
      return this.dataManager;
   }

   public e getKitManager() {
      return this.kitManager;
   }

   public QueueManager getQueueManager() {
      return this.queueManager;
   }

   public q getQueueMatchTimeTracker() {
      return this.queueMatchTimeTracker;
   }

   public i getMultiQueueManager() {
      return this.multiQueueManager;
   }

   public void reloadMultiQueue() {
      if (this.multiQueueManager != null) {
         this.multiQueueManager
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
      }
   }

   public v getRandomKitManager() {
      return this.randomKitManager;
   }

   public org.lime.swiftCore.kit.b getTntTagManager() {
      return this.tntTagManager;
   }

   public o getTntSumoManager() {
      return this.tntSumoManager;
   }

   public r getFlowerCrownManager() {
      return this.flowerCrownManager;
   }

   public s getKitEditor() {
      return this.kitEditor;
   }

   public j getCustomItemsManager() {
      return this.customItemsManager;
   }

   public ScoreboardManager getScoreboardManager() {
      return this.scoreboardManager;
   }

   public TablistManager getTablistManager() {
      return this.tablistManager;
   }

   public org.lime.swiftCore.arena.i getArenaManager() {
      return this.arenaManager;
   }

   public org.lime.swiftCore.k.b getFenceWandManager() {
      return this.fenceWandManager;
   }

   public h getArenaCopyPasteService() {
      return this.arenaCopyPasteService;
   }

   public org.lime.swiftCore.arena.b getDynamicArenaManager() {
      return this.dynamicArenaManager;
   }

   public DuelManager getDuelManager() {
      return this.duelManager;
   }

   public org.lime.swiftCore.duel.h getBotDuelManager() {
      return this.botDuelManager;
   }

   public org.lime.swiftCore.arena.e getArenaResetManager() {
      return this.arenaResetManager;
   }

   public org.lime.swiftCore.arena.j getArenaElevatorManager() {
      return this.arenaElevatorManager;
   }

   public k getBallisticEntryManager() {
      return this.ballisticEntryManager;
   }

   public org.lime.swiftCore.ab.c getStatsManager() {
      return this.statsManager;
   }

   public b getKitStatsManager() {
      return this.kitStatsManager;
   }

   public d getKitStatsGUI() {
      return this.kitStatsGUI;
   }

   public org.lime.swiftCore.x.b.c getMenuConfigRegistry() {
      return this.menuConfigRegistry;
   }

   public void reloadMenuConfigRegistry() {
      if (this.menuConfigRegistry != null) {
         this.menuConfigRegistry
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      }
   }

   public void reloadKitStatsGUI() {
      if (this.kitStatsGUI != null) {
         this.kitStatsGUI
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      }
   }

   public org.lime.swiftCore.ab.f getKillStreakManager() {
      return this.killStreakManager;
   }

   public l getMessagesManager() {
      return this.messagesManager;
   }

   public org.lime.swiftCore.b.i getKillMessageManager() {
      return this.killMessageManager;
   }

   public org.lime.swiftCore.x.c getQueueGUI() {
      return this.queueGUI;
   }

   public org.lime.swiftCore.x.b getTeamQueueGUI() {
      return this.teamQueueGUI;
   }

   public ob getKitRulesListener() {
      return this.kitRulesListener;
   }

   public n getHPIndicatorManager() {
      return this.hpIndicatorManager;
   }

   public CustomKitAPI getCustomKitAPI() {
      return this.customKitAPI;
   }

   private void setupTeamQueueEffects() {
      this.teamQueueManager
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            (var1, var2) -> {
               if (!var2) {
                  Bukkit.getScheduler()
                     .runTaskLater(
                        this,
                        () -> {
                           for (UUID var3x : var1.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()) {
                              if (!this.teamQueueManager
                                    .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                                       var3x
                                    )
                                 && !this.partyGameManager.isPlayerInPartyGame(var3x)) {
                                 Player var4 = Bukkit.getPlayer(var3x);
                                 if (var4 != null && var4.isOnline()) {
                                    this.duelManager
                                       .getMatchFoundEffects()
                                       .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                                          var3x
                                       );
                                    boolean var5 = this.partyManager.isInParty(var3x);
                                    this.scoreboardManager.setState(var4, var5 ? ScoreboardState.PARTY : ScoreboardState.DEFAULT);
                                    this.scoreboardManager.setPlaceholder(var4, "in_queue", "false");
                                    this.scoreboardManager.setPlaceholder(var4, "in_queue_kitname", "");
                                    this.scoreboardManager.setPlaceholder(var4, "team_queue_size", "");
                                    this.scoreboardManager.setPlaceholder(var4, "team_queue_mode", "");
                                    this.spawnItemsManager
                                       .giveSpawnItems(var4, var5 ? "party" : "default", var5, var5 && this.partyManager.isPartyOwner(var3x));
                                    if (this.tablistManager != null) {
                                       this.tablistManager.resetContext(var4);
                                    }
                                 }
                              }
                           }
                        },
                        1L
                     );
               } else {
                  Runnable var3 = () -> {
                     String var2x = var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                        .name()
                        .toLowerCase(Locale.ROOT);
                     String var3x = var1.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if()
                        + "v"
                        + var1.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if()
                        + " "
                        + var2x
                        + " • "
                        + this.dataManager
                           .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                              var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void()
                           );

                     for (UUID var5 : var1.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()) {
                        Player var6 = Bukkit.getPlayer(var5);
                        if (var6 != null && var6.isOnline()) {
                           this.scoreboardManager.setState(var6, ScoreboardState.QUEUE);
                           this.scoreboardManager.setPlaceholder(var6, "in_queue", "true");
                           this.scoreboardManager.setPlaceholder(var6, "in_queue_kitname", var3x);
                           this.scoreboardManager
                              .setPlaceholder(
                                 var6,
                                 "team_queue_size",
                                 String.valueOf(
                                    var1.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if()
                                 )
                              );
                           this.scoreboardManager.setPlaceholder(var6, "team_queue_mode", var2x);
                           this.duelManager
                              .getMatchFoundEffects()
                              .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var6,
                                 this.dataManager
                                    .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                       var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void()
                                    ),
                                 var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(),
                                 var1.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if(),
                                 var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                              );
                           this.spawnItemsManager.giveSpawnItems(var6, "queue", false, false);
                           if (this.tablistManager != null) {
                              this.tablistManager.setPlaceholder(var6, "in_queue_kitname", var3x);
                              this.tablistManager
                                 .setPlaceholder(
                                    var6,
                                    "fight_kitname",
                                    this.dataManager
                                       .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                          var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void()
                                       )
                                 );
                              this.tablistManager
                                 .setPlaceholder(
                                    var6,
                                    "team_queue_size",
                                    String.valueOf(
                                       var1.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if()
                                    )
                                 );
                              this.tablistManager.setPlaceholder(var6, "team_queue_mode", var2x);
                              this.tablistManager.setPlaceholder(var6, "blue_alive", "?");
                              this.tablistManager
                                 .setPlaceholder(
                                    var6,
                                    "blue_total",
                                    String.valueOf(
                                       var1.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if()
                                    )
                                 );
                              this.tablistManager.setPlaceholder(var6, "red_alive", "?");
                              this.tablistManager
                                 .setPlaceholder(
                                    var6,
                                    "red_total",
                                    String.valueOf(
                                       var1.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if()
                                    )
                                 );
                              this.tablistManager
                                 .setupTeamQueueContext(
                                    var6,
                                    var1.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                                 );
                           }
                        }
                     }
                  };
                  if (Bukkit.isPrimaryThread()) {
                     var3.run();
                  } else {
                     Bukkit.getScheduler().runTask(this, var3);
                  }
               }
            }
         );
   }

   private void setupQueueMatchmaking() {
      this.queueManager
         .setMatchFoundCallback(
            (var1, var2, var3, var4, var5) -> {
               if (var3 != null) {
                  boolean var6 = var3.startsWith("customkit:");
                  String var7 = var6
                     ? this.resolveCustomKitDisplayName(var1.getUniqueId(), var2.getUniqueId())
                     : this.dataManager
                        .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                           var3
                        );
                  HashMap var8 = new HashMap<>(
                     this.multiQueueManager
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var1, false, var3, var4
                        )
                  );
                  var8.put(
                     "opponent",
                     this.playerSettingsManager
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var1, var2
                        )
                  );
                  var8.put("kit", var7);
                  String var9 = this.duelManager
                     .getMatchFoundEffects()
                     .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
                        var3
                     );
                  var8.put("kit_sprite", var9);
                  var8.put("queue_sprite", var9);
                  var8.put("sprite", var9);
                  var8.put("in_queue", String.valueOf(this.queueManager.getQueueSize(var3)));
                  var8.put("players", String.valueOf(this.queueManager.getQueueSize(var3)));
                  this.duelManager
                     .getMatchFoundEffects()
                     .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                        var1.getUniqueId()
                     );
                  this.messagesManager
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var1,
                        this.multiQueueManager
                           .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                              false
                           ),
                        var8
                     );
                  this.duelManager
                     .getMatchFoundEffects()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var1, var8
                     );
                  HashMap var10 = new HashMap<>(
                     this.multiQueueManager
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var2, false, var3, var5
                        )
                  );
                  var10.put(
                     "opponent",
                     this.playerSettingsManager
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var2, var1
                        )
                  );
                  var10.put("kit", var7);
                  var10.put("kit_sprite", var9);
                  var10.put("queue_sprite", var9);
                  var10.put("sprite", var9);
                  var10.put("in_queue", String.valueOf(this.queueManager.getQueueSize(var3)));
                  var10.put("players", String.valueOf(this.queueManager.getQueueSize(var3)));
                  this.duelManager
                     .getMatchFoundEffects()
                     .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                        var2.getUniqueId()
                     );
                  this.messagesManager
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var2,
                        this.multiQueueManager
                           .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                              false
                           ),
                        var10
                     );
                  this.duelManager
                     .getMatchFoundEffects()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var2, var10
                     );
                  boolean var11 = this.duelManager
                     .getMatchFoundEffects()
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
                  int var12 = this.duelManager
                     .getMatchFoundEffects()
                     .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
                  Runnable var13 = () -> {
                     boolean var5x = var1.isOnline() && this.queueManager.hasMatchFound(var1.getUniqueId());
                     boolean var6x = var2.isOnline() && this.queueManager.hasMatchFound(var2.getUniqueId());
                     if (!var5x || !var6x) {
                        this.queueManager.removeFightingPlayer(var1.getUniqueId());
                        this.queueManager.removeFightingPlayer(var2.getUniqueId());
                        if (var6) {
                           if (!var5x) {
                              this.customKitAPI.removeKit(var1.getUniqueId());
                           }

                           if (!var6x) {
                              this.customKitAPI.removeKit(var2.getUniqueId());
                           }
                        }

                        if (var5x) {
                           if (this.queueManager.addToQueue(var1.getUniqueId(), var3)) {
                              this.scoreboardManager.setState(var1, ScoreboardState.QUEUE);
                              this.scoreboardManager.setPlaceholder(var1, "in_queue", "true");
                              this.spawnItemsManager.giveSpawnItems(var1, "queue", false, false);
                              this.messagesManager
                                 .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                    var1, "queue-opponent-left"
                                 );
                           }
                        } else if (var1.isOnline()) {
                           this.scoreboardManager.setState(var1, ScoreboardState.DEFAULT);
                           this.scoreboardManager.setPlaceholder(var1, "in_queue", "false");
                           this.scoreboardManager.setPlaceholder(var1, "in_queue_kitname", "");
                           this.spawnItemsManager.giveSpawnItems(var1, "default", false, false);
                        }

                        if (var6x) {
                           if (this.queueManager.addToQueue(var2.getUniqueId(), var3)) {
                              this.scoreboardManager.setState(var2, ScoreboardState.QUEUE);
                              this.scoreboardManager.setPlaceholder(var2, "in_queue", "true");
                              this.spawnItemsManager.giveSpawnItems(var2, "queue", false, false);
                              this.messagesManager
                                 .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                    var2, "queue-opponent-left"
                                 );
                           }
                        } else if (var2.isOnline()) {
                           this.scoreboardManager.setState(var2, ScoreboardState.DEFAULT);
                           this.scoreboardManager.setPlaceholder(var2, "in_queue", "false");
                           this.scoreboardManager.setPlaceholder(var2, "in_queue_kitname", "");
                           this.spawnItemsManager.giveSpawnItems(var2, "default", false, false);
                        }
                     } else if (var6) {
                        this.startCustomKitFromQueue(var1, var2);
                     } else {
                        this.duelManager.startQueueMatch(var1, var2, var3);
                     }
                  };
                  if (!var11 && var12 > 0) {
                     this.getServer().getScheduler().runTaskLater(this, var13, (long)var12);
                  } else {
                     var13.run();
                  }
               }
            }
         );
   }

   private String resolveCustomKitDisplayName(UUID var1, UUID var2) {
      CustomKitAPI.CustomKitData var3 = this.customKitAPI.getKit(var1);
      if (var3 != null) {
         return var3.displayName();
      } else {
         CustomKitAPI.CustomKitData var4 = this.customKitAPI.getKit(var2);
         return var4 != null ? var4.displayName() : "Custom Kit";
      }
   }

   private void startCustomKitFromQueue(Player var1, Player var2) {
      CustomKitAPI.CustomKitData var3 = this.customKitAPI.getKit(var1.getUniqueId());
      CustomKitAPI.CustomKitData var4 = this.customKitAPI.getKit(var2.getUniqueId());
      if (var3 == null && var4 == null) {
         this.messagesManager
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "queue-kit-unavailable"
            );
         this.messagesManager
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var2, "queue-kit-unavailable"
            );
         this.queueManager.removeFightingPlayer(var1.getUniqueId());
         this.queueManager.removeFightingPlayer(var2.getUniqueId());
      } else {
         CustomKitAPI.CustomKitData var5 = var3 != null ? var3 : var4;
         ItemStack[] var6 = var3 != null ? var3.contents() : var5.contents();
         ItemStack[] var7 = var3 != null ? var3.armor() : var5.armor();
         ItemStack[] var8 = var4 != null ? var4.contents() : var5.contents();
         ItemStack[] var9 = var4 != null ? var4.armor() : var5.armor();
         ArenaType var10 = var5.arenaType();
         Map var11 = var5.rules();
         int var12 = var5.rounds();
         if (var3 == null) {
            this.customKitAPI.registerKit(var1.getUniqueId(), var5.displayName(), var6, var7, var10, var11, var12);
         }

         if (var4 == null) {
            this.customKitAPI.registerKit(var2.getUniqueId(), var5.displayName(), var8, var9, var10, var11, var12);
         }

         this.duelManager.startCustomKitMatch(var1, var2, var10, var6, var7, var8, var9, var11, var12);
      }
   }

   public void reloadMessages() {
      this.messagesManager = new l(this);
      if (this.editorListener != null) {
         this.editorListener.loadCachedConfig();
      }
   }

   public void reloadOpenChallenge() {
      if (this.openChallengeManager != null) {
         this.openChallengeManager
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      }
   }

   public void reloadDuelScoreActionBar() {
      if (this.duelManager != null) {
         this.duelManager.reloadDuelScoreActionBar();
      }
   }

   public void reloadMiscellaneous() {
      this.reloadOpenChallenge();
      this.reloadDuelScoreActionBar();
      if (this.dynamicArenaManager != null) {
         this.dynamicArenaManager
            .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void();
      }

      if (this.ballisticEntryManager != null) {
         this.ballisticEntryManager
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
         if (this.arenaElevatorManager != null) {
            this.arenaElevatorManager
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String();
         }
      }
   }

   public void reloadBotConfig() {
      if (this.botDuelManager != null) {
         this.botDuelManager.reloadConfig();
      }
   }

   public void reloadScoreboard() {
      if (this.scoreboardManager != null) {
         this.scoreboardManager.shutdown();

         for (Player var2 : this.getServer().getOnlinePlayers()) {
            this.scoreboardManager.removeScoreboard(var2);
         }
      }

      this.scoreboardManager = new ScoreboardManager(this);
      this.startScoreboard();
      this.getServer()
         .getScheduler()
         .runTaskLater(
            this,
            () -> {
               for (Player var2x : this.getServer().getOnlinePlayers()) {
                  if (this.getPlayerSettingsManager()
                     .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                        var2x
                     )
                     .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
                     )
                   {
                     this.scoreboardManager.createScoreboard(var2x);
                  }

                  UUID var3 = var2x.getUniqueId();
                  if (this.ffaManager.isInFFA(var3)) {
                     String var4 = this.ffaManager.getPlayerArena(var3);
                     org.lime.swiftCore.arena.d var5 = this.arenaManager
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                           var4
                        );
                     if (var5 != null
                        && var5.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper()
                        )
                      {
                        String var6 = var5.öÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000ifnew()
                           .iterator()
                           .next();
                        this.scoreboardManager.setState(var2x, ScoreboardState.FFA);
                        this.scoreboardManager.setPlaceholder(var2x, "in_ffa", "true");
                        this.scoreboardManager.setPlaceholder(var2x, "is_ffa", "true");
                        this.scoreboardManager
                           .setPlaceholder(
                              var2x,
                              "kit",
                              this.dataManager
                                 .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                    var6
                                 )
                           );
                        Set var7 = this.ffaManager.getArenaPlayers(var4);
                        this.scoreboardManager.setPlaceholder(var2x, "ffa_players", String.valueOf(var7.size()));
                     }
                  } else if (this.duelManager.isInMatch(var3)) {
                     this.scoreboardManager.setState(var2x, ScoreboardState.DUEL);
                     org.lime.swiftCore.duel.g var8 = this.duelManager.getMatch(var3);
                     if (var8 != null) {
                        this.scoreboardManager
                           .setPlaceholder(
                              var2x,
                              "kit",
                              this.dataManager
                                 .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                    var8.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                                 )
                           );
                        this.scoreboardManager.setPlaceholder(var2x, "in_fight", "true");
                     }
                  } else if (this.partyGameManager.isPlayerInPartyGame(var3)) {
                     org.lime.swiftCore.party.d var9 = this.partyGameManager.getPlayerActiveGame(var3);
                     if (var9 != null) {
                        ScoreboardState var13 = switch (var9.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()) {
                           case Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new -> ScoreboardState.PARTY_FFA;
                           case Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String -> ScoreboardState.PARTY_SPLIT;
                           case o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super -> ScoreboardState.PARTY_VS;
                        };
                        this.scoreboardManager.setState(var2x, var13);
                        this.scoreboardManager
                           .setPlaceholder(
                              var2x,
                              "kit",
                              this.dataManager
                                 .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                                    var9.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                                 )
                           );
                        this.scoreboardManager.setPlaceholder(var2x, "in_party", "true");
                     }
                  } else if (this.tournamentManager != null
                     && this.tournamentManager
                        .ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if(
                           var3
                        )) {
                     this.scoreboardManager.setState(var2x, ScoreboardState.SPECTATING);
                     this.scoreboardManager.setPlaceholder(var2x, "spectating", "true");
                  } else if (this.eventManager != null
                     && this.eventManager
                        .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                           var3
                        )) {
                     this.scoreboardManager.setState(var2x, ScoreboardState.SPECTATING);
                     this.scoreboardManager.setPlaceholder(var2x, "spectating", "true");
                  } else if (this.tournamentManager != null
                     && this.tournamentManager
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                           var3
                        )) {
                     this.scoreboardManager.setState(var2x, ScoreboardState.TOURNAMENT);
                     org.lime.swiftCore.eb.g var12 = this.tournamentManager
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
                     if (var12 != null) {
                        this.scoreboardManager
                           .setPlaceholder(
                              var2x,
                              "tournament_kit",
                              var12.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
                           );
                        this.scoreboardManager
                           .setPlaceholder(
                              var2x,
                              "tournament_type",
                              var12.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
                                 .name()
                           );
                        this.scoreboardManager
                           .setPlaceholder(
                              var2x,
                              "tournament_round",
                              String.valueOf(
                                 var12.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void()
                              )
                           );
                        this.scoreboardManager
                           .setPlaceholder(
                              var2x,
                              "tournament_alive",
                              String.valueOf(
                                 var12.oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do()
                              )
                           );
                        this.scoreboardManager
                           .setPlaceholder(
                              var2x,
                              "tournament_max",
                              String.valueOf(
                                 var12.ÒO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000while()
                              )
                           );
                        this.scoreboardManager
                           .setPlaceholder(
                              var2x,
                              "tournament_state",
                              var12.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper()
                                 .name()
                           );
                     }
                  } else if (this.eventManager != null
                     && this.eventManager
                        .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                           var3
                        )) {
                     this.scoreboardManager.setState(var2x, ScoreboardState.EVENT);
                     org.lime.swiftCore.hb.d var11 = this.eventManager
                        .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void();
                     if (var11 != null) {
                        this.scoreboardManager
                           .setPlaceholder(
                              var2x,
                              "event_type",
                              var11.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null()
                                 .name()
                           );
                        this.scoreboardManager
                           .setPlaceholder(
                              var2x,
                              "event_arena",
                              var11.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
                           );
                        this.scoreboardManager
                           .setPlaceholder(
                              var2x,
                              "event_kit",
                              var11.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                                    != null
                                 ? var11.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                                 : ""
                           );
                        this.scoreboardManager
                           .setPlaceholder(
                              var2x,
                              "event_alive",
                              String.valueOf(
                                 var11.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                              )
                           );
                        this.scoreboardManager
                           .setPlaceholder(
                              var2x,
                              "event_max",
                              String.valueOf(
                                 var11.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void()
                              )
                           );
                        this.scoreboardManager
                           .setPlaceholder(
                              var2x,
                              "event_state",
                              var11.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
                                 .name()
                           );
                     }
                  } else if (this.spectatorManager
                     .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                        var3
                     )) {
                     this.scoreboardManager.setState(var2x, ScoreboardState.SPECTATING);
                     this.scoreboardManager.setPlaceholder(var2x, "spectating", "true");
                  } else if (this.kitEditor
                     .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                        var2x
                     )) {
                     this.scoreboardManager.setState(var2x, ScoreboardState.KIT_EDITING);
                     String var10 = this.kitEditor
                        .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                           var2x
                        );
                     if (var10 != null) {
                        this.scoreboardManager.setPlaceholder(var2x, "editing_kit", "true");
                        this.scoreboardManager.setPlaceholder(var2x, "kit", var10);
                     }
                  } else if (!this.queueManager.isInAnyQueue(var3) && !this.rankedManager.isInQueue(var3)) {
                     this.scoreboardManager.setState(var2x, ScoreboardState.DEFAULT);
                     this.scoreboardManager.setPlaceholder(var2x, "in_ffa", "false");
                     this.scoreboardManager.setPlaceholder(var2x, "is_ffa", "false");
                  } else {
                     this.scoreboardManager.setState(var2x, ScoreboardState.QUEUE);
                     this.scoreboardManager.setPlaceholder(var2x, "in_queue", "true");
                  }

                  this.scoreboardManager.updateScoreboard(var2x, true);
               }
            },
            5L
         );
   }

   public void reloadTablist() {
      if (this.tablistManager != null) {
         this.tablistManager.reload();
      }
   }

   public void reloadDuelGUI() {
      if (this.duelGUI != null) {
         this.duelGUI
            .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null();
      }
   }

   public void reloadQueueGUI() {
      if (this.queueGUI != null) {
         this.queueGUI
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      }

      if (this.teamQueueGUI != null) {
         this.teamQueueGUI
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      }

      if (this.teamQueueManager != null) {
         this.teamQueueManager
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
      }
   }

   public void reloadRounds() {
      if (this.duelGUI != null) {
         this.duelGUI
            .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null();
      }
   }

   public void reloadSpawnItems() {
      this.spawnItemsManager = new SpawnItemsManager(this);
      if (this.duelManager != null) {
         this.duelManager.reloadRematchItem();
      }
   }

   public void reloadKitEditor() {
      this.editorGUI = new f(this);
      if (this.editorListener != null) {
         this.editorListener.loadCachedConfig();
      }

      if (this.armorTrimGUI != null) {
         this.armorTrimGUI
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      }

      if (this.shieldEditorGUI != null) {
         this.shieldEditorGUI
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      }
   }

   public void reloadArmorTrim() {
      this.armorTrimGUI = new org.lime.swiftCore.x.j(this);
   }

   public void reloadShieldEditor() {
      if (this.shieldEditorGUI != null) {
         this.shieldEditorGUI
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      }
   }

   public org.lime.swiftCore.kit.c getPrePotionsManager() {
      return this.prePotionsManager;
   }

   public org.lime.swiftCore.v.c getLimboManager() {
      return this.limboManager;
   }

   public void reloadLimbo() {
      if (this.limboManager != null) {
         this.limboManager.reload();
      }
   }

   public void reloadPrePotions() {
      if (this.prePotionsManager != null) {
         this.prePotionsManager
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      }
   }

   public void reloadPartyMenus() {
      for (Player var2 : Bukkit.getOnlinePlayers()) {
         if (org.lime.swiftCore.b.j.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var2.getUniqueId()
            )
            == org.lime.swiftCore.b.j._b.Ôo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000ifsuper
            )
          {
            var2.closeInventory();
            org.lime.swiftCore.b.j.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var2.getUniqueId()
            );
         }
      }

      this.partySettingsGUI = new org.lime.swiftCore.party.b.b(this, this.partyManager);
      this.partyManageGUI = new org.lime.swiftCore.party.b.e(this, this.partyManager);
      this.partyInfoGUI = new org.lime.swiftCore.party.b.d(this, this.partyManager);
      this.partyDisbandGUI
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      if (this.partyListGUIConfig != null) {
         this.partyListGUIConfig
            .õO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectsuper();
      }
   }

   public SpawnItemsManager getSpawnItemsManager() {
      return this.spawnItemsManager;
   }

   public org.lime.swiftCore.spawn.b getLobbyManager() {
      return this.lobbyManager;
   }

   public org.lime.swiftCore.spawn.c getLobbyEffectManager() {
      return this.lobbyEffectManager;
   }

   public void reloadLobbyEffects() {
      if (this.lobbyEffectManager != null) {
         this.lobbyEffectManager
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      }
   }

   public org.lime.swiftCore.q.b getPillarsOfFortuneManager() {
      return this.pillarsOfFortuneManager;
   }

   public void reloadPillarsOfFortune() {
      if (this.pillarsOfFortuneManager != null) {
         this.pillarsOfFortuneManager
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
      }
   }

   public org.lime.swiftCore.p.b getLavaRaiseManager() {
      return this.lavaRaiseManager;
   }

   public void reloadLavaRaise() {
      if (this.lavaRaiseManager != null) {
         this.lavaRaiseManager.reload();
      }
   }

   public org.lime.swiftCore.x.l getKitRuleGUI() {
      return this.kitRuleGUI;
   }

   public FFAManager getFFAManager() {
      return this.ffaManager;
   }

   public org.lime.swiftCore.ffa.b getFFAGUI() {
      return this.ffaGUI;
   }

   public void reloadFFAGUI() {
      if (this.ffaGUI != null) {
         this.ffaGUI
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      }
   }

   public org.lime.swiftCore.ffa.d getFFACombatManager() {
      return this.ffaCombatManager;
   }

   public org.lime.swiftCore.ffa.c getRandomFFAInventoryManager() {
      return this.randomFFAInventoryManager;
   }

   public org.lime.swiftCore.h.b getSpectatorManager() {
      return this.spectatorManager;
   }

   public org.lime.swiftCore.h.d getSpectatePlayersGUI() {
      return this.spectatePlayersGUI;
   }

   public void reloadSpectateGUI() {
      this.spectatePlayersGUI
         .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float();
   }

   public PartyManager getPartyManager() {
      return this.partyManager;
   }

   public PartyGameManager getPartyGameManager() {
      return this.partyGameManager;
   }

   public org.lime.swiftCore.fb.d getTeamQueueManager() {
      return this.teamQueueManager;
   }

   public org.lime.swiftCore.party.b.b getPartySettingsGUI() {
      return this.partySettingsGUI;
   }

   public org.lime.swiftCore.party.b.e getPartyManageGUI() {
      return this.partyManageGUI;
   }

   public org.lime.swiftCore.party.b.d getPartyInfoGUI() {
      return this.partyInfoGUI;
   }

   public org.lime.swiftCore.party.b.f getPartyListGUIConfig() {
      return this.partyListGUIConfig;
   }

   public org.lime.swiftCore.x.k getPartyDisbandGUI() {
      return this.partyDisbandGUI;
   }

   public org.lime.swiftCore.cb.b getPlayerSettingsManager() {
      return this.playerSettingsManager;
   }

   public w getArenaListener() {
      return this.arenaListener;
   }

   public org.lime.swiftCore.cb.d getPlayerSettingsGUI() {
      return this.playerSettingsGUI;
   }

   public org.lime.swiftCore.w.b getOpenChallengeManager() {
      return this.openChallengeManager;
   }

   public RankedManager getRankedManager() {
      return this.rankedManager;
   }

   public org.lime.swiftCore.ranked.d getEloHistoryManager() {
      return this.eloHistoryManager;
   }

   public org.lime.swiftCore.ranked.b getKitsRankManager() {
      return this.kitsRankManager;
   }

   public org.lime.swiftCore.n.c getKillEffectManager() {
      return this.killEffectManager;
   }

   public org.lime.swiftCore.n.b getKillEffectGUI() {
      return this.killEffectGUI;
   }

   public org.lime.swiftCore.d.b getAnnouncementMessageManager() {
      return this.announcementMessageManager;
   }

   public org.lime.swiftCore.d.c getJoinMessageGUI() {
      return this.joinMessageGUI;
   }

   public org.lime.swiftCore.d.c getLeaveMessageGUI() {
      return this.leaveMessageGUI;
   }

   public org.lime.swiftCore.d.d getAnnouncementMessageService() {
      return this.announcementMessageService;
   }

   public org.lime.swiftCore.j.c getKillSoundManager() {
      return this.killSoundManager;
   }

   public org.lime.swiftCore.j.b getKillSoundGUI() {
      return this.killSoundGUI;
   }

   public org.lime.swiftCore.x.i getModeSelectionGUI() {
      return this.modeSelectionGUI;
   }

   public org.lime.swiftCore.x.h getBotDifficultyGUI() {
      return this.botDifficultyGUI;
   }

   public org.lime.swiftCore.x.n getRankedKitGUI() {
      return this.rankedKitGUI;
   }

   public g getDuelGUI() {
      return this.duelGUI;
   }

   public m getDuelRequestGUI() {
      return this.duelRequestGUI;
   }

   public void reloadModeGUI() {
      if (this.modeSelectionGUI != null) {
         this.modeSelectionGUI
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      }
   }

   public void reloadRankedKitGUI() {
      if (this.rankedKitGUI != null) {
         this.rankedKitGUI
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      }
   }

   public org.lime.swiftCore.x.e getRankedQueueGUI() {
      return this.rankedQueueGUI;
   }

   public void reloadRankedQueueGUI() {
      if (this.rankedQueueGUI != null) {
         this.rankedQueueGUI
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      }
   }

   public void reloadKillEffectGUI() {
      this.killEffectGUI = new org.lime.swiftCore.n.b(this, this.killEffectManager);
   }

   public void reloadAnnouncementMessages() {
      this.announcementMessageManager
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      this.joinMessageGUI
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      this.leaveMessageGUI
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
   }

   public void reloadPlayerSettingsGUI() {
      if (this.playerSettingsManager != null) {
         this.playerSettingsManager
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      }

      this.playerSettingsGUI = new org.lime.swiftCore.cb.d(this, this.playerSettingsManager);
   }

   private void setupEconomy() {
      try {
         Class var1 = Class.forName("net.milkbowl.vault.economy.Economy");
         RegisteredServiceProvider var2 = this.getServer().getServicesManager().getRegistration(var1);
         if (var2 == null) {
            this.getLogger().info("No economy plugin found. Economy features disabled.");
            return;
         }

         this.economy = var2.getProvider();
         this.getLogger().info("Vault economy hooked!");
      } catch (Exception var3) {
         this.getLogger().info("Vault not available. Economy features disabled.");
      }
   }

   public Object getEconomy() {
      return this.economy;
   }

   public org.lime.swiftCore.l.b getEconomyManager() {
      return this.economyManager;
   }

   public boolean isWorldDisabled(World var1) {
      return var1 == null ? false : this.isWorldDisabled(var1.getName());
   }

   public boolean isWorldDisabled(String var1) {
      return var1 == null ? false : this.disabledWorldsCache.contains(var1);
   }

   public void reloadDisabledWorlds() {
      this.disabledWorldsCache.clear();

      for (String var2 : this.getConfig().getStringList("disabled-worlds")) {
         this.disabledWorldsCache.add(var2);
      }

      this.debug = this.getConfig().getBoolean("debug", false);
   }

   public void reloadLobbyDuelInteractConfig() {
      if (this.lobbyDuelInteractListener != null) {
         this.lobbyDuelInteractListener.cacheConfig();
      }
   }

   private void logVersionCompat() {
      String var1 = Bukkit.getMinecraftVersion();
      boolean var2 = org.lime.swiftCore.b.b.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      this.getLogger().info("Detected MC " + var1 + (var2 ? " (26.1+ world format)" : " (legacy world format)"));
      if (var2) {
         this.getLogger().info("World migration compat enabled — stored world names will be resolved automatically.");
      }
   }

   public boolean isDebug() {
      return this.debug;
   }

   public org.lime.swiftCore.s.b getChunkyIntegration() {
      return this.chunkyIntegration;
   }

   public org.lime.swiftCore.kit.k getArmorTrimManager() {
      return this.armorTrimManager;
   }

   public org.lime.swiftCore.x.j getArmorTrimGUI() {
      return this.armorTrimGUI;
   }

   public x getShieldDesignManager() {
      return this.shieldDesignManager;
   }

   public org.lime.swiftCore.x.d getShieldEditorGUI() {
      return this.shieldEditorGUI;
   }

   public f getEditorGUI() {
      return this.editorGUI;
   }

   public org.lime.swiftCore.kit.l getKitStateResolver() {
      return this.kitStateResolver;
   }

   public org.lime.swiftCore.r.b getSkyWarsLootManager() {
      return this.skyWarsLootManager;
   }

   public void reloadSkyWarsLoot() {
      if (this.skyWarsLootManager != null) {
         this.skyWarsLootManager
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      }
   }

   public org.lime.swiftCore.h.e getSpectatorPlayersGUI() {
      return this.spectatorPlayersGUI;
   }

   public void reloadSpectatorPlayersGUI() {
      if (this.spectatorPlayersGUI != null) {
         this.spectatorPlayersGUI
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      }
   }

   public org.lime.swiftCore.z.b getParkourManager() {
      return this.parkourManager;
   }

   public org.lime.swiftCore.f.c getSignQueueManager() {
      return this.signQueueManager;
   }

   public void reloadSignQueue() {
      if (this.signQueueManager != null) {
         this.signQueueManager
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      }
   }

   public org.lime.swiftCore.c.b getDeathAnimationManager() {
      return this.deathAnimationManager;
   }

   public org.lime.swiftCore.c.c getDeathAnimationGUI() {
      return this.deathAnimationGUI;
   }

   public void reloadDeathAnimationGUI() {
      this.deathAnimationGUI = new org.lime.swiftCore.c.c(this, this.deathAnimationManager);
   }

   public org.lime.swiftCore.i.b getTrailEffectManager() {
      return this.trailEffectManager;
   }

   public org.lime.swiftCore.i.c getTrailEffectGUI() {
      return this.trailEffectGUI;
   }

   public void reloadTrailEffectGUI() {
      this.trailEffectGUI = new org.lime.swiftCore.i.c(this, this.trailEffectManager);
   }

   public org.lime.swiftCore.eb.b getTournamentManager() {
      return this.tournamentManager;
   }

   public org.lime.swiftCore.hb.c getEventManager() {
      return this.eventManager;
   }

   public org.lime.swiftCore.g.d getClanManager() {
      return this.clanManager;
   }

   public boolean isClansEnabled() {
      return this.getConfig().getBoolean("clans.enabled", true);
   }

   private void unregisterClanCommand() {
      PluginCommand var1 = this.getCommand("clan");
      if (var1 != null) {
         try {
            CommandMap var2 = this.getServer().getCommandMap();
            var1.unregister(var2);
            var2.getKnownCommands().entrySet().removeIf(var1x -> var1x.getValue() == var1);
         } catch (Throwable var3) {
            this.getLogger().warning("Failed to unregister /clan while clans are disabled: " + var3.getMessage());
         }
      }
   }

   public org.lime.swiftCore.g.b.b getClanGUI() {
      return this.clanGUI;
   }

   public org.lime.swiftCore.party.b.c getOpenPartyGUI() {
      return this.openPartyGUI;
   }

   public void reloadOpenPartyGUI() {
      if (this.openPartyGUI != null) {
         this.openPartyGUI
            .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return();
      }
   }

   public org.lime.swiftCore.u.d getMatchHistoryManager() {
      return this.matchHistoryManager;
   }

   public org.lime.swiftCore.u.b getMatchHistoryGUI() {
      return this.matchHistoryGUI;
   }

   public void reloadMatchHistoryGUI() {
      if (this.matchHistoryGUI != null) {
         this.matchHistoryGUI
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      }
   }

   public org.lime.swiftCore.db.d getFriendManager() {
      return this.friendManager;
   }
}
