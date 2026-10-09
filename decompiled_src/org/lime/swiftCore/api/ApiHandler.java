package org.lime.swiftCore.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.lime.swiftCore.SwiftCore;
import org.lime.swiftCore.y.c;

public class ApiHandler {
   private final SwiftCore plugin;
   private final c databaseManager;
   private final Gson gson;

   public ApiHandler(SwiftCore var1) {
      this.plugin = var1;
      this.databaseManager = var1.getDatabaseManager();
      this.gson = new Gson();
   }

   public JsonObject handlePlayerLookup(String var1) {
      UUID var2 = this.resolvePlayerUUID(var1);
      if (var2 == null) {
         return errorJson(404, "Player not found");
      } else {
         JsonObject var3 = new JsonObject();
         var3.addProperty("uuid", var2.toString());
         String var4 = this.resolvePlayerName(var2);
         var3.addProperty("name", var4 != null ? var4 : "Unknown");
         JsonObject var5 = this.loadPlayerStats(var2);
         if (var5 != null) {
            var3.add("stats", var5);
         }

         JsonObject var6 = this.loadRankedData(var2);
         if (var6 != null) {
            var3.add("ranked", var6);
         }

         JsonObject var7 = this.loadKitStats(var2);
         if (var7 != null) {
            var3.add("kits", var7);
         }

         return var3;
      }
   }

   public JsonObject handleGlobalLeaderboard(int var1, int var2) {
      var1 = Math.min(Math.max(var1, 1), 100);
      var2 = Math.max(var2, 0);
      JsonObject var3 = new JsonObject();
      var3.addProperty("type", "global");
      JsonArray var4 = new JsonArray();
      HashMap var5 = new HashMap();
      String var6 = "SELECT player_id, ranked_wins, ranked_losses, matches_played, peak_points FROM player_ranked";

      try (
         Connection var7 = this.databaseManager
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String();
         PreparedStatement var8 = var7.prepareStatement(var6);
      ) {
         ResultSet var9 = var8.executeQuery();

         while (var9.next()) {
            UUID var10 = this.parseUUID(var9.getString("player_id"));
            if (var10 != null) {
               var5.put(
                  var10,
                  new ApiHandler.RankedSummary(
                     var9.getInt("ranked_wins"), var9.getInt("ranked_losses"), var9.getInt("matches_played"), var9.getInt("peak_points")
                  )
               );
            }
         }
      } catch (SQLException var19) {
         this.plugin.getLogger().warning("[API] Failed to query global leaderboard: " + var19.getMessage());
         return errorJson(500, "Database error");
      }

      if (this.plugin.getKitsRankManager() != null) {
         for (UUID var27 : this.plugin
            .getKitsRankManager()
            .ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if()) {
            var5.putIfAbsent(var27, new ApiHandler.RankedSummary(0, 0, 0, 0));
         }
      }

      ArrayList var23 = new ArrayList();

      for (Entry var28 : var5.entrySet()) {
         UUID var30 = (UUID)var28.getKey();
         int var11 = this.getOverallElo(var30);
         var23.add(new ApiHandler.GlobalLeaderboardEntry(var30, var11, (ApiHandler.RankedSummary)var28.getValue()));
      }

      var23.sort(Comparator.comparingInt(ApiHandler.GlobalLeaderboardEntry::points).reversed());
      int var26 = var23.size();
      int var29 = Math.min(var2, var26);
      int var31 = Math.min(var29 + var1, var26);

      for (int var32 = var29; var32 < var31; var32++) {
         ApiHandler.GlobalLeaderboardEntry var12 = (ApiHandler.GlobalLeaderboardEntry)var23.get(var32);
         ApiHandler.RankedSummary var13 = var12.summary();
         JsonObject var14 = new JsonObject();
         var14.addProperty("rank", var32 + 1);
         var14.addProperty("uuid", var12.playerId().toString());
         var14.addProperty("name", this.resolvePlayerName(var12.playerId()));
         var14.addProperty("elo", var12.points());
         var14.addProperty("tier", this.getTierNameByPoints(var12.points()));
         var14.addProperty("wins", var13.wins());
         var14.addProperty("losses", var13.losses());
         var14.addProperty("matches_played", var13.matchesPlayed());
         var14.addProperty("peak_elo", Math.max(var13.peakElo(), var12.points()));
         var4.add(var14);
      }

      var3.add("entries", var4);
      var3.addProperty("total", var26);
      var3.addProperty("limit", var1);
      var3.addProperty("offset", var2);
      return var3;
   }

   public JsonObject handleKitLeaderboard(String var1, int var2, int var3) {
      var2 = Math.min(Math.max(var2, 1), 100);
      var3 = Math.max(var3, 0);
      String var4 = var1.toLowerCase();
      JsonObject var5 = new JsonObject();
      var5.addProperty("type", "kit");
      var5.addProperty("kit", var4);
      JsonArray var6 = new JsonArray();
      int var7 = 0;
      String var8 = "SELECT player_id, kit_stats FROM player_stats WHERE kit_stats IS NOT NULL AND kit_stats != ''";

      try (
         Connection var9 = this.databaseManager
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String();
         PreparedStatement var10 = var9.prepareStatement(var8);
      ) {
         ResultSet var11 = var10.executeQuery();
         ArrayList var12 = new ArrayList();

         while (var11.next()) {
            String var13 = var11.getString("player_id");
            String var14 = var11.getString("kit_stats");
            Map var15 = (Map)this.gson.fromJson(var14, (new TypeToken<Map<String, Map<String, Integer>>>() {
            }).getType());
            if (var15 != null) {
               int var16 = 0;
               int var17 = 0;
               int var18 = 0;
               int var19 = 0;
               if (var15.containsKey("tier_points")) {
                  var16 = ((Map)var15.get("tier_points")).getOrDefault(var4, 0);
               }

               if (var15.containsKey("wins")) {
                  var17 = ((Map)var15.get("wins")).getOrDefault(var4, 0);
               }

               if (var15.containsKey("losses")) {
                  var18 = ((Map)var15.get("losses")).getOrDefault(var4, 0);
               }

               if (var15.containsKey("streaks")) {
                  var19 = ((Map)var15.get("streaks")).getOrDefault(var4, 0);
               }

               if (var16 != 0 || var17 != 0 || var18 != 0) {
                  JsonObject var20 = new JsonObject();
                  var20.addProperty("uuid", var13);
                  var20.addProperty("name", this.resolvePlayerName(this.parseUUID(var13)));
                  var20.addProperty("elo", var16);
                  var20.addProperty("tier", this.getKitTierName(var4, var16));
                  var20.addProperty("wins", var17);
                  var20.addProperty("losses", var18);
                  var20.addProperty("win_streak", var19);
                  var12.add(var20);
               }
            }
         }

         var12.sort((var0, var1x) -> Integer.compare(var1x.get("elo").getAsInt(), var0.get("elo").getAsInt()));
         var7 = var12.size();
         int var29 = Math.min(var3 + var2, var12.size());

         for (int var30 = var3; var30 < var29; var30++) {
            JsonObject var31 = (JsonObject)var12.get(var30);
            var31.addProperty("rank", var30 + 1);
            var6.add(var31);
         }
      } catch (SQLException var25) {
         this.plugin.getLogger().warning("[API] Failed to query kit leaderboard: " + var25.getMessage());
         return errorJson(500, "Database error");
      }

      var5.add("entries", var6);
      var5.addProperty("total", var7);
      var5.addProperty("limit", var2);
      var5.addProperty("offset", var3);
      return var5;
   }

   public JsonObject handleKillsLeaderboard(int var1, int var2) {
      var1 = Math.min(Math.max(var1, 1), 100);
      var2 = Math.max(var2, 0);
      JsonObject var3 = new JsonObject();
      var3.addProperty("type", "kills");
      JsonArray var4 = new JsonArray();
      int var5 = 0;
      String var6 = "SELECT COUNT(*) FROM player_stats WHERE kills > 0";
      String var7 = "SELECT player_id, kills, deaths, duel_wins, duel_losses, win_streak, kill_streak\nFROM player_stats\nWHERE kills > 0\nORDER BY kills DESC\nLIMIT ? OFFSET ?\n";

      try (Connection var8 = this.databaseManager
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()) {
         try (PreparedStatement var9 = var8.prepareStatement(var6)) {
            ResultSet var10 = var9.executeQuery();
            if (var10.next()) {
               var5 = var10.getInt(1);
            }
         }

         try (PreparedStatement var25 = var8.prepareStatement(var7)) {
            var25.setInt(1, var1);
            var25.setInt(2, var2);
            ResultSet var26 = var25.executeQuery();
            int var11 = var2 + 1;

            while (var26.next()) {
               JsonObject var12 = new JsonObject();
               String var13 = var26.getString("player_id");
               int var14 = var26.getInt("kills");
               int var15 = var26.getInt("deaths");
               var12.addProperty("rank", var11++);
               var12.addProperty("uuid", var13);
               var12.addProperty("name", this.resolvePlayerName(this.parseUUID(var13)));
               var12.addProperty("kills", var14);
               var12.addProperty("deaths", var15);
               var12.addProperty("kd_ratio", var15 == 0 ? (double)var14 : (double)Math.round((double)var14 / (double)var15 * 100.0) / 100.0);
               var12.addProperty("win_streak", var26.getInt("win_streak"));
               var12.addProperty("kill_streak", var26.getInt("kill_streak"));
               var4.add(var12);
            }
         }
      } catch (SQLException var22) {
         this.plugin.getLogger().warning("[API] Failed to query kills leaderboard: " + var22.getMessage());
         return errorJson(500, "Database error");
      }

      var3.add("entries", var4);
      var3.addProperty("total", var5);
      var3.addProperty("limit", var1);
      var3.addProperty("offset", var2);
      return var3;
   }

   public JsonObject handleWinsLeaderboard(int var1, int var2) {
      var1 = Math.min(Math.max(var1, 1), 100);
      var2 = Math.max(var2, 0);
      JsonObject var3 = new JsonObject();
      var3.addProperty("type", "wins");
      JsonArray var4 = new JsonArray();
      int var5 = 0;
      String var6 = "SELECT COUNT(*) FROM player_stats WHERE duel_wins > 0";
      String var7 = "SELECT player_id, duel_wins, duel_losses, kills, deaths, win_streak\nFROM player_stats\nWHERE duel_wins > 0\nORDER BY duel_wins DESC\nLIMIT ? OFFSET ?\n";

      try (Connection var8 = this.databaseManager
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()) {
         try (PreparedStatement var9 = var8.prepareStatement(var6)) {
            ResultSet var10 = var9.executeQuery();
            if (var10.next()) {
               var5 = var10.getInt(1);
            }
         }

         try (PreparedStatement var25 = var8.prepareStatement(var7)) {
            var25.setInt(1, var1);
            var25.setInt(2, var2);
            ResultSet var26 = var25.executeQuery();
            int var11 = var2 + 1;

            while (var26.next()) {
               JsonObject var12 = new JsonObject();
               String var13 = var26.getString("player_id");
               int var14 = var26.getInt("duel_wins");
               int var15 = var26.getInt("duel_losses");
               var12.addProperty("rank", var11++);
               var12.addProperty("uuid", var13);
               var12.addProperty("name", this.resolvePlayerName(this.parseUUID(var13)));
               var12.addProperty("wins", var14);
               var12.addProperty("losses", var15);
               var12.addProperty("win_rate", var14 + var15 == 0 ? 0.0 : (double)Math.round((double)var14 / (double)(var14 + var15) * 10000.0) / 100.0);
               var12.addProperty("win_streak", var26.getInt("win_streak"));
               var4.add(var12);
            }
         }
      } catch (SQLException var22) {
         this.plugin.getLogger().warning("[API] Failed to query wins leaderboard: " + var22.getMessage());
         return errorJson(500, "Database error");
      }

      var3.add("entries", var4);
      var3.addProperty("total", var5);
      var3.addProperty("limit", var1);
      var3.addProperty("offset", var2);
      return var3;
   }

   public JsonObject handlePlayerMatchHistory(String var1, int var2, int var3, String var4, String var5) {
      UUID var6 = this.resolvePlayerUUID(var1);
      if (var6 == null) {
         return errorJson(404, "Player not found");
      } else {
         var2 = Math.min(Math.max(var2, 1), 100);
         var3 = Math.max(var3, 0);
         String var7 = var6.toString();
         Boolean var8 = this.parseBooleanFilter(var5);
         String var9 = var4 != null && !var4.isBlank() ? var4.trim() : null;
         StringBuilder var10 = new StringBuilder("(winner_uuid = ? OR loser_uuid = ?)");
         ArrayList var11 = new ArrayList();
         var11.add(var7);
         var11.add(var7);
         if (var9 != null) {
            var10.append(" AND LOWER(kit) = LOWER(?)");
            var11.add(var9);
         }

         if (var8 != null) {
            var10.append(" AND ranked = ?");
            var11.add(var8);
         }

         JsonObject var12 = new JsonObject();
         var12.addProperty("uuid", var7);
         var12.addProperty("name", this.resolvePlayerName(var6));
         var12.addProperty("limit", var2);
         var12.addProperty("offset", var3);
         if (var9 != null) {
            var12.addProperty("kit", var9);
         }

         if (var8 != null) {
            var12.addProperty("ranked", var8);
         }

         JsonArray var13 = new JsonArray();
         String var14 = "SELECT COUNT(*) FROM match_history WHERE " + var10;
         String var15 = "SELECT id, winner_uuid, winner_name, loser_uuid, loser_name, kit, arena, ranked, match_date FROM match_history WHERE "
            + var10
            + " ORDER BY match_date DESC LIMIT ? OFFSET ?";

         try (Connection var16 = this.databaseManager
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()) {
            try (PreparedStatement var17 = var16.prepareStatement(var14)) {
               this.bindParams(var17, var11);
               ResultSet var18 = var17.executeQuery();
               var12.addProperty("total", var18.next() ? var18.getInt(1) : 0);
            }

            try (PreparedStatement var30 = var16.prepareStatement(var15)) {
               int var31 = this.bindParams(var30, var11);
               var30.setInt(var31++, var2);
               var30.setInt(var31, var3);
               ResultSet var19 = var30.executeQuery();

               while (var19.next()) {
                  JsonObject var20 = new JsonObject();
                  var20.addProperty("match_id", var19.getLong("id"));
                  var20.addProperty("winner_uuid", var19.getString("winner_uuid"));
                  var20.addProperty("winner_name", var19.getString("winner_name"));
                  var20.addProperty("loser_uuid", var19.getString("loser_uuid"));
                  var20.addProperty("loser_name", var19.getString("loser_name"));
                  var20.addProperty("kit", var19.getString("kit"));
                  var20.addProperty("arena", var19.getString("arena"));
                  var20.addProperty("ranked", var19.getBoolean("ranked"));
                  var20.add("duration_seconds", JsonNull.INSTANCE);
                  var20.addProperty("match_date", var19.getLong("match_date"));
                  var20.addProperty("result", var7.equals(var19.getString("winner_uuid")) ? "win" : "loss");
                  var13.add(var20);
               }
            }
         } catch (SQLException var27) {
            this.plugin.getLogger().warning("[API] Failed to query player match history: " + var27.getMessage());
            return errorJson(500, "Database error");
         }

         var12.add("matches", var13);
         return var12;
      }
   }

   public JsonObject handlePlayerEloHistory(String var1, int var2, int var3, int var4, String var5, String var6) {
      UUID var7 = this.resolvePlayerUUID(var1);
      if (var7 == null) {
         return errorJson(404, "Player not found");
      } else {
         var2 = Math.min(Math.max(var2, 1), 365);
         var3 = Math.min(Math.max(var3, 1), 250);
         var4 = Math.max(var4, 0);
         long var8 = System.currentTimeMillis() - (long)var2 * 86400000L;
         String var10 = var7.toString();
         String var11 = var5 != null && !var5.isBlank() ? var5.trim() : null;
         String var12 = var6 != null && !var6.isBlank() ? var6.trim().toLowerCase() : null;
         StringBuilder var13 = new StringBuilder("player_id = ? AND match_date >= ?");
         ArrayList var14 = new ArrayList();
         var14.add(var10);
         var14.add(var8);
         if (var11 != null) {
            var13.append(" AND LOWER(kit) = LOWER(?)");
            var14.add(var11);
         }

         if (var12 != null) {
            var13.append(" AND LOWER(scope) = LOWER(?)");
            var14.add(var12);
         }

         JsonObject var15 = new JsonObject();
         var15.addProperty("uuid", var10);
         var15.addProperty("name", this.resolvePlayerName(var7));
         var15.addProperty("days", var2);
         var15.addProperty("since", var8);
         var15.addProperty("limit", var3);
         var15.addProperty("offset", var4);
         JsonArray var16 = new JsonArray();
         String var17 = "SELECT COUNT(*) FROM elo_rating_events WHERE " + var13;
         String var18 = "SELECT id, player_id, opponent_id, opponent_name, kit, scope, old_points, new_points, delta_points, match_date, match_id FROM elo_rating_events WHERE "
            + var13
            + " ORDER BY match_date DESC LIMIT ? OFFSET ?";

         try (Connection var19 = this.databaseManager
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()) {
            try (PreparedStatement var20 = var19.prepareStatement(var17)) {
               this.bindParams(var20, var14);
               ResultSet var21 = var20.executeQuery();
               var15.addProperty("total", var21.next() ? var21.getInt(1) : 0);
            }

            try (PreparedStatement var35 = var19.prepareStatement(var18)) {
               int var36 = this.bindParams(var35, var14);
               var35.setInt(var36++, var3);
               var35.setInt(var36, var4);
               ResultSet var22 = var35.executeQuery();

               while (var22.next()) {
                  JsonObject var23 = new JsonObject();
                  var23.addProperty("id", var22.getLong("id"));
                  var23.addProperty("player_uuid", var22.getString("player_id"));
                  var23.addProperty("opponent_uuid", var22.getString("opponent_id"));
                  var23.addProperty("opponent_name", var22.getString("opponent_name"));
                  var23.addProperty("kit", var22.getString("kit"));
                  var23.addProperty("scope", var22.getString("scope"));
                  var23.addProperty("old_points", var22.getInt("old_points"));
                  var23.addProperty("new_points", var22.getInt("new_points"));
                  var23.addProperty("delta_points", var22.getInt("delta_points"));
                  var23.addProperty("match_date", var22.getLong("match_date"));
                  String var24 = var22.getString("match_id");
                  if (var24 != null && !var24.isBlank()) {
                     var23.addProperty("match_id", var24);
                  } else {
                     var23.add("match_id", JsonNull.INSTANCE);
                  }

                  var16.add(var23);
               }
            }
         } catch (SQLException var31) {
            this.plugin.getLogger().warning("[API] Failed to query player ELO history: " + var31.getMessage());
            return errorJson(500, "Database error");
         }

         var15.add("events", var16);
         return var15;
      }
   }

   public JsonObject handlePlayerEloSummary(String var1, int var2) {
      UUID var3 = this.resolvePlayerUUID(var1);
      if (var3 == null) {
         return errorJson(404, "Player not found");
      } else {
         var2 = Math.min(Math.max(var2, 1), 365);
         long var4 = System.currentTimeMillis() - (long)var2 * 86400000L;
         String var6 = var3.toString();
         JsonObject var7 = new JsonObject();
         var7.addProperty("uuid", var6);
         var7.addProperty("name", this.resolvePlayerName(var3));
         var7.addProperty("days", var2);
         var7.addProperty("since", var4);
         var7.addProperty("current_elo", this.getOverallElo(var3));
         String var8 = "SELECT\n    COALESCE(SUM(delta_points), 0) AS delta_sum,\n    COUNT(*) AS matches_count,\n    COALESCE(MAX(delta_points), 0) AS best_delta,\n    COALESCE(MIN(delta_points), 0) AS worst_delta\nFROM elo_rating_events\nWHERE player_id = ? AND match_date >= ?\n";
         String var9 = "SELECT kit, scope, COALESCE(SUM(delta_points), 0) AS delta_sum, COUNT(*) AS events_count\nFROM elo_rating_events\nWHERE player_id = ? AND match_date >= ?\nGROUP BY kit, scope\nORDER BY delta_sum DESC\n";
         JsonArray var10 = new JsonArray();

         try (Connection var11 = this.databaseManager
               .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()) {
            try (PreparedStatement var12 = var11.prepareStatement(var8)) {
               var12.setString(1, var6);
               var12.setLong(2, var4);
               ResultSet var13 = var12.executeQuery();
               if (var13.next()) {
                  var7.addProperty("elo_delta", var13.getInt("delta_sum"));
                  var7.addProperty("events_count", var13.getInt("matches_count"));
                  var7.addProperty("best_delta", var13.getInt("best_delta"));
                  var7.addProperty("worst_delta", var13.getInt("worst_delta"));
               }
            }

            try (PreparedStatement var23 = var11.prepareStatement(var9)) {
               var23.setString(1, var6);
               var23.setLong(2, var4);
               ResultSet var24 = var23.executeQuery();

               while (var24.next()) {
                  JsonObject var14 = new JsonObject();
                  var14.addProperty("kit", var24.getString("kit"));
                  var14.addProperty("scope", var24.getString("scope"));
                  var14.addProperty("elo_delta", var24.getInt("delta_sum"));
                  var14.addProperty("events_count", var24.getInt("events_count"));
                  var10.add(var14);
               }
            }
         } catch (SQLException var21) {
            this.plugin.getLogger().warning("[API] Failed to query player ELO summary: " + var21.getMessage());
            return errorJson(500, "Database error");
         }

         var7.add("per_kit", var10);
         return var7;
      }
   }

   public JsonObject handleServerInfo() {
      JsonObject var1 = new JsonObject();
      var1.addProperty("online_players", Bukkit.getOnlinePlayers().size());
      var1.addProperty("max_players", Bukkit.getMaxPlayers());
      JsonArray var2 = new JsonArray();
      if (this.plugin.getKitManager() != null) {
         for (String var4 : this.plugin
            .getKitManager()
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()) {
            var2.add(var4);
         }
      }

      var1.add("kits", var2);
      return var1;
   }

   private JsonObject loadPlayerStats(UUID var1) {
      String var2 = "SELECT * FROM player_stats WHERE player_id = ?";

      try {
         JsonObject var13;
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

            JsonObject var6 = new JsonObject();
            int var7 = var5.getInt("kills");
            int var8 = var5.getInt("deaths");
            var6.addProperty("kills", var7);
            var6.addProperty("deaths", var8);
            var6.addProperty("kd_ratio", var8 == 0 ? (double)var7 : (double)Math.round((double)var7 / (double)var8 * 100.0) / 100.0);
            var6.addProperty("duel_wins", var5.getInt("duel_wins"));
            var6.addProperty("duel_losses", var5.getInt("duel_losses"));
            int var9 = var5.getInt("duel_wins");
            int var10 = var5.getInt("duel_losses");
            var6.addProperty("win_rate", var9 + var10 == 0 ? 0.0 : (double)Math.round((double)var9 / (double)(var9 + var10) * 10000.0) / 100.0);
            var6.addProperty("win_streak", var5.getInt("win_streak"));
            var6.addProperty("kill_streak", var5.getInt("kill_streak"));
            var6.addProperty("ffa_kills", var5.getInt("ffa_kills"));
            var6.addProperty("ffa_deaths", var5.getInt("ffa_deaths"));
            int var11 = var5.getInt("ffa_kills");
            int var12 = var5.getInt("ffa_deaths");
            var6.addProperty("ffa_kd_ratio", var12 == 0 ? (double)var11 : (double)Math.round((double)var11 / (double)var12 * 100.0) / 100.0);
            var13 = var6;
         }

         return var13;
      } catch (SQLException var18) {
         this.plugin.getLogger().warning("[API] Failed to load player stats: " + var18.getMessage());
         return null;
      }
   }

   private JsonObject loadRankedData(UUID var1) {
      String var2 = "SELECT * FROM player_ranked WHERE player_id = ?";

      try {
         label93: {
            JsonObject var8;
            try (
               Connection var3 = this.databaseManager
                  .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String();
               PreparedStatement var4 = var3.prepareStatement(var2);
            ) {
               var4.setString(1, var1.toString());
               ResultSet var5 = var4.executeQuery();
               if (!var5.next()) {
                  break label93;
               }

               JsonObject var6 = new JsonObject();
               int var7 = this.getOverallElo(var1);
               var6.addProperty("elo", var7);
               var6.addProperty("tier", this.getTierNameByPoints(var7));
               var6.addProperty("wins", var5.getInt("ranked_wins"));
               var6.addProperty("losses", var5.getInt("ranked_losses"));
               var6.addProperty("matches_played", var5.getInt("matches_played"));
               var6.addProperty("peak_elo", Math.max(var5.getInt("peak_points"), var7));
               var8 = var6;
            }

            return var8;
         }
      } catch (SQLException var13) {
         this.plugin.getLogger().warning("[API] Failed to load ranked data: " + var13.getMessage());
      }

      if (this.plugin.getKitsRankManager() != null
         && this.plugin
            .getKitsRankManager()
            .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class()
         )
       {
         JsonObject var14 = new JsonObject();
         int var15 = this.getOverallElo(var1);
         var14.addProperty("elo", var15);
         var14.addProperty("tier", this.getTierNameByPoints(var15));
         var14.addProperty("wins", 0);
         var14.addProperty("losses", 0);
         var14.addProperty("matches_played", 0);
         var14.addProperty("peak_elo", var15);
         return var14;
      } else {
         return null;
      }
   }

   private JsonObject loadKitStats(UUID var1) {
      String var2 = "SELECT kit_stats FROM player_stats WHERE player_id = ?";

      try {
         Object var7;
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

            String var6 = var5.getString("kit_stats");
            if (var6 != null && !var6.isEmpty()) {
               Map var22 = (Map)this.gson.fromJson(var6, (new TypeToken<Map<String, Map<String, Integer>>>() {
               }).getType());
               if (var22 == null) {
                  return null;
               }

               Map var8 = var22.getOrDefault("wins", Map.of());
               Map var9 = var22.getOrDefault("losses", Map.of());
               Map var10 = var22.getOrDefault("streaks", Map.of());
               HashSet var11 = new HashSet();
               var11.addAll(var8.keySet());
               var11.addAll(var9.keySet());
               if (this.plugin.getKitsRankManager() != null) {
                  Map var12 = this.plugin
                     .getKitsRankManager()
                     .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                        var1
                     );
                  if (var12 != null) {
                     var11.addAll(var12.keySet());
                  }
               }

               if (var11.isEmpty()) {
                  return null;
               }

               JsonObject var24 = new JsonObject();

               for (String var14 : var11) {
                  JsonObject var15 = new JsonObject();
                  var15.addProperty("wins", var8.getOrDefault(var14, 0));
                  var15.addProperty("losses", var9.getOrDefault(var14, 0));
                  var15.addProperty("win_streak", var10.getOrDefault(var14, 0));
                  int var16 = 0;
                  if (this.plugin.getKitsRankManager() != null) {
                     var16 = this.plugin
                        .getKitsRankManager()
                        .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                           var1, var14
                        );
                  }

                  var15.addProperty("elo", var16);
                  var15.addProperty("tier", this.getKitTierName(var14, var16));
                  var24.add(var14, var15);
               }

               return var24;
            }

            var7 = null;
         }

         return (JsonObject)var7;
      } catch (SQLException var21) {
         this.plugin.getLogger().warning("[API] Failed to load kit stats: " + var21.getMessage());
         return null;
      }
   }

   private UUID resolvePlayerUUID(String var1) {
      try {
         return UUID.fromString(var1);
      } catch (IllegalArgumentException var16) {
         OfflinePlayer var2 = Bukkit.getOfflinePlayerIfCached(var1);
         if (var2 != null) {
            return var2.getUniqueId();
         } else {
            UUID var3 = this.databaseManager
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1
               );
            if (var3 != null) {
               return var3;
            } else {
               try (Connection var4 = this.databaseManager
                     .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()) {
                  String var5 = "SELECT player_id FROM player_stats";

                  try (PreparedStatement var6 = var4.prepareStatement(var5)) {
                     ResultSet var7 = var6.executeQuery();

                     while (var7.next()) {
                        UUID var8 = this.parseUUID(var7.getString("player_id"));
                        if (var8 != null) {
                           OfflinePlayer var9 = Bukkit.getOfflinePlayer(var8);
                           if (var9.getName() != null && var9.getName().equalsIgnoreCase(var1)) {
                              return var8;
                           }
                        }
                     }

                     return null;
                  }
               } catch (SQLException var15) {
                  this.plugin.getLogger().warning("[API] Failed to resolve player name: " + var15.getMessage());
                  return null;
               }
            }
         }
      }
   }

   private String resolvePlayerName(UUID var1) {
      if (var1 == null) {
         return "Unknown";
      } else {
         OfflinePlayer var2 = Bukkit.getOfflinePlayer(var1);
         if (var2.getName() != null) {
            return var2.getName();
         } else {
            String var3 = this.databaseManager
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1
               );
            return var3 != null && !var3.isBlank() ? var3 : "Unknown";
         }
      }
   }

   private UUID parseUUID(String var1) {
      if (var1 == null) {
         return null;
      } else {
         try {
            return UUID.fromString(var1);
         } catch (IllegalArgumentException var3) {
            return null;
         }
      }
   }

   private int getOverallElo(UUID var1) {
      return this.plugin.getRankedManager() == null ? 0 : this.plugin.getRankedManager().getStoredGlobalElo(var1);
   }

   private String getTierNameByPoints(int var1) {
      if (this.plugin.getRankedManager() != null) {
         String var2 = this.plugin.getRankedManager().getTierNameByPoints(var1);
         if (var2 != null && !var2.isEmpty()) {
            return this.stripColorCodes(var2);
         }
      }

      return this.resolveTierFromConfig("tier-ranks.tiers", var1);
   }

   private String getKitTierName(String var1, int var2) {
      String var3 = var1.toLowerCase();
      if (var3.startsWith("tier")) {
         var3 = var3.substring(4);
      }

      String var4 = this.resolveTierFromConfig("per-kits-tier." + var3, var2);
      if (!"Unranked".equals(var4)) {
         return var4;
      } else {
         if (!var3.equals(var1.toLowerCase())) {
            var4 = this.resolveTierFromConfig("per-kits-tier." + var1.toLowerCase(), var2);
            if (!"Unranked".equals(var4)) {
               return var4;
            }
         }

         return "Unranked";
      }
   }

   private String resolveTierFromConfig(String var1, int var2) {
      ConfigurationSection var3 = this.plugin.getConfig().getConfigurationSection(var1);
      if (var3 == null) {
         return "Unranked";
      } else {
         for (String var5 : var3.getKeys(false)) {
            if (!var5.equalsIgnoreCase("starting-tier")) {
               ConfigurationSection var6 = var3.getConfigurationSection(var5);
               if (var6 != null) {
                  String var7 = var6.getString("tier-range", "");
                  if (var7.contains("-")) {
                     String[] var8 = var7.split("-");

                     try {
                        int var9 = Integer.parseInt(var8[0].trim());
                        int var10 = Integer.parseInt(var8[1].trim());
                        if (var2 >= var9 && var2 <= var10) {
                           return this.stripColorCodes(var6.getString("name", "Unranked"));
                        }
                     } catch (NumberFormatException var11) {
                     }
                  }
               }
            }
         }

         return "Unranked";
      }
   }

   private String stripColorCodes(String var1) {
      return var1 == null ? "Unranked" : var1.replaceAll("[§&][0-9a-fk-orA-FK-OR]", "").trim();
   }

   private int bindParams(PreparedStatement var1, List<Object> var2) throws SQLException {
      int var3 = 1;

      for (Object var5 : var2) {
         if (var5 instanceof Boolean var6) {
            var1.setBoolean(var3++, var6);
         } else if (var5 instanceof Long var7) {
            var1.setLong(var3++, var7);
         } else if (var5 instanceof Integer var8) {
            var1.setInt(var3++, var8);
         } else {
            var1.setString(var3++, String.valueOf(var5));
         }
      }

      return var3;
   }

   private Boolean parseBooleanFilter(String var1) {
      if (var1 == null || var1.isBlank()) {
         return null;
      } else if (var1.equalsIgnoreCase("true") || var1.equalsIgnoreCase("yes") || var1.equals("1")) {
         return true;
      } else {
         return !var1.equalsIgnoreCase("false") && !var1.equalsIgnoreCase("no") && !var1.equals("0") ? null : false;
      }
   }

   public static JsonObject errorJson(int var0, String var1) {
      JsonObject var2 = new JsonObject();
      var2.addProperty("error", true);
      var2.addProperty("code", var0);
      var2.addProperty("message", var1);
      return var2;
   }

   private static record GlobalLeaderboardEntry(UUID playerId, int points, ApiHandler.RankedSummary summary) {
   }

   private static record RankedSummary(int wins, int losses, int matchesPlayed, int peakElo) {
   }
}
