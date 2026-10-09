package org.lime.swiftCore.scoreboard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.lime.swiftCore.SwiftCore;

public class ScoreboardManager {
   private static final Pattern INTERNAL_PLACEHOLDER_PATTERN = Pattern.compile("%([a-zA-Z_][a-zA-Z0-9_]*)%");
   private final SwiftCore plugin;
   private final Map<UUID, PlayerScoreboardData> playerData;
   private final ScoreboardProvider provider;
   private final FileConfiguration scoreboardConfig;
   private boolean enabled;
   private boolean hideNumbers;
   private boolean globalEnabled;
   private Set<String> globalEnabledWorlds;
   private String globalTitle;
   private List<String> globalLines;
   private final ScheduledExecutorService scoreboardExecutor;
   private long lastPapiCleanup = 0L;
   private static final long PAPI_CLEANUP_INTERVAL_MS = 5000L;
   private static final int LINE_UPDATE_INTERVAL = 10;
   private int refreshTickCounter = 0;

   public ScoreboardManager(SwiftCore var1) {
      this.plugin = var1;
      this.playerData = new ConcurrentHashMap<>();
      this.scoreboardConfig = this.loadScoreboardConfig();
      this.provider = new ScoreboardProvider(var1, this.scoreboardConfig);
      this.enabled = this.scoreboardConfig.getBoolean("scoreboard.enabled", true);
      this.hideNumbers = this.scoreboardConfig.getBoolean("scoreboard.hide-numbers", false);
      this.scoreboardExecutor = Executors.newSingleThreadScheduledExecutor(var0 -> {
         Thread var1x = new Thread(var0, "SwiftCore-Scoreboard");
         var1x.setDaemon(true);
         return var1x;
      });
      this.loadGlobalConfig();
   }

   private void loadGlobalConfig() {
      this.globalEnabled = this.scoreboardConfig.getBoolean("global.enable", false);
      this.globalEnabledWorlds = new HashSet<>();

      for (String var2 : this.scoreboardConfig.getStringList("global.enable-worlds")) {
         this.globalEnabledWorlds.add(var2.toLowerCase());
      }

      this.globalTitle = this.scoreboardConfig.getString("global.title", "&e&lGLOBAL");
      this.globalLines = this.scoreboardConfig.getStringList("global.scoreboard");
   }

   public boolean isGlobalEnabled() {
      return this.globalEnabled;
   }

   public boolean isWorldEnabledForGlobal(String var1) {
      return this.globalEnabled && this.globalEnabledWorlds.contains(var1.toLowerCase());
   }

   public String getGlobalTitle() {
      return this.globalTitle;
   }

   public List<String> getGlobalLines() {
      return this.globalLines;
   }

   private FileConfiguration loadScoreboardConfig() {
      File var1 = new File(this.plugin.getDataFolder(), "scoreboard.yml");
      if (!var1.exists()) {
         try {
            Files.createDirectories(var1.getParentFile().toPath());

            try (InputStream var2 = this.plugin.getResource("scoreboard.yml")) {
               if (var2 != null) {
                  Files.copy(var2, var1.toPath());
               }
            }
         } catch (IOException var11) {
            this.plugin.getLogger().severe("Failed to save scoreboard.yml: " + var11.getMessage());
         }
      }

      YamlConfiguration var12 = YamlConfiguration.loadConfiguration(var1);
      if (var1.length() > 0L && var12.getKeys(false).isEmpty()) {
         this.plugin.getLogger().severe("=======================================================");
         this.plugin.getLogger().severe("scoreboard.yml has a YAML syntax error and could not be parsed!");
         this.plugin.getLogger().severe("Your scoreboard config will NOT be overwritten.");
         this.plugin.getLogger().severe("Fix the syntax error in scoreboard.yml and reload again.");
         this.plugin.getLogger().severe("=======================================================");

         try (InputStream var3 = this.plugin.getResource("scoreboard.yml")) {
            return var3 != null ? YamlConfiguration.loadConfiguration(new InputStreamReader(var3)) : var12;
         } catch (IOException var9) {
            return var12;
         }
      } else {
         this.mergeAndApplyDefaults(var12, var1);
         return var12;
      }
   }

   private void mergeAndApplyDefaults(FileConfiguration var1, File var2) {
      try {
         try (InputStream var3 = this.plugin.getResource("scoreboard.yml")) {
            if (var3 != null) {
               YamlConfiguration var4 = YamlConfiguration.loadConfiguration(new InputStreamReader(var3));
               var1.setDefaults(var4);
               boolean var5 = false;

               for (String var7 : var4.getKeys(true)) {
                  if (!var1.isSet(var7) && !var4.isList(var7)) {
                     var1.set(var7, var4.get(var7));
                     var5 = true;
                  }
               }

               if (var5) {
                  try {
                     var1.save(var2);
                     this.plugin.getLogger().info("Updated scoreboard.yml with new default sections.");
                  } catch (IOException var9) {
                     this.plugin.getLogger().warning("Could not save merged scoreboard.yml: " + var9.getMessage());
                  }

                  return;
               }

               return;
            }
         }
      } catch (IOException var11) {
         this.plugin.getLogger().warning("Could not load default scoreboard.yml from JAR: " + var11.getMessage());
      }
   }

   public void createScoreboard(Player var1) {
      if (this.enabled) {
         if (this.plugin
            .getPlayerSettingsManager()
            .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
               var1
            )
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
            )
          {
            UUID var2 = var1.getUniqueId();
            if (this.playerData.containsKey(var2)) {
               this.removeScoreboard(var1);
            }

            PlayerScoreboardData var3 = new PlayerScoreboardData(var2);
            this.playerData.put(var2, var3);
            SwiftBoard var4 = new SwiftBoard(var1, this.hideNumbers);
            var3.setBoard(var4);
            String var5 = this.provider.getTitle(var3);
            Component var6 = this.provider.getTitleComponent();
            var3.setPreviousTitleHash(var5.hashCode());
            var4.updateTitle(var6, var5);
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
               if (var1.isOnline() && var3.getBoard() != null && !var3.getBoard().isDeleted()) {
                  this.updateLinesOnly(var1);
               }
            }, 1L);
         }
      }
   }

   public void updateScoreboard(Player var1) {
      this.updateScoreboard(var1, false);
   }

   public void updateScoreboard(Player var1, boolean var2) {
      if (this.enabled) {
         PlayerScoreboardData var3 = this.playerData.get(var1.getUniqueId());
         if (var3 != null) {
            SwiftBoard var4 = var3.getBoard();
            if (var4 != null && !var4.isDeleted()) {
               UUID var5 = var1.getUniqueId();
               int var6 = var3.getPreviousTitleHash();
               int var7 = var3.getPreviousLinesHash();
               Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                  Player var5x = Bukkit.getPlayer(var5);
                  if (var5x != null && var5x.isOnline()) {
                     PlayerScoreboardData var6x = this.playerData.get(var5);
                     if (var6x != null) {
                        SwiftBoard var7x = var6x.getBoard();
                        if (var7x != null && !var7x.isDeleted()) {
                           List var8 = this.provider.getLines(var5x, var6x);
                           String var9 = this.provider.getTitle(var6x);
                           int var10 = var9.hashCode();
                           int var11 = computeLinesHash(var8);
                           boolean var12 = var10 != var6;
                           boolean var13 = var11 != var7;
                           if (!var2 && !var12 && !var13) {
                              var6x.clearDirty();
                           } else {
                              if (var2 || var12) {
                                 Component var14 = this.provider.getTitleComponent(var6x);
                                 var6x.setPreviousTitleHash(var10);
                                 var7x.updateTitle(var14, var9);
                              }

                              if (var2 || var13) {
                                 List var15 = this.provider.getComponentLines(var5x, var6x, var8);
                                 var6x.setPreviousLinesHash(var11);
                                 var7x.updateLines(var15, var8, var2);
                              }

                              var6x.clearDirty();
                              var6x.setLastUpdate(System.currentTimeMillis());
                           }
                        }
                     }
                  }
               });
            }
         }
      }
   }

   public void updateTitleOnly(Player var1) {
      PlayerScoreboardData var2 = this.playerData.get(var1.getUniqueId());
      if (var2 != null) {
         SwiftBoard var3 = var2.getBoard();
         if (var3 != null && !var3.isDeleted()) {
            String var4 = this.provider.getTitle(var2);
            int var5 = var4.hashCode();
            if (var5 != var2.getPreviousTitleHash()) {
               Component var6 = this.provider.getTitleComponent(var2);
               var2.setPreviousTitleHash(var5);
               var3.updateTitle(var6, var4);
            }
         }
      }
   }

   public void updateLinesOnly(Player var1) {
      PlayerScoreboardData var2 = this.playerData.get(var1.getUniqueId());
      if (var2 != null) {
         SwiftBoard var3 = var2.getBoard();
         if (var3 != null && !var3.isDeleted()) {
            List var4 = this.provider.getLines(var1, var2);
            int var5 = computeLinesHash(var4);
            if (var5 != var2.getPreviousLinesHash()) {
               List var6 = this.provider.getComponentLines(var1, var2, var4);
               var2.setPreviousLinesHash(var5);
               var3.updateLines(var6, var4);
               var2.clearDirty();
            }
         }
      }
   }

   public void removeScoreboard(Player var1) {
      UUID var2 = var1.getUniqueId();
      PlayerScoreboardData var3 = this.playerData.remove(var2);
      if (var3 != null && var3.getBoard() != null && !var3.getBoard().isDeleted()) {
         var3.getBoard().delete();
      }
   }

   public void setState(Player var1, ScoreboardState var2) {
      if (!this.plugin
         .getPlayerSettingsManager()
         .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
            var1
         )
         .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
         )
       {
         if (this.plugin.isDebug()) {
            this.plugin.getLogger().info("[Scoreboard Debug] setState SKIP (scoreboard disabled) player=" + var1.getName() + " state=" + var2);
         }
      } else {
         UUID var3 = var1.getUniqueId();
         PlayerScoreboardData var4 = this.playerData.get(var3);
         if (var4 == null) {
            if (this.plugin.isDebug()) {
               this.plugin.getLogger().info("[Scoreboard Debug] setState SKIP (no data) player=" + var1.getName() + " state=" + var2);
            }
         } else if (var4.getState() == var2) {
            if (this.plugin.isDebug()) {
               this.plugin.getLogger().info("[Scoreboard Debug] setState SKIP (same state) player=" + var1.getName() + " state=" + var2);
            }
         } else {
            ScoreboardState var5 = var4.getState();
            long var6 = (long)Bukkit.getCurrentTick();
            var4.setState(var2);
            var4.clearPapiCache();
            var4.resetHashes();
            var4.setLastUpdateTick(var6);
            if (this.plugin.isDebug()) {
               this.plugin.getLogger().info("[Scoreboard Debug] setState player=" + var1.getName() + " " + var5 + " -> " + var2);
            }

            this.updateScoreboardInstant(var1);
         }
      }
   }

   public ScoreboardState getState(Player var1) {
      if (var1 == null) {
         return null;
      } else {
         PlayerScoreboardData var2 = this.playerData.get(var1.getUniqueId());
         return var2 != null ? var2.getState() : null;
      }
   }

   public void updateScoreboardInstant(Player var1) {
      if (this.enabled) {
         UUID var2 = var1.getUniqueId();
         PlayerScoreboardData var3 = this.playerData.get(var2);
         if (var3 != null) {
            SwiftBoard var4 = var3.getBoard();
            if (var4 != null && !var4.isDeleted()) {
               int var5 = var3.getPreviousTitleHash();
               int var6 = var3.getPreviousLinesHash();
               Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
                  Player var4x = Bukkit.getPlayer(var2);
                  if (var4x != null && var4x.isOnline()) {
                     PlayerScoreboardData var5x = this.playerData.get(var2);
                     if (var5x != null) {
                        SwiftBoard var6x = var5x.getBoard();
                        if (var6x != null && !var6x.isDeleted()) {
                           List var7 = this.provider.getLines(var4x, var5x);
                           String var8 = this.provider.getTitle(var5x);
                           int var9 = var8.hashCode();
                           int var10 = computeLinesHash(var7);
                           boolean var11 = var9 != var5;
                           boolean var12 = var10 != var6;
                           if (var11 || var12) {
                              if (var11) {
                                 Component var13 = this.provider.getTitleComponent(var5x);
                                 var5x.setPreviousTitleHash(var9);
                                 var6x.updateTitle(var13, var8);
                              }

                              if (var12) {
                                 List var14 = this.provider.getComponentLines(var4x, var5x, var7);
                                 var5x.setPreviousLinesHash(var10);
                                 var6x.updateLines(var14, var7);
                              }

                              var5x.setLastUpdate(System.currentTimeMillis());
                           }
                        }
                     }
                  }
               });
            }
         }
      }
   }

   private void scheduleAsyncUpdate(Player var1) {
      if (this.enabled) {
         UUID var2 = var1.getUniqueId();
         Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            Player var2x = Bukkit.getPlayer(var2);
            if (var2x != null && var2x.isOnline()) {
               PlayerScoreboardData var3 = this.playerData.get(var2);
               if (var3 != null) {
                  if (var3.isDirty()) {
                     var3.clearDirty();
                     SwiftBoard var4 = var3.getBoard();
                     if (var4 != null && !var4.isDeleted()) {
                        List var5 = this.provider.getLines(var2x, var3);
                        String var6 = this.provider.getTitle(var3);
                        int var7 = var6.hashCode();
                        int var8 = computeLinesHash(var5);
                        boolean var9 = var7 != var3.getPreviousTitleHash();
                        boolean var10 = var8 != var3.getPreviousLinesHash();
                        if (var9 || var10) {
                           if (var9) {
                              Component var11 = this.provider.getTitleComponent(var3);
                              var3.setPreviousTitleHash(var7);
                              var4.updateTitle(var11, var6);
                           }

                           if (var10) {
                              List var12 = this.provider.getComponentLines(var2x, var3, var5);
                              var3.setPreviousLinesHash(var8);
                              var4.updateLines(var12, var5);
                           }

                           var3.setLastUpdate(System.currentTimeMillis());
                        }
                     }
                  }
               }
            }
         });
      }
   }

   public void setPlaceholder(Player var1, String var2, String var3) {
      if (this.plugin
         .getPlayerSettingsManager()
         .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
            var1
         )
         .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
         )
       {
         PlayerScoreboardData var4 = this.playerData.get(var1.getUniqueId());
         if (var4 != null) {
            boolean var5 = var4.setPlaceholder(var2, var3);
            if (var5) {
               var4.clearPapiCache();
               this.scheduleAsyncUpdate(var1);
            }
         }

         if (this.plugin.getTablistManager() != null) {
            this.plugin.getTablistManager().setPlaceholder(var1, var2, var3);
         }
      }
   }

   public void setPlaceholders(Player var1, Map<String, String> var2) {
      if (this.plugin
         .getPlayerSettingsManager()
         .oO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000do(
            var1
         )
         .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String()
         )
       {
         PlayerScoreboardData var3 = this.playerData.get(var1.getUniqueId());
         if (var3 != null) {
            boolean var4 = false;

            for (Entry var6 : var2.entrySet()) {
               if (var3.setPlaceholder((String)var6.getKey(), (String)var6.getValue())) {
                  var4 = true;
               }
            }

            if (var4) {
               var3.clearPapiCache();
               this.scheduleAsyncUpdate(var1);
            }
         }

         if (this.plugin.getTablistManager() != null) {
            for (Entry var8 : var2.entrySet()) {
               this.plugin.getTablistManager().setPlaceholder(var1, (String)var8.getKey(), (String)var8.getValue());
            }
         }
      }
   }

   private boolean isImportantPlaceholder(String var1) {
      return var1.equals("opponent")
         || var1.equals("kit")
         || var1.equals("in_fight")
         || var1.equals("in_queue")
         || var1.equals("in_ffa")
         || var1.equals("in_party")
         || var1.equals("spectating")
         || var1.equals("round")
         || var1.equals("own_wins")
         || var1.equals("opponent_wins")
         || var1.equals("team_icon")
         || var1.equals("team_color")
         || var1.equals("fight_kitname")
         || var1.equals("in_queue_kitname")
         || var1.contains("countdown")
         || var1.contains("timer")
         || var1.contains("time")
         || var1.contains("duration")
         || var1.contains("seconds")
         || var1.contains("wait");
   }

   public PlayerScoreboardData getData(Player var1) {
      return this.playerData.get(var1.getUniqueId());
   }

   public String getPlaceholder(Player var1, String var2) {
      PlayerScoreboardData var3 = this.playerData.get(var1.getUniqueId());
      return var3 == null ? "" : var3.getPlaceholder(var2);
   }

   public String resolveInternalPlaceholders(Player var1, String var2) {
      PlayerScoreboardData var3 = this.playerData.get(var1.getUniqueId());
      if (var3 != null && var2 != null) {
         Matcher var4 = INTERNAL_PLACEHOLDER_PATTERN.matcher(var2);
         StringBuilder var5 = new StringBuilder();

         while (var4.find()) {
            String var6 = var4.group(1);
            String var7 = var3.getPlaceholder(var6);
            if (var7 != null && !var7.isEmpty()) {
               var4.appendReplacement(var5, Matcher.quoteReplacement(var7));
            }
         }

         var4.appendTail(var5);
         return var5.toString();
      } else {
         return var2;
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public ScoreboardProvider getProvider() {
      return this.provider;
   }

   public FileConfiguration getScoreboardConfig() {
      return this.scoreboardConfig;
   }

   public boolean hasSectionForState(ScoreboardState var1) {
      String var2 = "scoreboard." + var1.name().toLowerCase();
      return this.scoreboardConfig.isList(var2) && !this.scoreboardConfig.getStringList(var2).isEmpty();
   }

   public int getUpdateTime() {
      return this.scoreboardConfig.getInt("scoreboard.update-interval", 20);
   }

   public boolean shouldTeleportUpdate() {
      return this.scoreboardConfig.getBoolean("scoreboard.teleport-update", true);
   }

   public void startRefreshTask() {
      ParsedLine.updatePapiStatus();
      int var1 = this.provider.getMinimumAnimationIntervalMs();
      this.scoreboardExecutor.scheduleAtFixedRate(() -> {
         try {
            ParsedLine.updatePapiStatus();
            long var1x = System.currentTimeMillis();
            boolean var3 = var1x - this.lastPapiCleanup >= 5000L;
            if (var3) {
               this.lastPapiCleanup = var1x;
            }

            boolean var4 = this.refreshTickCounter % 10 == 0;
            this.refreshTickCounter++;

            for (UUID var6 : this.playerData.keySet()) {
               Player var7 = Bukkit.getPlayer(var6);
               if (var7 != null && var7.isOnline()) {
                  PlayerScoreboardData var8 = this.playerData.get(var6);
                  if (var8 != null) {
                     SwiftBoard var9 = var8.getBoard();
                     if (var9 != null && !var9.isDeleted()) {
                        if (var3) {
                           var8.clearExpiredPapi();
                        }

                        String var10 = this.provider.getTitle(var8);
                        int var11 = var10.hashCode();
                        boolean var12 = var11 != var8.getPreviousTitleHash();
                        if (var12) {
                           Component var13 = this.provider.getTitleComponent(var8);
                           var8.setPreviousTitleHash(var11);
                           var9.updateTitle(var13, var10);
                        }

                        if (var4 || var8.isDirty()) {
                           List var18 = this.provider.getLines(var7, var8);
                           int var14 = computeLinesHash(var18);
                           boolean var15 = var14 != var8.getPreviousLinesHash();
                           if (var15) {
                              List var16 = this.provider.getComponentLines(var7, var8, var18);
                              var8.setPreviousLinesHash(var14);
                              var9.updateLines(var16, var18);
                           }

                           var8.clearDirty();
                        }

                        var8.setLastUpdate(var1x);
                     }
                  }
               }
            }
         } catch (Exception var17) {
            this.plugin.getLogger().warning("Error in scoreboard refresh: " + var17.getMessage());
         }
      }, (long)var1, (long)var1, TimeUnit.MILLISECONDS);
   }

   private static int computeLinesHash(List<String> var0) {
      int var1 = 1;

      for (String var3 : var0) {
         var1 = 31 * var1 + var3.hashCode();
      }

      return var1;
   }

   public void shutdown() {
      this.scoreboardExecutor.shutdown();
   }

   public ScheduledExecutorService getExecutor() {
      return this.scoreboardExecutor;
   }
}
