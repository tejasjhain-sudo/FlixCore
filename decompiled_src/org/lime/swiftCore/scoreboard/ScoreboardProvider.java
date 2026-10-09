package org.lime.swiftCore.scoreboard;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.lime.swiftCore.SwiftCore;
import org.lime.swiftCore.arena.d;
import org.lime.swiftCore.b.e;
import org.lime.swiftCore.b.f;
import org.lime.swiftCore.duel.g;
import org.lime.swiftCore.h.c;
import org.lime.swiftCore.kit.n;
import org.lime.swiftCore.libs.caffeine.cache.Cache;
import org.lime.swiftCore.libs.caffeine.cache.Caffeine;

public class ScoreboardProvider {
   private static final String STEVE_HEAD_TAG = "<head:entity/player/wide/steve>";
   private static final Pattern DISPLAY_PATTERN = Pattern.compile("\\[display=<(!)?([^>]+)>]");
   private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("<([^>]+)>");
   private static final Pattern PERCENT_PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
   private static final Pattern ANIMATION_PATTERN = Pattern.compile("%animation:([^%]+)%");
   private static final Pattern PAPI_PATTERN = PERCENT_PLACEHOLDER_PATTERN;
   private final SwiftCore plugin;
   private final FileConfiguration config;
   private boolean papiEnabled;
   private AnimationManager animationManager;
   private static final MiniMessage MINI_MESSAGE = e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
   private final Map<ScoreboardState, List<String>> colorizedTemplateCache = new ConcurrentHashMap<>();
   private final Map<ScoreboardState, List<String>> rawTemplateCache = new ConcurrentHashMap<>();
   private final Map<ScoreboardState, List<String>> cleanTemplateCache = new ConcurrentHashMap<>();
   private final Map<ScoreboardState, List<ParsedLine>> parsedLinesCache = new ConcurrentHashMap<>();
   private final Map<ScoreboardState, List<String>> staticResolvedLinesCache = new ConcurrentHashMap<>();
   private final Cache<String, Component> componentCache = Caffeine.newBuilder().maximumSize(2000L).expireAfterAccess(Duration.ofMinutes(5L)).build();
   private String colorizedTitleCache = null;
   private ParsedLine parsedTitleCache = null;
   private static final Set<String> DYNAMIC_PAPI_PATTERNS = Set.of(
      "player_health", "player_food_level", "player_ping", "player_exp", "server_tps", "server_online", "server_ram", "swiftcore_"
   );
   private boolean shadowEnabled;
   private String shadowColor;
   private String cachedShadowPrefix;
   private String cachedShadowSuffix;
   private FileConfiguration jarDefaults;

   public ScoreboardProvider(SwiftCore var1, FileConfiguration var2) {
      this.plugin = var1;
      this.config = var2;
      this.animationManager = new AnimationManager(var1);
      this.loadJarDefaults();
      this.loadShadowConfig();
      this.checkPAPIStatus();
      this.buildTemplateCache();
   }

   private void loadJarDefaults() {
      try (InputStream var1 = this.plugin.getResource("scoreboard.yml")) {
         if (var1 != null) {
            this.jarDefaults = YamlConfiguration.loadConfiguration(new InputStreamReader(var1));
         }
      } catch (IOException var6) {
         this.plugin.getLogger().warning("Could not load default scoreboard.yml from JAR");
      }
   }

   private void loadShadowConfig() {
      this.shadowEnabled = this.config.getBoolean("scoreboard.shadow", false);
      this.shadowColor = this.config.getString("scoreboard.shadow-color", "#000000FF");
      if (this.shadowEnabled) {
         this.cachedShadowPrefix = "<shadow:" + this.shadowColor + ">";
         this.cachedShadowSuffix = "</shadow>";
      } else {
         this.cachedShadowPrefix = null;
         this.cachedShadowSuffix = null;
      }
   }

   private void buildTemplateCache() {
      this.colorizedTemplateCache.clear();
      this.rawTemplateCache.clear();
      this.cleanTemplateCache.clear();
      this.parsedLinesCache.clear();
      this.staticResolvedLinesCache.clear();
      this.colorizedTitleCache = null;
      this.parsedTitleCache = null;
      String var1 = this.config.getString("scoreboard.title", "§6§lPRACTICE");
      this.colorizedTitleCache = this.colorize(var1);
      this.parsedTitleCache = new ParsedLine(this.plugin, var1, this.colorizedTitleCache, null, false);

      for (ScoreboardState var5 : ScoreboardState.values()) {
         String var6 = var5.name().toLowerCase();
         List var7;
         if (var5 == ScoreboardState.GLOBAL) {
            var7 = this.getConfiguredStringList("global.scoreboard");
         } else {
            var7 = this.getConfiguredStringList("scoreboard." + var6);
         }

         String var8 = var7.isEmpty() ? "empty" : "config";
         if (var7.isEmpty() && var5 == ScoreboardState.DEFAULT && this.jarDefaults != null) {
            var7 = this.jarDefaults.getStringList("scoreboard." + var6);
            if (!var7.isEmpty()) {
               var8 = "jarDefaults";
            }
         }

         if (var7.isEmpty()) {
            ScoreboardState var9 = this.getFallbackState(var5);
            if (var9 != null) {
               var7 = this.getConfiguredStringList("scoreboard." + var9.name().toLowerCase());
               if (!var7.isEmpty()) {
                  var8 = "fallback(" + var9 + ")";
               }

               if (var7.isEmpty() && this.jarDefaults != null) {
                  var7 = this.jarDefaults.getStringList("scoreboard." + var9.name().toLowerCase());
                  if (!var7.isEmpty()) {
                     var8 = "jarFallback(" + var9 + ")";
                  }
               }
            }
         }

         if (var7.isEmpty()) {
            var7 = this.getConfiguredStringList("scoreboard.default");
            if (!var7.isEmpty()) {
               var8 = "default";
            }
         }

         if (var7.isEmpty() && this.jarDefaults != null) {
            var7 = this.jarDefaults.getStringList("scoreboard.default");
            if (!var7.isEmpty()) {
               var8 = "jarDefault";
            }
         }

         if (this.plugin.isDebug()) {
            this.plugin.getLogger().info("[Scoreboard Debug] Template " + var5 + " -> " + var8 + " (" + var7.size() + " lines)");
         }

         this.rawTemplateCache.put(var5, new ArrayList<>(var7));
         ArrayList var19 = new ArrayList(var7.size());
         ArrayList var10 = new ArrayList(var7.size());
         ArrayList var11 = new ArrayList(var7.size());

         for (String var13 : var7) {
            String var14 = this.colorize(var13);
            var19.add(var14);
            String var15 = null;
            boolean var16 = false;
            Matcher var17 = DISPLAY_PATTERN.matcher(var14);
            if (var17.find()) {
               var16 = var17.group(1) != null;
               var15 = var17.group(2);
            }

            String var18 = DISPLAY_PATTERN.matcher(var14).replaceAll("");
            var10.add(var18);
            var11.add(new ParsedLine(this.plugin, var13, var18, var15, var16));
         }

         this.colorizedTemplateCache.put(var5, var19);
         this.cleanTemplateCache.put(var5, var10);
         this.parsedLinesCache.put(var5, var11);
         if (var11.stream().allMatch(var0 -> var0.isFullyStatic() && !var0.hasDisplayCondition())) {
            List var20 = var11.stream().map(var0 -> var0.resolve(null, null)).filter(var0 -> !var0.trim().isEmpty()).toList();
            this.staticResolvedLinesCache.put(var5, var20);
         }
      }
   }

   private List<String> getConfiguredStringList(String var1) {
      return (List<String>)(!this.config.contains(var1, true) ? new ArrayList<>() : this.config.getStringList(var1));
   }

   private ScoreboardState getFallbackState(ScoreboardState var1) {
      return switch (var1) {
         case DUEL_BLOCK_DECAY, DUEL_FLOWER_CROWN, DUEL_TNT_TAG, DUEL_BEDWARS -> ScoreboardState.DUEL;
         case PARTY_BLOCK_DECAY, PARTY_FLOWER_CROWN, PARTY_TNT_TAG, PARTY_BEDWARS -> ScoreboardState.PARTY_FFA;
         case TOURNAMENT_WAITING -> ScoreboardState.TOURNAMENT;
         case EVENT_WAITING -> ScoreboardState.EVENT;
         case TOURNAMENT, EVENT -> ScoreboardState.DEFAULT;
         default -> null;
      };
   }

   public void invalidateCache() {
      this.colorizedTemplateCache.clear();
      this.rawTemplateCache.clear();
      this.cleanTemplateCache.clear();
      this.parsedLinesCache.clear();
      this.staticResolvedLinesCache.clear();
      this.componentCache.invalidateAll();
      this.colorizedTitleCache = null;
      this.parsedTitleCache = null;
      this.loadShadowConfig();
      this.buildTemplateCache();
   }

   private void checkPAPIStatus() {
      this.papiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
   }

   public List<String> getLines(Player var1, PlayerScoreboardData var2) {
      ScoreboardState var3 = var2.getState();
      List var4 = this.staticResolvedLinesCache.get(var3);
      if (var4 != null) {
         return this.sanitizeScoreboardHeads(var4, var2);
      } else {
         List var5 = this.parsedLinesCache.get(var3);
         if (var5 == null) {
            var4 = this.staticResolvedLinesCache.get(ScoreboardState.DEFAULT);
            if (var4 != null) {
               return this.sanitizeScoreboardHeads(var4, var2);
            }

            var5 = this.parsedLinesCache.get(ScoreboardState.DEFAULT);
         }

         if (var5 == null) {
            return new ArrayList<>();
         } else {
            ArrayList var6 = new ArrayList(var5.size());

            for (ParsedLine var8 : var5) {
               if (var8.shouldDisplay(var2)) {
                  String var9 = var8.resolve(var1, var2);
                  if (!var9.trim().isEmpty()) {
                     var6.add(this.sanitizeScoreboardHeads(var9, var2));
                  }
               }
            }

            return var6;
         }
      }
   }

   public List<String[]> getLinesWithAnimationInfo(Player var1, PlayerScoreboardData var2) {
      ScoreboardState var3 = var2.getState();
      List var4 = this.parsedLinesCache.get(var3);
      if (var4 == null) {
         var4 = this.parsedLinesCache.get(ScoreboardState.DEFAULT);
      }

      if (var4 == null) {
         return new ArrayList<>();
      } else {
         ArrayList var5 = new ArrayList(var4.size());

         for (ParsedLine var7 : var4) {
            if (var7.shouldDisplay(var2)) {
               String var8 = var7.resolve(var1, var2);
               if (!var8.trim().isEmpty()) {
                  var5.add(new String[]{this.sanitizeScoreboardHeads(var8, var2), var7.hasAnimation() ? "1" : "0"});
               }
            }
         }

         return var5;
      }
   }

   private List<String> getLinesInternal(Player var1, PlayerScoreboardData var2, boolean var3) {
      ScoreboardState var4 = var2.getState();
      List var5 = this.rawTemplateCache.get(var4);
      List var6 = this.cleanTemplateCache.get(var4);
      if (var5 == null || var6 == null) {
         var5 = this.rawTemplateCache.get(ScoreboardState.DEFAULT);
         var6 = this.cleanTemplateCache.get(ScoreboardState.DEFAULT);
      }

      if (var5 != null && var6 != null) {
         boolean var7 = false;

         for (String var9 : var5) {
            if (var9.indexOf(91) != -1 && DISPLAY_PATTERN.matcher(var9).find()) {
               var7 = true;
               break;
            }
         }

         ArrayList var12 = new ArrayList(var6.size());
         if (!var7) {
            for (String var10 : var6) {
               String var11 = this.replacePlaceholders(var10, var1, var2);
               if (!var11.trim().isEmpty()) {
                  var12.add(var11);
               }
            }
         } else {
            for (int var14 = 0; var14 < var5.size(); var14++) {
               if (this.shouldDisplay((String)var5.get(var14), var2)) {
                  String var15 = this.replacePlaceholders((String)var6.get(var14), var1, var2);
                  if (!var15.trim().isEmpty()) {
                     var12.add(var15);
                  }
               }
            }
         }

         return var12;
      } else {
         return new ArrayList<>();
      }
   }

   private boolean shouldDisplay(String var1, PlayerScoreboardData var2) {
      if (var1.indexOf(91) == -1) {
         return true;
      } else {
         Matcher var3 = DISPLAY_PATTERN.matcher(var1);

         while (var3.find()) {
            boolean var4 = var3.group(1) != null;
            String var5 = var3.group(2);
            String var6 = var2.getPlaceholder(var5);
            boolean var7 = var6 != null && !var6.isEmpty() && (var6.equalsIgnoreCase("true") || var6.equals("1"));
            if (var4) {
               var7 = !var7;
            }

            if (!var7) {
               return false;
            }
         }

         return true;
      }
   }

   private String replacePlaceholders(String var1, Player var2, PlayerScoreboardData var3) {
      if (var1.indexOf(37) != -1 && var1.contains("%animation:")) {
         var1 = this.replaceAnimationPlaceholders(var1);
      }

      if (var1.indexOf(60) != -1 || var1.indexOf(37) != -1) {
         var1 = this.replaceInternalPlaceholders(var1, var2, var3);
      }

      if (this.papiEnabled && var1.indexOf(37) != -1) {
         var1 = this.replacePapiPlaceholdersWithCache(var1, var2, var3);
      }

      return var1;
   }

   private boolean isDynamicPlaceholder(String var1) {
      String var2 = var1.toLowerCase();

      for (String var4 : DYNAMIC_PAPI_PATTERNS) {
         if (var2.contains(var4)) {
            return true;
         }
      }

      return false;
   }

   private String replacePapiPlaceholdersWithCache(String var1, Player var2, PlayerScoreboardData var3) {
      Matcher var4 = PAPI_PATTERN.matcher(var1);
      StringBuffer var5 = new StringBuffer();

      while (var4.find()) {
         String var6 = var4.group(1);
         if (!var6.startsWith("animation:")) {
            String var7 = "%" + var6 + "%";

            String var8;
            try {
               if (this.isDynamicPlaceholder(var6)) {
                  var8 = PlaceholderAPI.setPlaceholders(var2, var7);
               } else {
                  String var9 = var3.getCachedPapi(var6);
                  if (var9 != null) {
                     var8 = var9;
                  } else {
                     var8 = PlaceholderAPI.setPlaceholders(var2, var7);
                     var3.setCachedPapi(var6, var8);
                  }
               }
            } catch (ConcurrentModificationException var10) {
               var8 = var7;
            }

            var4.appendReplacement(var5, Matcher.quoteReplacement(var8));
         }
      }

      var4.appendTail(var5);
      return var5.toString();
   }

   private String replaceAnimationPlaceholders(String var1) {
      Matcher var2 = ANIMATION_PATTERN.matcher(var1);
      StringBuffer var3 = new StringBuffer();

      while (var2.find()) {
         String var4 = var2.group(1);
         String var5 = this.animationManager.getAnimationFrame(var4);
         var2.appendReplacement(var3, Matcher.quoteReplacement(var5));
      }

      var2.appendTail(var3);
      return var3.toString();
   }

   private String replaceInternalPlaceholders(String var1, Player var2, PlayerScoreboardData var3) {
      if (var1.indexOf(60) != -1) {
         Matcher var4 = PLACEHOLDER_PATTERN.matcher(var1);
         StringBuffer var5 = new StringBuffer();

         while (var4.find()) {
            String var6 = var4.group(1);
            if (!ParsedLine.isInternalPlaceholderKey(var6)) {
               var4.appendReplacement(var5, Matcher.quoteReplacement("<" + var6 + ">"));
            } else {
               String var7 = this.getInternalPlaceholder(var6, var2, var3);
               var4.appendReplacement(var5, Matcher.quoteReplacement(var7));
            }
         }

         var4.appendTail(var5);
         var1 = var5.toString();
      }

      if (var1.indexOf(37) != -1) {
         Matcher var8 = PERCENT_PLACEHOLDER_PATTERN.matcher(var1);
         StringBuffer var9 = new StringBuffer();

         while (var8.find()) {
            String var10 = var8.group(1);
            if (!var10.startsWith("luckperms_")
               && !var10.startsWith("swiftcore_")
               && !var10.startsWith("player_")
               && !var10.startsWith("server_")
               && !var10.startsWith("vault_")
               && !var10.contains("_")) {
               String var11 = this.getBasicInternalPlaceholder(var10, var2, var3);
               if (var11 != null) {
                  var8.appendReplacement(var9, Matcher.quoteReplacement(var11));
               } else {
                  var8.appendReplacement(var9, Matcher.quoteReplacement(var8.group()));
               }
            } else {
               var8.appendReplacement(var9, Matcher.quoteReplacement(var8.group()));
            }
         }

         var8.appendTail(var9);
         var1 = var9.toString();
      }

      return var1;
   }

   private String getInternalPlaceholder(String var1, Player var2, PlayerScoreboardData var3) {
      if (var1.startsWith("ffareset_")) {
         String var11 = var1.substring(9);
         d var12 = this.plugin
            .getArenaManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var11
            );
         if (var12 != null
            && var12.øo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000interfacesuper()
            )
          {
            int var14 = var12.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String();
            if (var14 > 0) {
               int var18 = var14 / 60;
               int var20 = var14 % 60;
               return var18 + ":" + (var20 < 10 ? "0" : "") + var20;
            }
         }

         return "N/A";
      } else if (var1.equals("match_duration")) {
         g var10 = this.plugin.getDuelManager().getMatch(var2.getUniqueId());
         if (var10 != null) {
            return var10.Öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000forsuper();
         } else {
            org.lime.swiftCore.party.d var5 = this.plugin.getPartyGameManager().getPlayerActiveGame(var2.getUniqueId());
            if (var5 != null) {
               return var5.ÓÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectnew();
            } else if (this.plugin.getFFAManager().isInFFA(var2.getUniqueId())) {
               return "N/A";
            } else {
               if (this.plugin
                  .getSpectatorManager()
                  .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                     var2.getUniqueId()
                  )) {
                  c var13 = this.plugin
                     .getSpectatorManager()
                     .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                        var2.getUniqueId()
                     );
                  if (var13 != null) {
                     UUID var17 = var13.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String();
                     g var19 = this.plugin.getDuelManager().getMatch(var17);
                     if (var19 != null) {
                        return var19.Öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000forsuper();
                     }

                     org.lime.swiftCore.party.d var9 = this.plugin.getPartyGameManager().getPlayerActiveGame(var17);
                     if (var9 != null) {
                        return var9.ÓÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectnew();
                     }
                  }
               }

               return "0:00";
            }
         }
      } else {
         String var4 = switch (var1) {
            case "player" -> var2.getName();
            case "player_head", "own_head", "head" -> this.playerHeadTag(var2);
            case "opponent_head" -> this.opponentHeadTag(var2);
            case "ping" -> String.valueOf(var2.getPing());
            case "kit" -> this.colorizeSmall(this.getKitDisplayName(var3.getPlaceholder("kit")));
            case "opponent" -> var3.getPlaceholder("opponent");
            case "opponent_ping" -> var3.getPlaceholder("opponent_ping");
            case "round" -> var3.getPlaceholder("round");
            case "current_round" -> var3.getPlaceholder("current_round");
            case "total_rounds" -> var3.getPlaceholder("total_rounds");
            case "own_wins" -> var3.getPlaceholder("own_wins");
            case "your_wins" -> var3.getPlaceholder("your_wins");
            case "opponent_wins" -> var3.getPlaceholder("opponent_wins");
            case "hits" -> var3.getPlaceholder("hits");
            case "hits_opponent" -> var3.getPlaceholder("hits_opponent");
            case "hits_difference" -> var3.getPlaceholder("hits_difference");
            case "duel_kills", "match_kills" -> var3.getPlaceholder("duel_kills");
            case "duel_deaths", "match_deaths" -> var3.getPlaceholder("duel_deaths");
            case "opponent_kills" -> var3.getPlaceholder("opponent_kills");
            case "opponent_deaths" -> var3.getPlaceholder("opponent_deaths");
            case "duration" -> var3.getPlaceholder("duration");
            case "ended" -> var3.getPlaceholder("ended");
            case "is_bestof" -> var3.getPlaceholder("is_bestof");
            case "is_boxing" -> var3.getPlaceholder("is_boxing");
            case "is_ffa" -> var3.getPlaceholder("is_ffa");
            case "ffa_players" -> var3.getPlaceholder("ffa_players");
            case "ffa_arena", "arenaname" -> var3.getPlaceholder("ffa_arena");
            case "fight_kitname" -> this.colorizeSmall(this.getKitDisplayName(var3.getPlaceholder("fight_kitname")));
            case "in_queue_kitname" -> this.colorizeSmall(this.getKitDisplayName(var3.getPlaceholder("in_queue_kitname")));
            case "party_ffa_alive" -> var3.getPlaceholder("party_ffa_alive");
            case "party_size" -> var3.getPlaceholder("party_size");
            case "party_owner" -> var3.getPlaceholder("party_owner");
            case "party_max_size" -> var3.getPlaceholder("party_max_size");
            case "party_mode" -> var3.getPlaceholder("party_mode");
            case "blue_alive" -> var3.getPlaceholder("blue_alive");
            case "red_alive" -> var3.getPlaceholder("red_alive");
            case "blue_total" -> var3.getPlaceholder("blue_total");
            case "red_total" -> var3.getPlaceholder("red_total");
            case "your_party_alive" -> var3.getPlaceholder("your_party_alive");
            case "your_party_total" -> var3.getPlaceholder("your_party_total");
            case "enemy_party_alive" -> var3.getPlaceholder("enemy_party_alive");
            case "enemy_party_total" -> var3.getPlaceholder("enemy_party_total");
            case "team_icon" -> this.colorizeSmall(var3.getPlaceholder("team_icon"));
            case "team_color" -> this.colorizeSmall(var3.getPlaceholder("team_color"));
            case "spectating_player" -> var3.getPlaceholder("spectating_player");
            case "spectating_arena" -> var3.getPlaceholder("spectating_arena");
            case "spectating_kit" -> this.colorizeSmall(this.getKitDisplayName(var3.getPlaceholder("spectating_kit")));
            case "spectating_players" -> {
               String var16 = var3.getPlaceholder("spectating_arena");
               yield var16 != null && !var16.isEmpty()
                  ? String.valueOf(
                     this.plugin
                        .getSpectatorManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var2.getUniqueId(), var16
                        )
                  )
                  : var3.getPlaceholder("spectating_players");
            }
            case "spectating_time" -> {
               c var15 = this.plugin
                  .getSpectatorManager()
                  .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                     var2.getUniqueId()
                  );
               yield var15 != null
                  ? var15.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                  : "0:00";
            }
            case "editing_kit" -> this.colorizeSmall(this.getKitDisplayName(var3.getPlaceholder("editing_kit")));
            case "opponent_hp" -> {
               n var7 = this.plugin.getHPIndicatorManager();
               if (var7 != null && var7.isActive(var2.getUniqueId())) {
                  double var8 = var7.getOpponentHealth(var2.getUniqueId());
                  yield var8 >= 0.0 ? String.format("%.1f", var8) : "";
               } else {
                  yield "";
               }
            }
            default -> var3.getPlaceholder(var1);
         };
         return var4 != null ? var4 : "";
      }
   }

   private String getBasicInternalPlaceholder(String var1, Player var2, PlayerScoreboardData var3) {
      return switch (var1) {
         case "player" -> var2.getName();
         case "player_head", "own_head", "head" -> this.playerHeadTag(var2);
         case "opponent_head" -> this.opponentHeadTag(var2);
         case "match_duration" -> {
            g var12 = this.plugin.getDuelManager().getMatch(var2.getUniqueId());
            if (var12 != null) {
               yield var12.Öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000forsuper();
            } else {
               org.lime.swiftCore.party.d var7 = this.plugin.getPartyGameManager().getPlayerActiveGame(var2.getUniqueId());
               if (var7 != null) {
                  yield var7.ÓÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectnew();
               } else if (this.plugin.getFFAManager().isInFFA(var2.getUniqueId())) {
                  yield "N/A";
               } else {
                  if (this.plugin
                     .getSpectatorManager()
                     .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
                        var2.getUniqueId()
                     )) {
                     c var8 = this.plugin
                        .getSpectatorManager()
                        .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                           var2.getUniqueId()
                        );
                     if (var8 != null) {
                        UUID var9 = var8.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String();
                        g var10 = this.plugin.getDuelManager().getMatch(var9);
                        if (var10 != null) {
                           yield var10.Öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000forsuper();
                        }

                        org.lime.swiftCore.party.d var11 = this.plugin.getPartyGameManager().getPlayerActiveGame(var9);
                        if (var11 != null) {
                           yield var11.ÓÒ00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Objectnew();
                        }
                     }
                  }

                  yield "0:00";
               }
            }
         }
         default -> {
            String var6 = var3.getPlaceholder(var1);
            yield var6 != null && !var6.isEmpty() ? var6 : null;
         }
      };
   }

   private String colorize(String var1) {
      return var1;
   }

   private String playerHeadTag(Player var1) {
      return var1 != null && var1.isOnline() ? "<head:" + var1.getName() + ">" : "<head:entity/player/wide/steve>";
   }

   private String opponentHeadTag(Player var1) {
      Player var2 = this.getVisibleOpponent(var1);
      return var2 != null ? this.playerHeadTag(var2) : "<head:entity/player/wide/steve>";
   }

   private Player getVisibleOpponent(Player var1) {
      if (var1 != null
         && !this.plugin
            .getPlayerSettingsManager()
            .Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
               var1
            )) {
         g var2 = this.plugin.getDuelManager().getMatch(var1.getUniqueId());
         if (var2 == null) {
            return null;
         } else {
            UUID var3 = var2.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void(
               var1.getUniqueId()
            );
            return var3 != null ? Bukkit.getPlayer(var3) : null;
         }
      } else {
         return null;
      }
   }

   private String colorizeSmall(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         var1 = this.translateHexColorCodes(var1);
         return this.translateAmpersandCodes(var1);
      } else {
         return var1;
      }
   }

   private String translateHexColorCodes(String var1) {
      int var2 = var1.indexOf("&x");
      if (var2 != -1) {
         StringBuilder var3 = new StringBuilder();
         int var4 = 0;
         int var5 = var2;

         while (var5 < var1.length() - 13) {
            if (var1.charAt(var5) == '&' && var1.charAt(var5 + 1) == 'x') {
               boolean var6 = true;

               for (int var7 = 0; var7 < 6; var7++) {
                  int var8 = var5 + 2 + var7 * 2;
                  if (var8 + 1 >= var1.length() || var1.charAt(var8) != '&' || !this.isHexChar(var1.charAt(var8 + 1))) {
                     var6 = false;
                     break;
                  }
               }

               if (var6) {
                  var3.append(var1, var4, var5);
                  var3.append("§x");

                  for (int var13 = 0; var13 < 6; var13++) {
                     var3.append('§').append(var1.charAt(var5 + 3 + var13 * 2));
                  }

                  var4 = var5 + 14;
                  var5 = var4;
                  continue;
               }
            }

            var5++;
         }

         if (var4 > 0) {
            var3.append(var1, var4, var1.length());
            var1 = var3.toString();
         }
      }

      int var9 = var1.indexOf("&#");
      if (var9 != -1) {
         StringBuilder var10 = new StringBuilder();
         int var11 = 0;
         int var12 = var9;

         while (var12 < var1.length() - 7) {
            if (var1.charAt(var12) == '&' && var1.charAt(var12 + 1) == '#') {
               boolean var14 = true;

               for (int var15 = 0; var15 < 6; var15++) {
                  if (!this.isHexChar(var1.charAt(var12 + 2 + var15))) {
                     var14 = false;
                     break;
                  }
               }

               if (var14) {
                  var10.append(var1, var11, var12);
                  var10.append("§x");

                  for (int var16 = 0; var16 < 6; var16++) {
                     var10.append('§').append(var1.charAt(var12 + 2 + var16));
                  }

                  var11 = var12 + 8;
                  var12 = var11;
                  continue;
               }
            }

            var12++;
         }

         if (var11 > 0) {
            var10.append(var1, var11, var1.length());
            var1 = var10.toString();
         }
      }

      return var1;
   }

   private boolean isHexChar(char var1) {
      return var1 >= '0' && var1 <= '9' || var1 >= 'a' && var1 <= 'f' || var1 >= 'A' && var1 <= 'F';
   }

   private String translateAmpersandCodes(String var1) {
      int var2 = var1.indexOf(38);
      if (var2 == -1) {
         return var1;
      } else {
         char[] var3 = var1.toCharArray();
         StringBuilder var4 = new StringBuilder(var3.length);

         for (int var5 = 0; var5 < var3.length; var5++) {
            if (var3[var5] == '&' && var5 + 1 < var3.length && this.isValidColorCode(var3[var5 + 1])) {
               var4.append('§').append(var3[var5 + 1]);
               var5++;
            } else {
               var4.append(var3[var5]);
            }
         }

         return var4.toString();
      }
   }

   private boolean isValidColorCode(char var1) {
      return var1 >= '0' && var1 <= '9'
         || var1 >= 'a' && var1 <= 'f'
         || var1 >= 'A' && var1 <= 'F'
         || var1 == 'r'
         || var1 == 'l'
         || var1 == 'm'
         || var1 == 'n'
         || var1 == 'o'
         || var1 == 'k'
         || var1 == 'x';
   }

   public String getTitle() {
      return this.colorizedTitleCache != null ? this.colorizedTitleCache : "§6§lPRACTICE";
   }

   public String getTitle(PlayerScoreboardData var1) {
      if (this.parsedTitleCache != null && !this.parsedTitleCache.isFullyStatic()) {
         return this.parsedTitleCache.resolve(null, var1);
      } else {
         return this.colorizedTitleCache != null ? this.colorizedTitleCache : "§6§lPRACTICE";
      }
   }

   public AnimationManager getAnimationManager() {
      return this.animationManager;
   }

   public int getMinimumAnimationIntervalMs() {
      int var1 = Integer.MAX_VALUE;

      for (String var3 : this.animationManager.getAllAnimationNames()) {
         int var4 = this.animationManager.getAnimationInterval(var3);
         if (var4 > 0) {
            var1 = Math.min(var1, var4);
         }
      }

      return var1 == Integer.MAX_VALUE ? 200 : Math.max(100, var1);
   }

   private String getKitDisplayName(String var1) {
      return var1 != null && !var1.isEmpty()
         ? this.plugin
            .getKitManager()
            .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
               var1
            )
         : "";
   }

   public Component getTitleComponent() {
      String var1 = this.colorizedTitleCache != null ? this.colorizedTitleCache : "§6§lPRACTICE";
      return this.parseToComponent(var1);
   }

   public Component getTitleComponent(PlayerScoreboardData var1) {
      String var2;
      if (this.parsedTitleCache != null && !this.parsedTitleCache.isFullyStatic()) {
         var2 = this.parsedTitleCache.resolve(null, var1);
      } else {
         var2 = this.colorizedTitleCache != null ? this.colorizedTitleCache : "§6§lPRACTICE";
      }

      return this.parseToComponent(var2);
   }

   public List<Component> getComponentLines(Player var1, PlayerScoreboardData var2) {
      List var3 = this.getLinesWithAnimationInfo(var1, var2);
      ArrayList var4 = new ArrayList(var3.size());

      for (String[] var6 : var3) {
         String var7 = var6[0];
         boolean var8 = "1".equals(var6[1]);
         var4.add(this.parseToComponent(var7, var8));
      }

      return var4;
   }

   public List<Component> getComponentLines(Player var1, PlayerScoreboardData var2, List<String> var3) {
      List var4 = var2.getPreviousLineStrings();
      List var5 = var2.getPreviousLineComponents();
      ArrayList var6 = new ArrayList(var3.size());

      for (int var7 = 0; var7 < var3.size(); var7++) {
         String var8 = (String)var3.get(var7);
         if (var4 != null && var7 < var4.size() && var5 != null && var7 < var5.size() && var8.equals(var4.get(var7))) {
            var6.add((Component)var5.get(var7));
         } else {
            var6.add(this.parseToComponent(var8));
         }
      }

      var2.setPreviousLines(new ArrayList<>(var3), var6);
      return var6;
   }

   private Component parseToComponent(String var1) {
      return (Component)(var1 != null && !var1.isEmpty() ? this.componentCache.get(var1, this::parseToComponentInternal) : Component.empty());
   }

   private Component parseToComponent(String var1, boolean var2) {
      return this.parseToComponent(var1);
   }

   private Component parseToComponentInternal(String var1) {
      try {
         String var2 = f.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            var1
         );
         if (this.shadowEnabled && this.cachedShadowPrefix != null) {
            String var3 = f.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var2, this.cachedShadowPrefix, this.cachedShadowSuffix
            );
            return MINI_MESSAGE.deserialize(var3);
         } else {
            return MINI_MESSAGE.deserialize(var2);
         }
      } catch (Exception var4) {
         return f.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
            var1
         );
      }
   }

   private List<String> sanitizeScoreboardHeads(List<String> var1, PlayerScoreboardData var2) {
      return var1;
   }

   private String sanitizeScoreboardHeads(String var1, PlayerScoreboardData var2) {
      return var1;
   }
}
