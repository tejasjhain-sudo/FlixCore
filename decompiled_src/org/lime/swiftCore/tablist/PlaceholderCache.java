package org.lime.swiftCore.tablist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderCache {
   private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
   private final Map<String, PlaceholderCache.NativePlaceholder> nativePlaceholders = new ConcurrentHashMap<>();
   private final Map<UUID, Map<String, PlaceholderCache.CachedValue>> playerCache = new ConcurrentHashMap<>();
   private final Map<String, PlaceholderCache.ParsedTemplate> templateCache = new ConcurrentHashMap<>();
   private final TablistManager manager;

   public PlaceholderCache(TablistManager var1) {
      this.manager = var1;
      this.registerDefaults();
      this.registerLuckPermsPlaceholders();
   }

   private void registerDefaults() {
      this.register("player_name", PlaceholderCache.RefreshType.STATIC, 0L, (var0, var1) -> var0.getName());
      this.register("player_displayname", PlaceholderCache.RefreshType.STATIC, 0L, (var0, var1) -> var0.getDisplayName());
      this.register("player_uuid", PlaceholderCache.RefreshType.STATIC, 0L, (var0, var1) -> var0.getUniqueId().toString());
      this.register("player_ping", PlaceholderCache.RefreshType.TIMED, 500L, (var0, var1) -> String.valueOf(var0.getPing()));
      this.register("player_health", PlaceholderCache.RefreshType.TIMED, 200L, (var0, var1) -> String.valueOf((int)var0.getHealth()));
      this.register("player_max_health", PlaceholderCache.RefreshType.TIMED, 1000L, (var0, var1) -> String.valueOf((int)var0.getMaxHealth()));
      this.register("player_food_level", PlaceholderCache.RefreshType.TIMED, 500L, (var0, var1) -> String.valueOf(var0.getFoodLevel()));
      this.register("player_level", PlaceholderCache.RefreshType.TIMED, 1000L, (var0, var1) -> String.valueOf(var0.getLevel()));
      this.register("player_exp", PlaceholderCache.RefreshType.TIMED, 1000L, (var0, var1) -> String.valueOf((int)(var0.getExp() * 100.0F)));
      this.register("player_world", PlaceholderCache.RefreshType.TIMED, 2000L, (var0, var1) -> var0.getWorld().getName());
      this.register("player_gamemode", PlaceholderCache.RefreshType.TIMED, 2000L, (var0, var1) -> var0.getGameMode().name());
      this.register("server_online", PlaceholderCache.RefreshType.TIMED, 1000L, (var0, var1) -> String.valueOf(Bukkit.getOnlinePlayers().size()));
      this.register("server_max_players", PlaceholderCache.RefreshType.STATIC, 0L, (var0, var1) -> String.valueOf(Bukkit.getMaxPlayers()));
      this.register("server_tps", PlaceholderCache.RefreshType.TIMED, 1000L, (var0, var1) -> {
         double[] var2 = Bukkit.getTPS();
         return String.format("%.1f", Math.min(var2[0], 20.0));
      });
   }

   private void registerLuckPermsPlaceholders() {
      if (Bukkit.getPluginManager().getPlugin("LuckPerms") != null) {
         this.register("luckperms_prefix", PlaceholderCache.RefreshType.EVENT, 0L, (var1, var2) -> {
            LuckPermsListener var3 = this.manager.getLuckPermsListener();
            return var3 != null ? var3.getPrefix(var1.getUniqueId()) : "";
         });
         this.register("luckperms_suffix", PlaceholderCache.RefreshType.EVENT, 0L, (var1, var2) -> {
            LuckPermsListener var3 = this.manager.getLuckPermsListener();
            return var3 != null ? var3.getSuffix(var1.getUniqueId()) : "";
         });
         this.register("luckperms_primary_group", PlaceholderCache.RefreshType.EVENT, 0L, (var1, var2) -> {
            LuckPermsListener var3 = this.manager.getLuckPermsListener();
            return var3 != null ? var3.getGroup(var1.getUniqueId()) : "default";
         });
      }
   }

   public void register(String var1, PlaceholderCache.RefreshType var2, long var3, BiFunction<Player, TablistPlayerData, String> var5) {
      this.nativePlaceholders.put(var1.toLowerCase(), new PlaceholderCache.NativePlaceholder(var1, var2, var3, var5));
   }

   public void invalidate(UUID var1, String var2) {
      Map var3 = this.playerCache.get(var1);
      if (var3 != null) {
         var3.remove(var2.toLowerCase());
      }
   }

   public void invalidateAll(UUID var1) {
      this.playerCache.remove(var1);
   }

   public void invalidateAllPlayers(String var1) {
      String var2 = var1.toLowerCase();
      this.playerCache.values().forEach(var1x -> var1x.remove(var2));
   }

   public void invalidateEventPlaceholders(UUID var1) {
      Map var2 = this.playerCache.get(var1);
      if (var2 != null) {
         this.nativePlaceholders.forEach((var1x, var2x) -> {
            if (var2x.type() == PlaceholderCache.RefreshType.EVENT) {
               var2.remove(var1x);
            }
         });
      }
   }

   public PlaceholderCache.ParsedTemplate preScan(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         PlaceholderCache.ParsedTemplate var2 = this.templateCache.get(var1);
         if (var2 != null) {
            return var2;
         } else {
            ArrayList var3 = new ArrayList();
            ArrayList var4 = new ArrayList();
            Matcher var5 = PLACEHOLDER_PATTERN.matcher(var1);

            while (var5.find()) {
               var3.add(var5.group(1).toLowerCase());
               var4.add(var5.start());
               var4.add(var5.end());
            }

            int[] var6 = var4.stream().mapToInt(Integer::intValue).toArray();
            PlaceholderCache.ParsedTemplate var7 = new PlaceholderCache.ParsedTemplate(var1, var3, var6);
            this.templateCache.put(var1, var7);
            return var7;
         }
      } else {
         return new PlaceholderCache.ParsedTemplate(var1, Collections.emptyList(), new int[0]);
      }
   }

   public String resolvePreScanned(Player var1, TablistPlayerData var2, PlaceholderCache.ParsedTemplate var3) {
      if (var3 != null && !var3.placeholderKeys().isEmpty()) {
         UUID var4 = var1.getUniqueId();
         Map var5 = this.playerCache.computeIfAbsent(var4, var0 -> new ConcurrentHashMap<>());
         String var6 = var3.template();
         List var7 = var3.placeholderKeys();
         int[] var8 = var3.placeholderPositions();
         StringBuilder var9 = new StringBuilder();
         int var10 = 0;
         boolean var11 = false;

         for (int var12 = 0; var12 < var7.size(); var12++) {
            int var13 = var8[var12 * 2];
            int var14 = var8[var12 * 2 + 1];
            var9.append(var6, var10, var13);
            String var15 = (String)var7.get(var12);
            PlaceholderCache.NativePlaceholder var16 = this.nativePlaceholders.get(var15);
            if (var16 != null) {
               var9.append(this.resolveNative(var1, var2, var5, var16));
            } else {
               var9.append(var6, var13, var14);
               var11 = true;
            }

            var10 = var14;
         }

         var9.append(var6, var10, var6.length());
         if (var11 && Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            String var17 = var9.toString();
            if (var2 != null) {
               String var18 = "tab:papi:" + var17;
               String var19 = var2.getCachedPapi(var18);
               if (var19 != null) {
                  return var19;
               } else {
                  String var20 = PlaceholderAPI.setPlaceholders(var1, var17);
                  var2.setCachedPapi(var18, var20);
                  return var20;
               }
            } else {
               return PlaceholderAPI.setPlaceholders(var1, var17);
            }
         } else {
            return var9.toString();
         }
      } else {
         return var3 != null ? var3.template() : "";
      }
   }

   public String resolve(Player var1, TablistPlayerData var2, String var3) {
      if (var3 != null && !var3.isEmpty()) {
         PlaceholderCache.ParsedTemplate var4 = this.preScan(var3);
         return this.resolvePreScanned(var1, var2, var4);
      } else {
         return "";
      }
   }

   private String resolveNative(Player var1, TablistPlayerData var2, Map<String, PlaceholderCache.CachedValue> var3, PlaceholderCache.NativePlaceholder var4) {
      String var5 = var4.key().toLowerCase();
      if (var4.type() == PlaceholderCache.RefreshType.STATIC) {
         PlaceholderCache.CachedValue var9 = (PlaceholderCache.CachedValue)var3.get(var5);
         if (var9 != null) {
            return var9.value();
         } else {
            String var11 = var4.resolver().apply(var1, var2);
            var3.put(var5, new PlaceholderCache.CachedValue(var11, Long.MAX_VALUE));
            return var11;
         }
      } else if (var4.type() == PlaceholderCache.RefreshType.TIMED) {
         PlaceholderCache.CachedValue var8 = (PlaceholderCache.CachedValue)var3.get(var5);
         if (var8 != null && !var8.isExpired()) {
            return var8.value();
         } else {
            String var10 = var4.resolver().apply(var1, var2);
            var3.put(var5, new PlaceholderCache.CachedValue(var10, System.currentTimeMillis() + var4.refreshInterval()));
            return var10;
         }
      } else if (var4.type() == PlaceholderCache.RefreshType.EVENT) {
         PlaceholderCache.CachedValue var6 = (PlaceholderCache.CachedValue)var3.get(var5);
         if (var6 != null) {
            return var6.value();
         } else {
            String var7 = var4.resolver().apply(var1, var2);
            var3.put(var5, new PlaceholderCache.CachedValue(var7, Long.MAX_VALUE));
            return var7;
         }
      } else {
         return var4.resolver().apply(var1, var2);
      }
   }

   public void updateEventPlaceholder(UUID var1, String var2, String var3) {
      Map var4 = this.playerCache.get(var1);
      if (var4 != null) {
         var4.put(var2.toLowerCase(), new PlaceholderCache.CachedValue(var3, Long.MAX_VALUE));
      }
   }

   public void cleanup(UUID var1) {
      this.playerCache.remove(var1);
   }

   public void clearAll() {
      this.playerCache.clear();
   }

   public boolean hasNativePlaceholder(String var1) {
      return this.nativePlaceholders.containsKey(var1.toLowerCase());
   }

   public Map<String, PlaceholderCache.NativePlaceholder> getNativePlaceholders() {
      return this.nativePlaceholders;
   }

   private static record CachedValue(String value, long expireAt) {
      boolean isExpired() {
         return System.currentTimeMillis() >= this.expireAt;
      }
   }

   public static record NativePlaceholder(
      String key, PlaceholderCache.RefreshType type, long refreshInterval, BiFunction<Player, TablistPlayerData, String> resolver
   ) {
   }

   public static record ParsedTemplate(String template, List<String> placeholderKeys, int[] placeholderPositions) {
   }

   public static enum RefreshType {
      STATIC,
      EVENT,
      TIMED;
   }
}
