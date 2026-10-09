package org.lime.swiftCore.tablist;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.lime.swiftCore.SwiftCore;
import org.lime.swiftCore.b.e;
import org.lime.swiftCore.b.f;
import org.lime.swiftCore.kit.n;
import org.lime.swiftCore.libs.caffeine.cache.Cache;
import org.lime.swiftCore.libs.caffeine.cache.Caffeine;
import org.lime.swiftCore.scoreboard.AnimationManager;

public class TablistProvider {
   private final SwiftCore plugin;
   private final FileConfiguration config;
   private final AnimationManager animationManager;
   private final MiniMessage miniMessage;
   private final Map<TablistContext, List<String>> headerCache;
   private final Map<TablistContext, List<String>> footerCache;
   private final Map<TablistContext, List<String>> colorizedHeaderCache;
   private final Map<TablistContext, List<String>> colorizedFooterCache;
   private final Map<TablistContext, String> joinedHeaderCache;
   private final Map<TablistContext, String> joinedFooterCache;
   private final Map<TablistContext, TablistProvider.TemplatePart[]> parsedHeaderCache;
   private final Map<TablistContext, TablistProvider.TemplatePart[]> parsedFooterCache;
   private static final ThreadLocal<StringBuilder> templateBuilder = ThreadLocal.withInitial(() -> new StringBuilder(256));
   private static final Pattern ANIMATION_PATTERN = Pattern.compile("%animation:([^%]+)%");
   private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("<([^>]+)>");
   private static final Pattern TEMPLATE_PART_PATTERN = Pattern.compile("%animation:[^%]+%|%[^%]+%|<[^>]+>");
   private static final Pattern HEX_PATTERN_1 = Pattern.compile("&x(&[0-9a-fA-F]){6}");
   private static final Pattern HEX_PATTERN_2 = Pattern.compile("&#([0-9a-fA-F]{6})");
   private boolean papiEnabled;
   private boolean shadowEnabled;
   private String shadowColor;
   private String cachedShadowPrefix;
   private String cachedShadowSuffix;
   private final Cache<String, Component> componentCache = Caffeine.newBuilder().maximumSize(2048L).expireAfterAccess(Duration.ofMinutes(5L)).build();

   public TablistProvider(SwiftCore var1, FileConfiguration var2) {
      this.plugin = var1;
      this.config = var2;
      this.animationManager = var1.getScoreboardManager().getProvider().getAnimationManager();
      this.miniMessage = e.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      this.headerCache = new EnumMap<>(TablistContext.class);
      this.footerCache = new EnumMap<>(TablistContext.class);
      this.colorizedHeaderCache = new EnumMap<>(TablistContext.class);
      this.colorizedFooterCache = new EnumMap<>(TablistContext.class);
      this.joinedHeaderCache = new EnumMap<>(TablistContext.class);
      this.joinedFooterCache = new EnumMap<>(TablistContext.class);
      this.parsedHeaderCache = new EnumMap<>(TablistContext.class);
      this.parsedFooterCache = new EnumMap<>(TablistContext.class);
      this.loadShadowConfig();
      this.checkPAPIStatus();
      this.buildCache();
   }

   private void loadShadowConfig() {
      this.shadowEnabled = this.config.getBoolean("tablist.shadow", false);
      this.shadowColor = this.config.getString("tablist.shadow-color", "#000000ff");
      if (this.shadowEnabled) {
         this.cachedShadowPrefix = "<shadow:" + this.shadowColor + ">";
         this.cachedShadowSuffix = "</shadow>";
      } else {
         this.cachedShadowPrefix = null;
         this.cachedShadowSuffix = null;
      }
   }

   private void checkPAPIStatus() {
      this.papiEnabled = Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
   }

   public void buildCache() {
      this.headerCache.clear();
      this.footerCache.clear();
      this.colorizedHeaderCache.clear();
      this.colorizedFooterCache.clear();
      this.joinedHeaderCache.clear();
      this.joinedFooterCache.clear();
      this.parsedHeaderCache.clear();
      this.parsedFooterCache.clear();

      for (TablistContext var4 : TablistContext.values()) {
         String var5;
         if (var4 == TablistContext.GLOBAL) {
            var5 = "global";
         } else {
            var5 = "tablist." + var4.name().toLowerCase();
         }

         List var6 = this.config.getStringList(var5 + ".header");
         List var7 = this.config.getStringList(var5 + ".footer");
         if (var6.isEmpty()) {
            var6 = this.config.getStringList("tablist.default.header");
         }

         if (var7.isEmpty()) {
            var7 = this.config.getStringList("tablist.default.footer");
         }

         this.headerCache.put(var4, new ArrayList<>(var6));
         this.footerCache.put(var4, new ArrayList<>(var7));
         ArrayList var8 = new ArrayList(var6.size());
         ArrayList var9 = new ArrayList(var7.size());

         for (String var11 : var6) {
            var8.add(this.colorize(var11));
         }

         for (String var14 : var7) {
            var9.add(this.colorize(var14));
         }

         this.colorizedHeaderCache.put(var4, var8);
         this.colorizedFooterCache.put(var4, var9);
         String var13 = String.join("\n", var8);
         String var15 = String.join("\n", var9);
         this.joinedHeaderCache.put(var4, var13);
         this.joinedFooterCache.put(var4, var15);
         this.parsedHeaderCache.put(var4, this.parseTemplate(var13));
         this.parsedFooterCache.put(var4, this.parseTemplate(var15));
      }
   }

   private TablistProvider.TemplatePart[] parseTemplate(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         ArrayList var2 = new ArrayList();
         Matcher var3 = TEMPLATE_PART_PATTERN.matcher(var1);

         int var4;
         for (var4 = 0; var3.find(); var4 = var3.end()) {
            if (var3.start() > var4) {
               var2.add(new TablistProvider.TemplatePart(var1.substring(var4, var3.start()), TablistProvider.TemplatePart.Type.LITERAL));
            }

            String var5 = var3.group();
            if (var5.startsWith("%animation:")) {
               var2.add(new TablistProvider.TemplatePart(var5.substring(11, var5.length() - 1), TablistProvider.TemplatePart.Type.ANIMATION));
            } else if (var5.startsWith("%") && var5.endsWith("%")) {
               var2.add(new TablistProvider.TemplatePart(var5, TablistProvider.TemplatePart.Type.PAPI));
            } else if (var5.startsWith("<") && var5.endsWith(">")) {
               String var6 = var5.substring(1, var5.length() - 1);
               if (this.isInternalTablistPlaceholder(var6)) {
                  var2.add(new TablistProvider.TemplatePart(var6, TablistProvider.TemplatePart.Type.INTERNAL));
               } else {
                  var2.add(new TablistProvider.TemplatePart(var5, TablistProvider.TemplatePart.Type.LITERAL));
               }
            } else {
               var2.add(new TablistProvider.TemplatePart(var5, TablistProvider.TemplatePart.Type.LITERAL));
            }
         }

         if (var4 < var1.length()) {
            var2.add(new TablistProvider.TemplatePart(var1.substring(var4), TablistProvider.TemplatePart.Type.LITERAL));
         }

         return var2.toArray(new TablistProvider.TemplatePart[0]);
      } else {
         return new TablistProvider.TemplatePart[0];
      }
   }

   private boolean isInternalTablistPlaceholder(String var1) {
      return switch (var1) {
         case "player", "opponent", "current_round", "total_rounds", "your_wins", "opponent_wins", "ffa_arena", "party_owner", "party_size", "party_max_size", "party_ffa_alive", "fight_kitname", "in_queue_kitname", "blue_alive", "blue_total", "red_alive", "red_total", "your_party_alive", "your_party_total", "enemy_party_alive", "enemy_party_total", "spectating_player", "spectating_arena", "spectating_kit", "round_winner", "opponent_hp", "team_queue_size", "team_queue_mode" -> true;
         default -> false;
      };
   }

   public void invalidateCache() {
      this.loadShadowConfig();
      this.buildCache();
      this.componentCache.invalidateAll();
   }

   public Component getHeader(Player var1, TablistPlayerData var2) {
      List var3 = this.colorizedHeaderCache.get(var2.getContext());
      if (var3 == null || var3.isEmpty()) {
         var3 = this.colorizedHeaderCache.get(TablistContext.DEFAULT);
      }

      return this.processLines(var3, var1, var2);
   }

   public Component getFooter(Player var1, TablistPlayerData var2) {
      List var3 = this.colorizedFooterCache.get(var2.getContext());
      if (var3 == null || var3.isEmpty()) {
         var3 = this.colorizedFooterCache.get(TablistContext.DEFAULT);
      }

      return this.processLines(var3, var1, var2);
   }

   public String getHeaderString(Player var1, TablistPlayerData var2) {
      TablistProvider.TemplatePart[] var3 = this.parsedHeaderCache.get(var2.getContext());
      if (var3 == null || var3.length == 0) {
         var3 = this.parsedHeaderCache.get(TablistContext.DEFAULT);
      }

      return var3 != null && var3.length != 0 ? this.resolveTemplate(var3, var1, var2) : "";
   }

   public String getFooterString(Player var1, TablistPlayerData var2) {
      TablistProvider.TemplatePart[] var3 = this.parsedFooterCache.get(var2.getContext());
      if (var3 == null || var3.length == 0) {
         var3 = this.parsedFooterCache.get(TablistContext.DEFAULT);
      }

      return var3 != null && var3.length != 0 ? this.resolveTemplate(var3, var1, var2) : "";
   }

   private String resolveTemplate(TablistProvider.TemplatePart[] var1, Player var2, TablistPlayerData var3) {
      StringBuilder var4 = templateBuilder.get();
      var4.setLength(0);

      for (TablistProvider.TemplatePart var8 : var1) {
         switch (var8.type) {
            case LITERAL:
               var4.append(var8.text);
               break;
            case ANIMATION:
               var4.append(this.animationManager.getAnimationFrame(var8.text));
               break;
            case PAPI:
               var4.append(this.resolvePapiPart(var8.text, var2, var3));
               break;
            case INTERNAL:
               String var9 = this.getInternalPlaceholder(var8.text, var2, var3);
               if (var9 != null && !var9.isEmpty()) {
                  if (var9.indexOf(38) >= 0) {
                     var9 = this.colorize(var9);
                  }

                  var4.append(var9);
               }
         }
      }

      return var4.toString();
   }

   private String resolvePapiPart(String var1, Player var2, TablistPlayerData var3) {
      if (this.papiEnabled && var1 != null && !var1.isEmpty()) {
         var1 = this.replaceInternalPlaceholders(var1, var2, var3);
         String var4 = "papi:" + var1;
         if (var3 != null) {
            String var5 = var3.getCachedPapi(var4);
            if (var5 != null) {
               return var5;
            }
         }

         String var7 = PlaceholderAPI.setPlaceholders(var2, var1);
         if (var3 != null) {
            var3.setCachedPapi(var4, var7);
         }

         return var7;
      } else {
         return var1 == null ? "" : var1;
      }
   }

   private String processTemplate(String var1, Player var2, TablistPlayerData var3) {
      var1 = this.replaceAnimations(var1);
      var1 = this.replaceInternalPlaceholders(var1, var2, var3);
      if (this.papiEnabled && var1.contains("%")) {
         String var4 = var1;
         if (var3 != null) {
            String var5 = var3.getCachedPapi(var1);
            if (var5 != null) {
               return var5;
            }
         }

         var1 = PlaceholderAPI.setPlaceholders(var2, var1);
         if (var3 != null) {
            var3.setCachedPapi(var4, var1);
         }
      }

      return var1;
   }

   public String renderConfigString(String var1, Player var2, TablistPlayerData var3) {
      return var1 != null && !var1.isEmpty() ? this.processTemplate(var1, var2, var3) : "";
   }

   private String processLinesAsString(List<String> var1, Player var2, TablistPlayerData var3) {
      if (var1 != null && !var1.isEmpty()) {
         String var4 = String.join("\n", var1);
         return this.processTemplate(var4, var2, var3);
      } else {
         return "";
      }
   }

   private Component processLines(List<String> var1, Player var2, TablistPlayerData var3) {
      String var4 = this.processLinesAsString(var1, var2, var3);
      return (Component)(var4.isEmpty() ? Component.empty() : this.parseToComponent(var4));
   }

   public Component stringToComponent(String var1) {
      return (Component)(var1 != null && !var1.isEmpty() ? this.componentCache.get(var1, this::parseToComponentCached) : Component.empty());
   }

   private Component parseToComponentCached(String var1) {
      return this.parseToComponent(var1);
   }

   private String replaceAnimations(String var1) {
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

   private String replaceInternalPlaceholders(String var1, Player var2, TablistPlayerData var3) {
      Matcher var4 = PLACEHOLDER_PATTERN.matcher(var1);
      StringBuffer var5 = new StringBuffer();

      while (var4.find()) {
         String var6 = var4.group(1);
         String var7 = this.getInternalPlaceholder(var6, var2, var3);
         if (var7 != null) {
            if (var7.indexOf(38) >= 0) {
               var7 = this.colorize(var7);
            }

            var4.appendReplacement(var5, Matcher.quoteReplacement(var7));
         }
      }

      var4.appendTail(var5);
      return var5.toString();
   }

   private String getInternalPlaceholder(String var1, Player var2, TablistPlayerData var3) {
      if (var3 != null && var2 != null) {
         String var4 = var3.getPlaceholder(var1);
         if (var4 != null) {
            return var4;
         } else {
            return switch (var1) {
               case "player" -> var2.getName();
               case "opponent" -> var3.getPlaceholder("opponent");
               case "current_round" -> var3.getPlaceholder("current_round");
               case "total_rounds" -> var3.getPlaceholder("total_rounds");
               case "your_wins" -> var3.getPlaceholder("your_wins");
               case "opponent_wins" -> var3.getPlaceholder("opponent_wins");
               case "ffa_arena" -> var3.getPlaceholder("ffa_arena");
               case "party_owner" -> var3.getPlaceholder("party_owner");
               case "party_size" -> var3.getPlaceholder("party_size");
               case "party_max_size" -> var3.getPlaceholder("party_max_size");
               case "party_ffa_alive" -> var3.getPlaceholder("party_ffa_alive");
               case "fight_kitname" -> var3.getPlaceholder("fight_kitname");
               case "in_queue_kitname" -> var3.getPlaceholder("in_queue_kitname");
               case "blue_alive" -> var3.getPlaceholder("blue_alive");
               case "blue_total" -> var3.getPlaceholder("blue_total");
               case "red_alive" -> var3.getPlaceholder("red_alive");
               case "red_total" -> var3.getPlaceholder("red_total");
               case "your_party_alive" -> var3.getPlaceholder("your_party_alive");
               case "your_party_total" -> var3.getPlaceholder("your_party_total");
               case "enemy_party_alive" -> var3.getPlaceholder("enemy_party_alive");
               case "enemy_party_total" -> var3.getPlaceholder("enemy_party_total");
               case "spectating_player" -> var3.getPlaceholder("spectating_player");
               case "spectating_arena" -> var3.getPlaceholder("spectating_arena");
               case "spectating_kit" -> var3.getPlaceholder("spectating_kit");
               case "round_winner" -> var3.getPlaceholder("round_winner");
               case "opponent_hp" -> {
                  n var7 = this.plugin.getHPIndicatorManager();
                  if (var7 != null && var7.isActive(var2.getUniqueId())) {
                     double var8 = var7.getOpponentHealth(var2.getUniqueId());
                     yield var8 >= 0.0 ? String.format("%.1f", var8) : "";
                  } else {
                     yield "";
                  }
               }
               default -> null;
            };
         }
      } else {
         return null;
      }
   }

   private String colorize(String var1) {
      Matcher var2 = HEX_PATTERN_1.matcher(var1);
      StringBuffer var3 = new StringBuffer();

      while (var2.find()) {
         String var4 = var2.group().replace("&", "");
         StringBuilder var5 = new StringBuilder("§x");

         for (char var9 : var4.toCharArray()) {
            if (var9 != 'x') {
               var5.append('§').append(var9);
            }
         }

         var2.appendReplacement(var3, var5.toString());
      }

      var2.appendTail(var3);
      var1 = var3.toString();
      Matcher var14 = HEX_PATTERN_2.matcher(var1);
      StringBuffer var15 = new StringBuffer();

      while (var14.find()) {
         String var16 = var14.group(1);
         StringBuilder var18 = new StringBuilder("§x");

         for (char var11 : var16.toCharArray()) {
            var18.append('§').append(var11);
         }

         var14.appendReplacement(var15, var18.toString());
      }

      var14.appendTail(var15);
      var1 = var15.toString();
      char[] var17 = var1.toCharArray();
      StringBuilder var19 = new StringBuilder();

      for (int var21 = 0; var21 < var17.length; var21++) {
         if (var17[var21] == '&' && var21 + 1 < var17.length) {
            char var23 = var17[var21 + 1];
            if (this.isValidColorCode(var23)) {
               var19.append('§').append(var23);
               var21++;
               continue;
            }
         }

         var19.append(var17[var21]);
      }

      return var19.toString();
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

   private Component parseToComponent(String var1) {
      try {
         String var2 = f.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
            var1
         );
         if (this.shadowEnabled && this.cachedShadowPrefix != null) {
            var2 = f.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var2, this.cachedShadowPrefix, this.cachedShadowSuffix
            );
         }

         return this.miniMessage.deserialize(var2);
      } catch (Exception var3) {
         return f.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
            var1
         );
      }
   }

   public AnimationManager getAnimationManager() {
      return this.animationManager;
   }

   public int getAnimationInterval(TablistContext var1) {
      List var2 = this.headerCache.get(var1);
      List var3 = this.footerCache.get(var1);
      int var4 = Integer.MAX_VALUE;
      boolean var5 = false;
      if (var2 != null) {
         for (String var7 : var2) {
            int var8 = this.extractAnimationInterval(var7);
            if (var8 > 0) {
               var4 = Math.min(var4, var8);
               var5 = true;
            }
         }
      }

      if (var3 != null) {
         for (String var10 : var3) {
            int var11 = this.extractAnimationInterval(var10);
            if (var11 > 0) {
               var4 = Math.min(var4, var11);
               var5 = true;
            }
         }
      }

      return var5 ? var4 : 0;
   }

   private int extractAnimationInterval(String var1) {
      Matcher var2 = ANIMATION_PATTERN.matcher(var1);
      int var3 = Integer.MAX_VALUE;
      boolean var4 = false;

      while (var2.find()) {
         String var5 = var2.group(1);
         int var6 = this.animationManager.getAnimationInterval(var5);
         if (var6 > 0) {
            var3 = Math.min(var3, var6);
            var4 = true;
         }
      }

      return var4 ? var3 : 0;
   }

   static final class TemplatePart {
      final String text;
      final TablistProvider.TemplatePart.Type type;

      TemplatePart(String var1, TablistProvider.TemplatePart.Type var2) {
         this.text = var1;
         this.type = var2;
      }

      static enum Type {
         LITERAL,
         ANIMATION,
         PAPI,
         INTERNAL;
      }
   }
}
