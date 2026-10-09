package org.lime.swiftCore.ranked;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.lime.swiftCore.SwiftCore;
import org.lime.swiftCore.arena.ArenaType;
import org.lime.swiftCore.b.k;
import org.lime.swiftCore.kit.ColorPartyKitGuard;
import org.lime.swiftCore.kit.m;
import org.lime.swiftCore.kit.t;
import org.lime.swiftCore.kit.w;
import org.lime.swiftCore.scoreboard.ScoreboardState;

public class RankedManager {
   private final SwiftCore plugin;
   private final org.lime.swiftCore.y.c databaseManager;
   private final Map<UUID, c> rankedData;
   private final Map<String, List<UUID>> rankedQueues;
   private final Map<UUID, Set<String>> playerQueues;
   private final Map<UUID, Map<String, Long>> queueJoinTimes;
   private final Set<String> pendingQueueChecks;
   private final Set<String> pendingArenaMatches = ConcurrentHashMap.newKeySet();
   private final int eloPerWin;
   private final int eloPerLoss;
   private final int placementMatches;
   private final int eloKFactor;
   private final int placementKFactor;
   private final int minEloGain;
   private final int maxEloGain;
   private final int minEloLoss;
   private final int maxEloLoss;
   private final int winStreakExtraPoints;
   private final int minimumWinStreakNeeded;
   private final int loseStreakExtraLoss;
   private final int minimumLoseStreakNeeded;
   private final int searchRangeStart;
   private final int searchRangeExpandPerSecond;
   private final int searchRangeMax;
   private e tierCommandsManager;
   private final List<RankedManager._b> cachedTiers;
   private k cachedTierUpSound;
   private k cachedTierDownSound;
   private boolean cachedPermissionWinBonusEnabled;
   private boolean cachedPermissionBonusApplyGlobal;
   private boolean cachedPermissionBonusApplyKit;
   private boolean cachedPermissionBonusesStack;
   private List<RankedManager._g> cachedPermissionEloBonuses = List.of();
   private final Set<UUID> pendingLoads = ConcurrentHashMap.newKeySet();
   private final Map<UUID, RankedManager._f> lastRankedResults = new ConcurrentHashMap<>();

   public RankedManager(SwiftCore var1, org.lime.swiftCore.y.c var2) {
      this.plugin = var1;
      this.databaseManager = var2;
      this.rankedData = new ConcurrentHashMap<>();
      this.rankedQueues = new ConcurrentHashMap<>();
      this.playerQueues = new ConcurrentHashMap<>();
      this.queueJoinTimes = new ConcurrentHashMap<>();
      this.pendingQueueChecks = ConcurrentHashMap.newKeySet();
      this.eloPerWin = var1.getConfig().getInt("ranked.elo-per-win", 12);
      this.eloPerLoss = var1.getConfig().getInt("ranked.elo-per-loss", 12);
      this.placementMatches = var1.getConfig().getInt("ranked.placement-matches", 5);
      this.eloKFactor = var1.getConfig().getInt("ranked.elo.k-factor", 32);
      this.placementKFactor = var1.getConfig().getInt("ranked.elo.placement-k-factor", 48);
      this.minEloGain = var1.getConfig().getInt("ranked.elo.min-gain", 4);
      this.maxEloGain = var1.getConfig().getInt("ranked.elo.max-gain", 40);
      this.minEloLoss = var1.getConfig().getInt("ranked.elo.min-loss", 4);
      this.maxEloLoss = var1.getConfig().getInt("ranked.elo.max-loss", 40);
      this.winStreakExtraPoints = var1.getConfig().getInt("ranked.streak-points.win-streak-extra-point", 0);
      this.minimumWinStreakNeeded = Math.max(1, var1.getConfig().getInt("ranked.streak-points.minimum-win-streak-needed", 5));
      this.loseStreakExtraLoss = var1.getConfig().getInt("ranked.streak-points.lose-streak-extra-loss", 0);
      this.minimumLoseStreakNeeded = Math.max(1, var1.getConfig().getInt("ranked.streak-points.minimum-lose-streak-needed", 5));
      this.searchRangeStart = var1.getConfig().getInt("ranked.matchmaking.search-range-start", 150);
      this.searchRangeExpandPerSecond = var1.getConfig().getInt("ranked.matchmaking.search-range-expand-per-second", 4);
      this.searchRangeMax = var1.getConfig().getInt("ranked.matchmaking.search-range-max", 450);
      this.tierCommandsManager = new e(var1);
      this.cachedTiers = this.buildTierCache();
      this.reloadConfig();
   }

   public void reloadConfig() {
      this.cachedTiers.clear();
      this.cachedTiers.addAll(this.buildTierCache());
      this.cachedTierUpSound = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig(), "ranked.sounds.tier-up", "UI_TOAST_CHALLENGE_COMPLETE"
      );
      this.cachedTierDownSound = k.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         this.plugin.getConfig(), "ranked.sounds.tier-down", "ENTITY_VILLAGER_NO"
      );
      this.cachedPermissionWinBonusEnabled = this.plugin.getConfig().getBoolean("ranked.permission-win-bonus.enabled", false);
      this.cachedPermissionBonusApplyGlobal = this.plugin.getConfig().getBoolean("ranked.permission-win-bonus.apply-to-global", true);
      this.cachedPermissionBonusApplyKit = this.plugin.getConfig().getBoolean("ranked.permission-win-bonus.apply-to-kit", true);
      this.cachedPermissionBonusesStack = this.plugin.getConfig().getBoolean("ranked.permission-win-bonus.stack-bonuses", false);
      this.cachedPermissionEloBonuses = this.buildPermissionEloBonusCache();
      this.tierCommandsManager = new e(this.plugin);
   }

   private List<RankedManager._g> buildPermissionEloBonusCache() {
      ConfigurationSection var1 = this.plugin.getConfig().getConfigurationSection("ranked.permission-win-bonus.bonuses");
      if (var1 == null) {
         return List.of();
      } else {
         ArrayList var2 = new ArrayList();

         for (String var4 : var1.getKeys(false)) {
            String var5 = var1.getString(var4 + ".permission", "").trim();
            int var6 = Math.max(0, var1.getInt(var4 + ".bonus", 0));
            if (!var5.isEmpty() && var6 > 0) {
               var2.add(new RankedManager._g(var5, var6));
            }
         }

         return List.copyOf(var2);
      }
   }

   private List<RankedManager._b> buildTierCache() {
      ArrayList var1 = new ArrayList();
      ConfigurationSection var2 = this.plugin.getConfig().getConfigurationSection("tier-ranks.tiers");
      if (var2 == null) {
         return var1;
      } else {
         for (String var4 : var2.getKeys(false)) {
            String var5 = var2.getString(var4 + ".tier-range", "");
            String var6 = var2.getString(var4 + ".name", "§7Unknown");
            if (var5.contains("-")) {
               String[] var7 = var5.split("-");

               try {
                  int var8 = Integer.parseInt(var7[0]);
                  int var9 = Integer.parseInt(var7[1]);
                  var1.add(new RankedManager._b(var8, var9, var6));
               } catch (NumberFormatException var10) {
               }
            }
         }

         return var1;
      }
   }

   c loadRankedData(UUID var1) {
      String var2 = "SELECT * FROM player_ranked WHERE player_id = ?";

      try {
         c var7;
         try (
            Connection var3 = this.databaseManager
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String();
            PreparedStatement var4 = var3.prepareStatement(var2);
         ) {
            var4.setString(1, var1.toString());
            ResultSet var5 = var4.executeQuery();
            if (!var5.next()) {
               return null;
            }

            int var6 = var5.getInt("tier_points");
            if (var6 < 0) {
               this.clampStoredRankedPoints(var1);
            }

            var7 = new c(
               var1,
               var6,
               var5.getInt("ranked_wins"),
               var5.getInt("ranked_losses"),
               var5.getInt("matches_played"),
               var5.getInt("win_streak"),
               var5.getInt("lose_streak")
            );
         }

         return var7;
      } catch (SQLException var12) {
         this.plugin.getLogger().severe("Failed to load ranked data: " + var12.getMessage());
         return null;
      }
   }

   public void saveData() {
      for (c var2 : this.rankedData.values()) {
         this.saveRankedDataSync(var2);
      }
   }

   private void saveRankedData(c var1) {
      String var2 = var1.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
         .toString();
      int var3 = var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float();
      int var4 = var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object();
      int var5 = var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void();
      int var6 = var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int();
      int var7 = var1.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class();
      int var8 = var1.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null();
      if (!this.plugin.isEnabled()) {
         this.saveRankedDataSync(var1);
      } else {
         Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> this.executeSaveRankedData(var2, var3, var4, var5, var6, var7, var8));
      }
   }

   private void saveRankedDataSync(c var1) {
      this.executeSaveRankedData(
         var1.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
            .toString(),
         var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(),
         var1.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(),
         var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(),
         var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int(),
         var1.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(),
         var1.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null()
      );
   }

   private void executeSaveRankedData(String var1, int var2, int var3, int var4, int var5, int var6, int var7) {
      String var8;
      if (this.databaseManager
         .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
         )
       {
         var8 = "INSERT INTO player_ranked (player_id, tier_points, ranked_wins, ranked_losses,\n    matches_played, win_streak, lose_streak, peak_points, last_match_at)\nVALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)\nON DUPLICATE KEY UPDATE tier_points=VALUES(tier_points), ranked_wins=VALUES(ranked_wins),\n    ranked_losses=VALUES(ranked_losses), matches_played=VALUES(matches_played),\n    win_streak=VALUES(win_streak), lose_streak=VALUES(lose_streak),\n    peak_points=GREATEST(peak_points, VALUES(peak_points)), last_match_at=VALUES(last_match_at)\n";
      } else {
         var8 = "MERGE INTO player_ranked (player_id, tier_points, ranked_wins, ranked_losses,\n    matches_played, win_streak, lose_streak, peak_points, last_match_at)\nKEY (player_id)\nVALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)\n";
      }

      try (
         Connection var9 = this.databaseManager
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String();
         PreparedStatement var10 = var9.prepareStatement(var8);
      ) {
         var10.setString(1, var1);
         var10.setInt(2, var2);
         var10.setInt(3, var3);
         var10.setInt(4, var4);
         var10.setInt(5, var5);
         var10.setInt(6, var6);
         var10.setInt(7, var7);
         var10.setInt(8, var2);
         var10.setLong(9, System.currentTimeMillis());
         var10.executeUpdate();
      } catch (SQLException var17) {
         this.plugin.getLogger().severe("Failed to save ranked data: " + var17.getMessage());
      }
   }

   public c getRankedData(UUID var1) {
      c var2 = this.rankedData.get(var1);
      if (var2 == null) {
         var2 = new c(var1);
         var2.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            this.getStartingTier()
         );
         c var3 = this.rankedData.putIfAbsent(var1, var2);
         if (var3 != null) {
            return var3;
         }

         this.preloadRankedDataAsync(var1);
      }

      return var2;
   }

   public void preloadRankedDataAsync(UUID var1) {
      if (this.pendingLoads.add(var1)) {
         if (!this.plugin.isEnabled()) {
            this.pendingLoads.remove(var1);
         } else {
            Bukkit.getScheduler()
               .runTaskAsynchronously(
                  this.plugin,
                  () -> {
                     try {
                        c var2 = this.loadRankedData(var1);
                        if (!this.plugin.isEnabled()) {
                           return;
                        }

                        if (var2 != null) {
                           this.rankedData.put(var1, var2);
                        } else {
                           c var3 = this.rankedData.get(var1);
                           if (var3 == null) {
                              c var4 = new c(var1);
                              var4.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 this.getStartingTier()
                              );
                              this.rankedData.put(var1, var4);
                              this.saveRankedData(var4);
                           }
                        }
                     } finally {
                        this.pendingLoads.remove(var1);
                     }
                  }
               );
         }
      }
   }

   private void clampStoredRankedPoints(UUID var1) {
      String var2 = "UPDATE player_ranked SET tier_points = 0 WHERE player_id = ? AND tier_points < 0";

      try (
         Connection var3 = this.databaseManager
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String();
         PreparedStatement var4 = var3.prepareStatement(var2);
      ) {
         var4.setString(1, var1.toString());
         var4.executeUpdate();
      } catch (SQLException var11) {
         this.plugin.getLogger().severe("Failed to clamp negative ranked data: " + var11.getMessage());
      }
   }

   public int getEloPerWin() {
      return this.eloPerWin;
   }

   public int getEloPerLoss() {
      return this.eloPerLoss;
   }

   public void addWin(UUID var1) {
      c var2 = this.getRankedData(var1);
      int var3 = var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float();
      String var4 = this.getTierNameByPoints(var3);
      var2.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if();
      int var5 = this.eloPerWin
         + this.winStreakBonus(
            var2.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
         )
         + this.permissionWinBonus(var1, false);
      var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
         var5
      );
      int var6 = var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float();
      String var7 = this.getTierNameByPoints(var6);
      this.saveRankedData(var2);
      Player var8 = Bukkit.getPlayer(var1);
      if (var8 != null && var8.isOnline()) {
         this.tierCommandsManager
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var8, var3, var6
            );
         this.notifyTierChange(var8, var3, var6, var4, var7);
      }
   }

   public void addLoss(UUID var1) {
      c var2 = this.getRankedData(var1);
      int var3 = var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float();
      String var4 = this.getTierNameByPoints(var3);
      var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      int var5 = -(
         this.eloPerLoss
            + this.loseStreakPenalty(
               var2.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null()
            )
      );
      var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
         var5
      );
      int var6 = var2.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float();
      String var7 = this.getTierNameByPoints(var6);
      this.saveRankedData(var2);
      Player var8 = Bukkit.getPlayer(var1);
      if (var8 != null && var8.isOnline()) {
         this.tierCommandsManager
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var8, var3, var6
            );
         this.notifyTierChange(var8, var3, var6, var4, var7);
      }
   }

   public RankedManager._e applyRankedResult(UUID var1, UUID var2) {
      return this.applyRankedResult(var1, var2, this.getGlobalElo(var1), this.getGlobalElo(var2));
   }

   public RankedManager._e applyRankedResult(UUID var1, UUID var2, int var3, int var4) {
      return this.applyRankedResult(var1, var2, var3, var4, true);
   }

   public RankedManager._e applyRankedResult(UUID var1, UUID var2, int var3, int var4, boolean var5) {
      c var6 = this.getRankedData(var1);
      c var7 = this.getRankedData(var2);
      boolean var8 = this.isInPlacements(var6);
      boolean var9 = this.isInPlacements(var7);
      String var10 = this.getTierNameByPoints(var3);
      String var11 = this.getTierNameByPoints(var4);
      int var12 = this.calculateEloDelta(var3, var4, true, var8);
      int var14 = -this.calculateEloDelta(var4, var3, false, var9);
      var6.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if();
      var7.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      int var15 = this.winStreakBonus(
         var6.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
      );
      int var16 = this.permissionWinBonus(var1, false);
      int var13 = var12 + var15 + var16;
      var14 -= this.loseStreakPenalty(
         var7.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null()
      );
      this.logEloBreakdown("global", var1, var3, var4, var12, var15, var16, var13);
      var6.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
         var13
      );
      var7.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
         var14
      );
      this.saveRankedData(var6);
      this.saveRankedData(var7);
      int var17 = var6.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float();
      int var18 = var7.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float();
      if (var5) {
         this.handleTierChange(var1, var3, var17, var10);
         this.handleTierChange(var2, var4, var18, var11);
      }

      RankedManager._d var19 = new RankedManager._d(
         var3,
         var17,
         var13,
         var8,
         Math.max(
            0,
            this.placementMatches
               - var6.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
         )
      );
      RankedManager._d var20 = new RankedManager._d(
         var4,
         var18,
         var14,
         var9,
         Math.max(
            0,
            this.placementMatches
               - var7.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
         )
      );
      return new RankedManager._e(var19, var20);
   }

   public RankedManager._e calculateKitRatingUpdate(UUID var1, UUID var2, String var3) {
      int var4 = this.plugin.getKitsRankManager() != null
         ? this.plugin
            .getKitsRankManager()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var1, var3
            )
         : this.getRankedData(var1)
            .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float();
      int var5 = this.plugin.getKitsRankManager() != null
         ? this.plugin
            .getKitsRankManager()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var2, var3
            )
         : this.getRankedData(var2)
            .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float();
      boolean var6 = this.isInPlacements(this.getRankedData(var1));
      boolean var7 = this.isInPlacements(this.getRankedData(var2));
      int var8 = this.calculateEloDelta(var4, var5, true, var6);
      int var10 = -this.calculateEloDelta(var5, var4, false, var7);
      int var11 = this.winStreakBonus(
         this.getRankedData(var1)
               .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
            + 1
      );
      int var12 = this.permissionWinBonus(var1, true);
      int var9 = var8 + var11 + var12;
      var10 -= this.loseStreakPenalty(
         this.getRankedData(var2)
               .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null()
            + 1
      );
      this.logEloBreakdown("kit:" + var3, var1, var4, var5, var8, var11, var12, var9);
      RankedManager._d var13 = new RankedManager._d(
         var4,
         Math.max(0, var4 + var9),
         var9,
         var6,
         Math.max(
            0,
            this.placementMatches
               - this.getRankedData(var1)
                  .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
         )
      );
      RankedManager._d var14 = new RankedManager._d(
         var5,
         Math.max(0, var5 + var10),
         var10,
         var7,
         Math.max(
            0,
            this.placementMatches
               - this.getRankedData(var2)
                  .õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
         )
      );
      return new RankedManager._e(var13, var14);
   }

   public void setLastRankedResult(UUID var1, int var2, int var3, String var4) {
      if (var1 != null) {
         this.lastRankedResults.put(var1, new RankedManager._f(var2, var3, var4));
      }
   }

   public RankedManager._f getLastRankedResult(UUID var1) {
      return var1 != null ? this.lastRankedResults.get(var1) : null;
   }

   private int calculateEloDelta(int var1, int var2, boolean var3, boolean var4) {
      int var5 = var4 ? this.placementKFactor : this.eloKFactor;
      double var6 = 1.0 / (1.0 + Math.pow(10.0, (double)(var2 - var1) / 400.0));
      double var8 = var3 ? 1.0 : 0.0;
      int var10 = (int)Math.round((double)var5 * Math.abs(var8 - var6));
      int var11 = var3 ? this.minEloGain : this.minEloLoss;
      int var12 = var3 ? this.maxEloGain : this.maxEloLoss;
      return Math.max(var11, Math.min(var12, var10));
   }

   private int winStreakBonus(int var1) {
      return this.winStreakExtraPoints > 0 && var1 >= this.minimumWinStreakNeeded ? this.winStreakExtraPoints : 0;
   }

   private int permissionWinBonus(UUID var1, boolean var2) {
      String var3 = var2 ? "kit" : "global";
      if (!this.cachedPermissionWinBonusEnabled) {
         this.logPermissionBonus(var1, var3, "skipped: feature disabled");
         return 0;
      } else if (var2 && !this.cachedPermissionBonusApplyKit) {
         this.logPermissionBonus(var1, var3, "skipped: apply-to-kit disabled");
         return 0;
      } else if (!var2 && !this.cachedPermissionBonusApplyGlobal) {
         this.logPermissionBonus(var1, var3, "skipped: apply-to-global disabled");
         return 0;
      } else {
         Player var4 = Bukkit.getPlayer(var1);
         if (var4 != null && var4.isOnline()) {
            int var5 = 0;

            for (RankedManager._g var7 : this.cachedPermissionEloBonuses) {
               boolean var8 = var4.hasPermission(
                  var7.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
               );
               this.logPermissionBonus(
                  var1,
                  var3,
                  "permission="
                     + var7.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
                     + " granted="
                     + var8
                     + " configured-bonus="
                     + var7.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
               );
               if (var8) {
                  if (this.cachedPermissionBonusesStack) {
                     var5 += var7.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
                  } else {
                     var5 = Math.max(
                        var5,
                        var7.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
                     );
                  }
               }
            }

            this.logPermissionBonus(
               var1, var3, "result=" + var5 + " stack=" + this.cachedPermissionBonusesStack + " configured-entries=" + this.cachedPermissionEloBonuses.size()
            );
            return var5;
         } else {
            this.logPermissionBonus(var1, var3, "skipped: player offline");
            return 0;
         }
      }
   }

   private void logPermissionBonus(UUID var1, String var2, String var3) {
      if (this.plugin.isDebug()) {
         this.plugin.getLogger().info("[Ranked ELO Debug] player=" + var1 + " scope=" + var2 + " " + var3);
      }
   }

   private void logEloBreakdown(String var1, UUID var2, int var3, int var4, int var5, int var6, int var7, int var8) {
      if (this.plugin.isDebug()) {
         this.plugin
            .getLogger()
            .info(
               "[Ranked ELO Debug] scope="
                  + var1
                  + " winner="
                  + var2
                  + " winner-old="
                  + var3
                  + " opponent-old="
                  + var4
                  + " base="
                  + var5
                  + " streak="
                  + var6
                  + " permission="
                  + var7
                  + " final="
                  + var8
            );
      }
   }

   private int loseStreakPenalty(int var1) {
      return this.loseStreakExtraLoss > 0 && var1 >= this.minimumLoseStreakNeeded ? this.loseStreakExtraLoss : 0;
   }

   private boolean isInPlacements(c var1) {
      return var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
         < this.placementMatches;
   }

   private void handleTierChange(UUID var1, int var2, int var3, String var4) {
      Player var5 = Bukkit.getPlayer(var1);
      if (var5 != null && var5.isOnline()) {
         this.tierCommandsManager
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var5, var2, var3
            );
         String var6 = this.getTierNameByPoints(var3);
         this.notifyTierChange(var5, var2, var3, var4, var6);
      }
   }

   public void notifyTierChange(Player var1, int var2, int var3, String var4, String var5) {
      if (var1 != null && var1.isOnline() && var4 != null && var5 != null && !var4.equals(var5)) {
         HashMap var6 = new HashMap();
         var6.put("old_tier", var4);
         var6.put("new_tier", var5);
         if (var3 > var2) {
            this.plugin
               .getMessagesManager()
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var1, "ranked-tier-up-title", var6
               );
            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1, "ranked-tier-up-message", var6
               );
            if (this.cachedTierUpSound != null) {
               this.cachedTierUpSound
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1
                  );
            }
         } else {
            this.plugin
               .getMessagesManager()
               .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                  var1, "ranked-tier-down-title", var6
               );
            this.plugin
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1, "ranked-tier-down-message", var6
               );
            if (this.cachedTierDownSound != null) {
               this.cachedTierDownSound
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1
                  );
            }
         }
      }
   }

   public void setPoints(UUID var1, int var2) {
      c var3 = this.getRankedData(var1);
      int var4 = var3.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float();
      String var5 = this.getTierNameByPoints(var4);
      var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
         var2
      );
      int var6 = var3.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float();
      String var7 = this.getTierNameByPoints(var6);
      this.saveRankedData(var3);
      Player var8 = Bukkit.getPlayer(var1);
      if (var8 != null && var8.isOnline()) {
         this.tierCommandsManager
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var8, var4, var6
            );
         this.notifyTierChange(var8, var4, var6, var5, var7);
      }
   }

   public void resetRankedProgress(UUID var1) {
      this.leaveQueue(var1);
      c var2 = this.getRankedData(var1);
      var2.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      this.lastRankedResults.remove(var1);
      this.saveRankedData(var2);
   }

   public String getTierName(UUID var1) {
      int var2 = this.getGlobalElo(var1);
      return var2 <= this.getStartingTier() ? "§7Unranked" : this.getTierNameByPoints(var2);
   }

   public int getGlobalElo(UUID var1) {
      return this.plugin.getKitsRankManager() != null
            && this.plugin
               .getKitsRankManager()
               .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
         ? this.plugin
            .getKitsRankManager()
            .Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return(
               var1
            )
         : this.getRankedData(var1)
            .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float();
   }

   public int getStoredGlobalElo(UUID var1) {
      if (this.plugin.getKitsRankManager() != null
         && this.plugin
            .getKitsRankManager()
            .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
         )
       {
         return this.plugin
            .getKitsRankManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var1
            );
      } else {
         int var2 = this.getStoredGlobalPointsForMigration(var1, true);
         return var2 > 0 ? var2 : this.getStartingTier();
      }
   }

   int getStoredGlobalPointsForMigration(UUID var1, boolean var2) {
      c var3 = this.rankedData.get(var1);
      if (var3 != null) {
         return Math.max(
            0,
            var3.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
         );
      } else {
         if (var2) {
            c var4 = this.loadRankedData(var1);
            if (var4 != null) {
               return Math.max(
                  0,
                  var4.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float()
               );
            }
         }

         return 0;
      }
   }

   public String getTierNameByPoints(int var1) {
      for (RankedManager._b var3 : this.cachedTiers) {
         if (var1
               >= var3.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
            && var1
               <= var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
            )
          {
            return var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
         }
      }

      return "§7Unranked";
   }

   public String getNextTierNameByPoints(int var1) {
      RankedManager._b var2 = null;

      for (RankedManager._b var4 : this.cachedTiers) {
         if (var4.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
               > var1
            && (
               var2 == null
                  || var4.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
                     < var2.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
            )) {
            var2 = var4;
         }
      }

      return var2 != null
         ? var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
         : this.getTierNameByPoints(var1);
   }

   public int getCurrentTierMinByPoints(int var1) {
      for (RankedManager._b var3 : this.cachedTiers) {
         if (var1
               >= var3.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
            && var1
               <= var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
            )
          {
            return var3.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object;
         }
      }

      return 0;
   }

   public int getNextTierRequiredPoints(int var1) {
      RankedManager._b var2 = null;
      RankedManager._b var3 = null;

      for (RankedManager._b var5 : this.cachedTiers) {
         if (var1
               >= var5.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
            && var1
               <= var5.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
            )
          {
            var3 = var5;
         }

         if (var5.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
               > var1
            && (
               var2 == null
                  || var5.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
                     < var2.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
            )) {
            var2 = var5;
         }
      }

      if (var2 != null) {
         return var2.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object;
      } else {
         return var3 != null
            ? var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
            : var1;
      }
   }

   public void sendRankedStats(Player var1, String var2) {
      if (var1 != null && var1.isOnline()) {
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "ranked-stats", this.buildRankedStatsPlaceholders(var1, var2)
            );
      }
   }

   public Map<String, String> buildRankedStatsPlaceholders(Player var1, String var2) {
      c var3 = this.getRankedData(var1.getUniqueId());
      int var4 = this.getGlobalElo(var1.getUniqueId());
      String var5 = var4 <= this.getStartingTier() ? "§7Unranked" : this.getTierNameByPoints(var4);
      String var6 = this.getNextTierNameByPoints(var4);
      int var7 = this.getCurrentTierMinByPoints(var4);
      int var8 = this.getNextTierRequiredPoints(var4);
      int var9 = this.calculateProgressPercent(var4, var7, var8);
      org.lime.swiftCore.ab.e var10 = this.plugin
         .getStatsManager()
         .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
            var1.getUniqueId()
         );
      String var11 = this.normalizeRankedKitName(var2);
      int var12 = var11.isEmpty()
         ? var10.oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000voidsuper()
            .values()
            .stream()
            .mapToInt(Integer::intValue)
            .max()
            .orElse(0)
         : var10.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            var11
         );
      double var13 = this.calculateKitWinRate(var10, var11);
      HashMap var15 = new HashMap();
      var15.put("player", var1.getName());
      var15.put("tier", var5);
      var15.put("next_tier", var6);
      var15.put("points", String.valueOf(var4));
      var15.put("next_points", String.valueOf(var8));
      var15.put("progress_bar", this.buildProgressBar(var9));
      var15.put("progress_percent", String.valueOf(var9));
      var15.put(
         "global_winstreak",
         String.valueOf(
            var10.Õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000floatsuper()
         )
      );
      var15.put(
         "global_best_winstreak",
         String.valueOf(
            var10.Õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000floatsuper()
         )
      );
      var15.put("kit_winstreak", String.valueOf(var12));
      var15.put("kit_best_winstreak", String.valueOf(var12));
      var15.put("kit_winrate", String.format(Locale.US, "%.2f%%", var13));
      var15.put(
         "wins",
         String.valueOf(
            var3.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object()
         )
      );
      var15.put(
         "losses",
         String.valueOf(
            var3.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void()
         )
      );
      var15.put(
         "winrate",
         String.format(
            Locale.US,
            "%.1f%%",
            var3.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
         )
      );
      var15.put(
         "matches",
         String.valueOf(
            var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
         )
      );
      var15.put(
         "placements_remaining",
         String.valueOf(
            Math.max(
               0,
               this.placementMatches
                  - var3.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int()
            )
         )
      );
      var15.put("placement_matches", String.valueOf(this.placementMatches));
      return var15;
   }

   private int calculateProgressPercent(int var1, int var2, int var3) {
      if (var3 <= var2) {
         return 100;
      } else {
         int var4 = var1 - var2;
         int var5 = var3 - var2;
         return Math.max(0, Math.min(100, (int)Math.round((double)var4 * 100.0 / (double)var5)));
      }
   }

   public int getProgressPercent(UUID var1) {
      if (var1 == null) {
         return 0;
      } else {
         int var2 = this.getGlobalElo(var1);
         return this.calculateProgressPercent(var2, this.getCurrentTierMinByPoints(var2), this.getNextTierRequiredPoints(var2));
      }
   }

   public String getProgressBar(UUID var1) {
      return this.buildProgressBar(this.getProgressPercent(var1));
   }

   private String buildProgressBar(int var1) {
      byte var2 = 10;
      int var3 = Math.max(0, Math.min(var2, (int)Math.round((double)var1 / 10.0)));
      return "■".repeat(var3) + "&7" + "■".repeat(var2 - var3);
   }

   private double calculateKitWinRate(org.lime.swiftCore.ab.e var1, String var2) {
      int var3;
      int var4;
      if (var2 != null && !var2.isEmpty()) {
         var3 = var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
            .getOrDefault(var2, 0);
         var4 = var1.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
            .getOrDefault(var2, 0);
      } else {
         var3 = var1.ØO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000supersuper()
            .values()
            .stream()
            .mapToInt(Integer::intValue)
            .sum();
         var4 = var1.öO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Stringsuper()
            .values()
            .stream()
            .mapToInt(Integer::intValue)
            .sum();
      }

      int var5 = var3 + var4;
      return var5 == 0 ? 0.0 : (double)var3 * 100.0 / (double)var5;
   }

   private String normalizeRankedKitName(String var1) {
      if (var1 == null) {
         return "";
      } else {
         String var2 = var1.toLowerCase(Locale.ROOT);
         return var2.startsWith("tier") ? var2.substring(4) : var2;
      }
   }

   public int getStartingTier() {
      return this.plugin.getConfig().getInt("tier-ranks.starting-tier", 1000);
   }

   public boolean isRankedEnabled() {
      return this.plugin.getConfig().getBoolean("tier-ranks.enable", true);
   }

   public void joinQueue(Player var1, String var2) {
      if (!this.isRankedEnabled()) {
         var1.sendMessage(Component.text("Ranked is disabled!").color(NamedTextColor.RED));
      } else if (this.plugin.getQueueManager().hasMatchFound(var1.getUniqueId())) {
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "queue-already-in"
            );
      } else if (this.isBusyForRankedQueue(var1)) {
         this.plugin
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, "queue-already-in-match"
            );
      } else {
         String var3 = var2.toLowerCase().startsWith("tier") ? var2 : "tier" + var2;
         if (this.plugin
               .getKitManager()
               .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                  var3
               )
            == null) {
            var1.sendMessage(Component.text("Ranked kit not found: " + var3).color(NamedTextColor.RED));
         } else if (!m.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            this.plugin, var1, var3
         )) {
            if (!ColorPartyKitGuard.blockRankedQueueUse(this.plugin, var1, var3)) {
               if (!w.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  this.plugin, var1, var3, "ranked"
               )) {
                  if (this.plugin
                     .getKitManager()
                     .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                        var1, var3
                     )) {
                     if (this.plugin.getSpectatorManager() != null
                        && this.plugin
                           .getSpectatorManager()
                           .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                              var1.getUniqueId()
                           )) {
                        this.plugin
                           .getMessagesManager()
                           .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                              var1, "queue-already-in-match"
                           );
                     } else {
                        UUID var4 = var1.getUniqueId();
                        if (this.plugin.getTeamQueueManager() != null
                           && this.plugin
                              .getTeamQueueManager()
                              .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                                 var4
                              )) {
                           this.plugin
                              .getMessagesManager()
                              .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                 var1, "queue-already-in"
                              );
                        } else {
                           boolean var5 = this.isInQueue(var4);
                           this.plugin.getQueueManager().removeFromAllQueues(var4);
                           Set var6 = this.playerQueues.computeIfAbsent(var4, var0 -> ConcurrentHashMap.newKeySet());
                           if (!var6.contains(var3)) {
                              if (!this.plugin
                                    .getMultiQueueManager()
                                    .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                                       true
                                    )
                                 && !var6.isEmpty()) {
                                 this.claimQueuesForMatch(var4);
                                 var6 = this.playerQueues.computeIfAbsent(var4, var0 -> ConcurrentHashMap.newKeySet());
                              }

                              if (!this.plugin
                                 .getMultiQueueManager()
                                 .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                    true, var6.size()
                                 )) {
                                 this.plugin
                                    .getMessagesManager()
                                    .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                       var1,
                                       "multi-queue-limit-reached",
                                       Map.of(
                                          "limit",
                                          String.valueOf(
                                             this.plugin
                                                .getMultiQueueManager()
                                                .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                                   true
                                                )
                                          )
                                       )
                                    );
                              } else {
                                 List var8 = this.rankedQueues.computeIfAbsent(var3, var0 -> new LinkedList<>());
                                 if (!var8.contains(var4)) {
                                    var8.add(var4);
                                 }

                                 var6.add(var3);
                                 this.queueJoinTimes.computeIfAbsent(var4, var0 -> new ConcurrentHashMap<>()).put(var3, System.currentTimeMillis());
                                 HashMap var9 = new HashMap<>(
                                    this.plugin
                                       .getMultiQueueManager()
                                       .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                          var1, true, var3
                                       )
                                 );
                                 var9.put("players", String.valueOf(var8.size()));
                                 this.plugin
                                    .getMessagesManager()
                                    .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                       var1,
                                       this.plugin
                                             .getMultiQueueManager()
                                             .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                                                true
                                             )
                                          ? "multi-queue-joined"
                                          : "ranked-queue-joined",
                                       var9
                                    );
                                 this.plugin.getScoreboardManager().setState(var1, ScoreboardState.QUEUE);
                                 this.plugin.getScoreboardManager().setPlaceholder(var1, "in_queue", "true");
                                 String var10 = this.plugin
                                    .getMultiQueueManager()
                                    .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                       var1, true
                                    );
                                 this.plugin.getScoreboardManager().setPlaceholder(var1, "kit", var10);
                                 this.plugin.getScoreboardManager().setPlaceholder(var1, "in_queue_kitname", var10);
                                 if (!var5) {
                                    this.plugin.getSpawnItemsManager().giveSpawnItems(var1, "queue", false, false);
                                 }

                                 this.plugin
                                    .getDuelManager()
                                    .getMatchFoundEffects()
                                    .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                       var1, var10, var3, true
                                    );
                                 if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
                                    String var11 = var1.getName();
                                    if (this.plugin.isDebug()) {
                                       this.plugin
                                          .getLogger()
                                          .info(
                                             "[CrossServer Debug] Adding "
                                                + var11
                                                + " to GLOBAL ranked queue '"
                                                + var3
                                                + "' (cross-server enabled). Local ranked matchmaking skipped."
                                          );
                                    }

                                    this.plugin
                                       .getLimboManager()
                                       .getGlobalQueueManager()
                                       .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                                          var1.getUniqueId(), var11, var3, true
                                       );
                                 } else {
                                    this.tryMatchPlayers(var3);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public void leaveQueue(UUID var1) {
      this.removeAllQueueMemberships(var1, true);
   }

   public void claimQueuesForMatch(UUID var1) {
      this.removeAllQueueMemberships(var1, false);
   }

   private void removeAllQueueMemberships(UUID var1, boolean var2) {
      Set var3 = this.playerQueues.remove(var1);
      this.queueJoinTimes.remove(var1);
      if (var3 != null) {
         for (String var5 : new HashSet(var3)) {
            List var6 = this.rankedQueues.get(var5);
            if (var6 != null) {
               var6.remove(var1);
            }

            if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
               this.plugin
                  .getLimboManager()
                  .getGlobalQueueManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var1, var5, true
                  );
            }
         }
      }

      if (var2) {
         Player var7 = Bukkit.getPlayer(var1);
         if (var7 != null && var7.isOnline()) {
            this.plugin.getScoreboardManager().setState(var7, ScoreboardState.DEFAULT);
            this.plugin.getScoreboardManager().setPlaceholder(var7, "in_queue", "false");
            this.plugin.getSpawnItemsManager().giveSpawnItems(var7, "default", false, false);
         }
      }

      if (this.plugin.getDuelManager() != null) {
         this.plugin
            .getDuelManager()
            .getMatchFoundEffects()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var1
            );
      }
   }

   public boolean leaveQueue(UUID var1, String var2) {
      String var3 = var2.toLowerCase().startsWith("tier") ? var2.toLowerCase() : "tier" + var2.toLowerCase();
      List var4 = this.rankedQueues.get(var3);
      boolean var5 = var4 != null && var4.remove(var1);
      if (!var5) {
         return false;
      } else {
         this.removeMembership(var1, var3);
         if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
            this.plugin
               .getLimboManager()
               .getGlobalQueueManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1, var3, true
               );
         }

         if (!this.isInQueue(var1)) {
            Player var6 = Bukkit.getPlayer(var1);
            if (var6 != null && var6.isOnline()) {
               this.plugin.getScoreboardManager().setState(var6, ScoreboardState.DEFAULT);
               this.plugin.getScoreboardManager().setPlaceholder(var6, "in_queue", "false");
               this.plugin.getSpawnItemsManager().giveSpawnItems(var6, "default", false, false);
            }

            this.plugin
               .getDuelManager()
               .getMatchFoundEffects()
               .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                  var1
               );
         } else {
            Player var9 = Bukkit.getPlayer(var1);
            String var7 = this.getPlayerQueue(var1).orElse(var3);
            if (var9 != null && var9.isOnline()) {
               String var8 = this.plugin
                  .getMultiQueueManager()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var9, true
                  );
               this.plugin.getScoreboardManager().setPlaceholder(var9, "kit", var8);
               this.plugin.getScoreboardManager().setPlaceholder(var9, "in_queue_kitname", var8);
               this.plugin
                  .getDuelManager()
                  .getMatchFoundEffects()
                  .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                     var9, var8, var7, true
                  );
            }
         }

         return true;
      }
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
      String var3 = var2.toLowerCase().startsWith("tier") ? var2.toLowerCase() : "tier" + var2.toLowerCase();
      Long var4 = this.queueJoinTimes.getOrDefault(var1, Map.of()).get(var3);
      return var4 == null ? 0L : System.currentTimeMillis() - var4;
   }

   private void tryMatchPlayers(String var1) {
      List var2 = this.rankedQueues.get(var1);
      if (var2 != null && var2.size() >= 2) {
         this.cleanupQueue(var2, var1);
         RankedManager._c var3 = this.findBestMatch(var2, var1);
         if (var3 == null) {
            this.scheduleQueueCheck(var1);
         } else {
            UUID var4 = var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
            UUID var5 = var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
            if (var4.equals(var5)) {
               var2.removeIf(var4::equals);
               this.removeMembership(var4, var1);
            } else {
               Player var6 = Bukkit.getPlayer(var4);
               Player var7 = Bukkit.getPlayer(var5);
               if (var6 != null && var6.isOnline() && var7 != null && var7.isOnline()) {
                  if (this.isBusyForRankedQueue(var6) || this.isBusyForRankedQueue(var7) || this.isSpectating(var4) || this.isSpectating(var5)) {
                     this.removeInvalidCandidate(var2, var4, var5);
                  } else if (!this.plugin.getQueueManager().hasMatchFound(var4)
                     && !this.plugin.getQueueManager().hasMatchFound(var5)
                     && this.plugin
                        .getMultiQueueManager()
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                           var4, var5
                        )) {
                     long var8 = this.getWaitTime(var4, var1);
                     long var10 = this.getWaitTime(var5, var1);
                     this.plugin
                        .getQueueMatchTimeTracker()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var1, var8, var10
                        );
                     Map var12 = this.buildMatchFoundPlaceholders(var6, var7, var1);
                     Map var13 = this.buildMatchFoundPlaceholders(var7, var6, var1);
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
                     this.plugin.getQueueManager().addFightingPlayer(var4, var1);
                     this.plugin.getQueueManager().addFightingPlayer(var5, var1);
                     this.plugin
                        .getMultiQueueManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var4, var5
                        );
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
                     this.plugin
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var6,
                           this.plugin
                              .getMultiQueueManager()
                              .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                                 true
                              ),
                           var12
                        );
                     this.plugin
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var7,
                           this.plugin
                              .getMultiQueueManager()
                              .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                                 true
                              ),
                           var13
                        );
                     this.plugin
                        .getDuelManager()
                        .getMatchFoundEffects()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var6, var12
                        );
                     this.plugin
                        .getDuelManager()
                        .getMatchFoundEffects()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var7, var13
                        );
                     this.plugin.getScoreboardManager().setPlaceholder(var6, "in_queue", "false");
                     this.plugin.getScoreboardManager().setPlaceholder(var7, "in_queue", "false");
                     t var14 = this.plugin
                        .getKitManager()
                        .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
                           var1
                        );
                     ArenaType var15 = var14 != null
                        ? var14.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                        : null;
                     org.lime.swiftCore.arena.d var16 = this.plugin
                        .getArenaManager()
                        .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                           var15, var1
                        );
                     if (var16 == null) {
                        String var18 = var4.compareTo(var5) < 0 ? var4 + ":" + var5 : var5 + ":" + var4;
                        if (!this.pendingArenaMatches.add(var18)) {
                           this.plugin.getQueueManager().removeFightingPlayer(var4);
                           this.plugin.getQueueManager().removeFightingPlayer(var5);
                        } else {
                           this.plugin
                              .getDynamicArenaManager()
                              .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
                                 var15, var1
                              )
                              .whenComplete(
                                 (var6x, var7x) -> Bukkit.getScheduler()
                                       .runTask(
                                          this.plugin,
                                          () -> {
                                             this.pendingArenaMatches.remove(var18);
                                             Player var8x = Bukkit.getPlayer(var4);
                                             Player var9 = Bukkit.getPlayer(var5);
                                             if (var7x != null) {
                                                this.handleNoRankedArena(var2, var4, var5, var1, var8x, var9);
                                             } else if (var8x != null
                                                && var8x.isOnline()
                                                && var9 != null
                                                && var9.isOnline()
                                                && !this.isBusyForRankedQueue(var8x)
                                                && !this.isBusyForRankedQueue(var9)
                                                && !this.isSpectating(var4)
                                                && !this.isSpectating(var5)
                                                && !this.isInQueue(var4)
                                                && !this.isInQueue(var5)
                                                && !this.plugin.getQueueManager().isInAnyQueue(var4)
                                                && !this.plugin.getQueueManager().isInAnyQueue(var5)) {
                                                int var10x = this.plugin.getDuelManager() != null
                                                   ? this.plugin.getDuelManager().getQueueDefaultRounds(var1, true)
                                                   : Math.max(1, this.plugin.getConfig().getInt("ranked.rounds", 1));
                                                this.plugin.getDuelManager().startRankedMatch(var8x, var9, var1, var10x, var6x);
                                             } else {
                                                this.plugin.getQueueManager().removeFightingPlayer(var4);
                                                this.plugin.getQueueManager().removeFightingPlayer(var5);
                                                this.plugin
                                                   .getDynamicArenaManager()
                                                   .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                                      var6x
                                                   );
                                             }
                                          }
                                       )
                              );
                        }
                     } else {
                        this.plugin
                           .getArenaManager()
                           .ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float(
                              var16
                           );
                        int var17 = this.plugin.getDuelManager() != null
                           ? this.plugin.getDuelManager().getQueueDefaultRounds(var1, true)
                           : Math.max(1, this.plugin.getConfig().getInt("ranked.rounds", 1));
                        Bukkit.getScheduler().runTask(this.plugin, () -> this.plugin.getDuelManager().startRankedMatch(var6, var7, var1, var17, var16));
                     }
                  }
               } else {
                  if (var6 == null || !var6.isOnline()) {
                     this.removeMembership(var4, var1);
                  } else if (!var2.contains(var4)) {
                     var2.add(var4);
                  }

                  if (var7 == null || !var7.isOnline()) {
                     this.removeMembership(var5, var1);
                  } else if (!var2.contains(var5)) {
                     var2.add(var5);
                  }
               }
            }
         }
      }
   }

   private Map<String, String> buildMatchFoundPlaceholders(Player var1, Player var2, String var3) {
      HashMap var4 = new HashMap<>(
         this.plugin
            .getMultiQueueManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, true, var3
            )
      );
      var4.put(
         "opponent",
         this.plugin
            .getPlayerSettingsManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1, var2
            )
      );
      String var5 = this.plugin
         .getDuelManager()
         .getMatchFoundEffects()
         .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
            var3
         );
      var4.put("kit_sprite", var5);
      var4.put("queue_sprite", var5);
      var4.put("sprite", var5);
      return var4;
   }

   private void handleNoRankedArena(List<UUID> var1, UUID var2, UUID var3, String var4, Player var5, Player var6) {
      this.plugin.getQueueManager().removeFightingPlayer(var2);
      this.plugin.getQueueManager().removeFightingPlayer(var3);
      if (var5 != null && var5.isOnline()) {
         var5.sendMessage(Component.text("No arenas available for ranked!").color(NamedTextColor.RED));
         if (!var1.contains(var2)) {
            var1.add(var2);
         }

         this.addMembership(var2, var4);
         this.plugin.getScoreboardManager().setPlaceholder(var5, "in_queue", "true");
         this.restoreRankedQueueUi(var5, var4);
      }

      if (var6 != null && var6.isOnline()) {
         var6.sendMessage(Component.text("No arenas available for ranked!").color(NamedTextColor.RED));
         if (!var1.contains(var3)) {
            var1.add(var3);
         }

         this.addMembership(var3, var4);
         this.plugin.getScoreboardManager().setPlaceholder(var6, "in_queue", "true");
         this.restoreRankedQueueUi(var6, var4);
      }
   }

   private void restoreRankedQueueUi(Player var1, String var2) {
      String var3 = this.plugin
         .getMultiQueueManager()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1, true
         );
      this.plugin.getScoreboardManager().setState(var1, ScoreboardState.QUEUE);
      this.plugin.getScoreboardManager().setPlaceholder(var1, "kit", var3);
      this.plugin.getScoreboardManager().setPlaceholder(var1, "in_queue_kitname", var3);
      this.plugin
         .getDuelManager()
         .getMatchFoundEffects()
         .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
            var1, var3, var2, true
         );
   }

   private void cleanupQueue(List<UUID> var1, String var2) {
      var1.removeIf(var2x -> {
         Player var3 = Bukkit.getPlayer(var2x);
         if (var3 != null && var3.isOnline()) {
            return false;
         } else {
            this.removeMembership(var2x, var2);
            return true;
         }
      });
   }

   private boolean isBusyForRankedQueue(Player var1) {
      UUID var2 = var1.getUniqueId();
      return this.plugin.getDuelManager().isInMatch(var2)
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

   private void removeInvalidCandidate(List<UUID> var1, UUID var2, UUID var3) {
      var1.removeIf(var2x -> var2x.equals(var2) || var2x.equals(var3));
      this.leaveQueue(var2);
      this.leaveQueue(var3);
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

   private RankedManager._c findBestMatch(List<UUID> var1, String var2) {
      RankedManager._c var3 = null;
      int var4 = Integer.MAX_VALUE;

      for (int var5 = 0; var5 < var1.size(); var5++) {
         UUID var6 = (UUID)var1.get(var5);
         int var7 = this.getMatchmakingRating(var6, var2);
         int var8 = this.getSearchRange(var6, var2);

         for (int var9 = var5 + 1; var9 < var1.size(); var9++) {
            UUID var10 = (UUID)var1.get(var9);
            if (!var6.equals(var10)) {
               int var11 = this.getMatchmakingRating(var10, var2);
               int var12 = Math.max(var8, this.getSearchRange(var10, var2));
               int var13 = Math.abs(var7 - var11);
               if (var13 <= var12 && var13 < var4) {
                  var3 = new RankedManager._c(var6, var10);
                  var4 = var13;
               }
            }
         }
      }

      return var3;
   }

   private int getMatchmakingRating(UUID var1, String var2) {
      int var3 = this.getGlobalElo(var1);
      if (this.plugin.getKitsRankManager() == null) {
         return var3;
      } else {
         int var4 = this.plugin
            .getKitsRankManager()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var1, var2
            );
         return (var3 + var4) / 2;
      }
   }

   private int getSearchRange(UUID var1, String var2) {
      long var3 = this.getWaitTime(var1, var2);
      int var5 = this.searchRangeStart + (int)(var3 / 1000L * (long)this.searchRangeExpandPerSecond);
      return Math.min(this.searchRangeMax, Math.max(this.searchRangeStart, var5));
   }

   private void scheduleQueueCheck(String var1) {
      if (this.pendingQueueChecks.add(var1)) {
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            this.pendingQueueChecks.remove(var1);
            this.tryMatchPlayers(var1);
         }, 100L);
      }
   }

   public boolean isInQueue(UUID var1) {
      Set var2 = this.playerQueues.get(var1);
      return var2 != null && !var2.isEmpty();
   }

   public boolean isInQueue(UUID var1, String var2) {
      String var3 = var2.toLowerCase().startsWith("tier") ? var2.toLowerCase() : "tier" + var2.toLowerCase();
      Set var4 = this.playerQueues.get(var1);
      return var4 != null && var4.contains(var3);
   }

   public Optional<String> getPlayerQueue(UUID var1) {
      return this.getPlayerQueues(var1).stream().findFirst();
   }

   public Set<String> getPlayerQueues(UUID var1) {
      Set var2 = this.playerQueues.get(var1);
      if (var2 == null) {
         return Set.of();
      } else {
         Map var3 = this.queueJoinTimes.getOrDefault(var1, Map.of());
         return var2.stream()
            .sorted(Comparator.comparingLong(var1x -> var3.getOrDefault(var1x, Long.MAX_VALUE)))
            .collect(Collectors.toCollection(LinkedHashSet::new));
      }
   }

   public int getQueueSize(String var1) {
      String var2 = var1.toLowerCase().startsWith("tier") ? var1.toLowerCase() : "tier" + var1.toLowerCase();
      List var3 = this.rankedQueues.get(var2);
      return var3 != null ? var3.size() : 0;
   }

   public int getTotalQueueSize() {
      int var1 = 0;

      for (List var3 : this.rankedQueues.values()) {
         var1 += var3.size();
      }

      return var1;
   }

   public Map<String, Integer> getQueuedKitCountsSnapshot() {
      HashMap var1 = new HashMap();

      for (Entry var3 : this.rankedQueues.entrySet()) {
         var1.put((String)var3.getKey(), ((List)var3.getValue()).size());
      }

      return var1;
   }

   public void invalidateCache(UUID var1) {
      this.rankedData.remove(var1);
   }

   public void handlePlayerQuit(Player var1) {
      UUID var2 = var1.getUniqueId();
      this.leaveQueue(var2);
      c var3 = this.rankedData.remove(var2);
      if (var3 != null) {
         this.saveRankedData(var3);
      }
   }

   private void addMembership(UUID var1, String var2) {
      this.playerQueues.computeIfAbsent(var1, var0 -> ConcurrentHashMap.newKeySet()).add(var2);
      this.queueJoinTimes.computeIfAbsent(var1, var0 -> new ConcurrentHashMap<>()).putIfAbsent(var2, System.currentTimeMillis());
   }

   private void removeMembership(UUID var1, String var2) {
      Set var3 = this.playerQueues.get(var1);
      if (var3 != null) {
         var3.remove(var2);
         if (var3.isEmpty()) {
            this.playerQueues.remove(var1);
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

   public e getTierCommandsManager() {
      return this.tierCommandsManager;
   }

   public void resyncGlobalQueueEntries() {
      if (this.plugin.getLimboManager() != null && this.plugin.getLimboManager().isCrossServer()) {
         org.lime.swiftCore.v.r.d var1 = this.plugin.getLimboManager().getGlobalQueueManager();
         if (var1 != null) {
            for (UUID var3 : new HashSet<>(this.playerQueues.keySet())) {
               if (this.isInQueue(var3)) {
                  Player var4 = Bukkit.getPlayer(var3);
                  if (var4 != null && var4.isOnline()) {
                     for (String var6 : this.getPlayerQueues(var3)) {
                        var1.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var3, var4.getName(), var6, true
                        );
                     }
                  }
               }
            }
         }
      }
   }

   private static record _b(
      int Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object,
      int o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super,
      String Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
   ) {
      public int Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new() {
         return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
      }

      public String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super() {
         return this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
      }
   }

   private static record _c(
      UUID Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new,
      UUID o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
   ) {
      public UUID o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super() {
         return this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
      }

      public UUID Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new() {
         return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
      }
   }

   public static record _d(
      int Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new,
      int Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String,
      int Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class,
      boolean o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super,
      int Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
   ) {
      public int o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super() {
         return this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String;
      }

      public boolean Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object() {
         return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
      }

      public int Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String() {
         return this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object;
      }
   }

   public static record _e(
      RankedManager._d o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super,
      RankedManager._d Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
   ) {
      public RankedManager._d Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new() {
         return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
      }

      public RankedManager._d o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super() {
         return this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
      }
   }

   public static record _f(
      int Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new,
      int Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object,
      String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
   ) {
      public int o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super() {
         return this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
      }

      public int Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new() {
         return this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object;
      }

      public String Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object() {
         return this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
      }
   }

   private static record _g(
      String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super,
      int Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
   ) {
   }
}
