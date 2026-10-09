package org.lime.swiftCore.spawn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerProfile;
import org.lime.swiftCore.SwiftCore;
import org.lime.swiftCore.scoreboard.PlayerScoreboardData;

public class SpawnItemsManager {
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
   private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%(\\w+)%");
   private static final Pattern MATERIAL_PROFILE_PATTERN = Pattern.compile("^([A-Za-z0-9_]+)(?:\\[profile=(?:\"([^\"]*)\"|'([^']*)'|([^\\]]+))])?$", 2);
   private static final int PLAYER_HEAD_PROFILE_CACHE_LIMIT = 512;
   private final SwiftCore plugin;
   private FileConfiguration spawnItemsConfig;
   private final Set<UUID> playersWithSpawnItems;
   private final Map<String, List<SpawnItemsManager._b>> itemCache = new ConcurrentHashMap<>();
   private boolean cachedHasPapi;
   private boolean cachedGiveOnLobbyWorldJoin;
   private int cachedAutoGiveDelay;
   private final Map<String, String> commandCache = new ConcurrentHashMap<>();
   private List<ItemFlag> globalItemFlags = new ArrayList<>();
   private final NamespacedKey commandKey;
   private final Map<String, PlayerProfile> playerHeadProfileCache = new ConcurrentHashMap<>();
   private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
   private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

   public SpawnItemsManager(SwiftCore var1) {
      this.plugin = var1;
      this.commandKey = new NamespacedKey(var1, "spawn_item_command");
      this.playersWithSpawnItems = new HashSet<>();
      this.loadConfig();
      this.buildCache();
      this.cachedHasPapi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
   }

   private void loadConfig() {
      File var1 = new File(this.plugin.getDataFolder(), "spawnitems.yml");
      if (!var1.exists()) {
         try {
            Files.createDirectories(var1.getParentFile().toPath());

            try (InputStream var2 = this.plugin.getResource("spawnitems.yml")) {
               if (var2 != null) {
                  Files.copy(var2, var1.toPath());
               }
            }
         } catch (IOException var7) {
            this.plugin.getLogger().severe("Failed to save spawnitems.yml: " + var7.getMessage());
         }
      }

      this.spawnItemsConfig = YamlConfiguration.loadConfiguration(var1);
   }

   private void buildCache() {
      this.itemCache.clear();
      this.commandCache.clear();
      this.globalItemFlags.clear();
      this.playerHeadProfileCache.clear();
      this.cachedHasPapi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
      ConfigurationSection var1 = this.spawnItemsConfig.getConfigurationSection("spawn-items");
      if (var1 != null) {
         for (String var4 : var1.getStringList("items-atributes")) {
            try {
               ItemFlag var5 = ItemFlag.valueOf(var4.toUpperCase().replace("-", "_"));
               this.globalItemFlags.add(var5);
            } catch (IllegalArgumentException var16) {
            }
         }

         for (String var18 : var1.getKeys(false)) {
            if (!var18.equals("items-atributes")) {
               ConfigurationSection var19 = var1.getConfigurationSection(var18);
               if (var19 != null) {
                  ArrayList var6 = new ArrayList();

                  for (String var8 : var19.getKeys(false)) {
                     ConfigurationSection var9 = var19.getConfigurationSection(var8);
                     if (var9 != null) {
                        try {
                           SpawnItemsManager._b var10 = this.buildCachedItem(var9);
                           if (var10 != null) {
                              var6.add(var10);
                              String var11 = var9.getString("command", "");
                              if (!var11.isEmpty()) {
                                 this.commandCache
                                    .put(
                                       var10.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new,
                                       var11
                                    );
                                 String var12 = var11.startsWith("/") ? var11.substring(1).split(" ")[0] : var11.split(" ")[0];

                                 try {
                                    if (Bukkit.getPluginCommand(var12) == null && Bukkit.getCommandMap().getCommand(var12) == null) {
                                       this.plugin
                                          .getLogger()
                                          .warning("[SpawnItems] Command '/" + var12 + "' in [" + var18 + "." + var8 + "] is not a registered command");
                                    }
                                 } catch (Exception var14) {
                                 }
                              }
                           }
                        } catch (IllegalArgumentException var15) {
                           this.plugin.getLogger().warning("Invalid material in spawn items [" + var18 + "." + var8 + "]: " + var15.getMessage());
                        }
                     }
                  }

                  this.itemCache.put(var18, var6);
               }
            }
         }

         this.cachedGiveOnLobbyWorldJoin = this.spawnItemsConfig.getBoolean("spawn-items.settings.give-on-lobby-world-join", true);
         this.cachedAutoGiveDelay = this.spawnItemsConfig.getInt("spawn-items.settings.auto-give-delay", 3);
         this.plugin.getLogger().info("Cached " + this.itemCache.values().stream().mapToInt(List::size).sum() + " spawn items");
      }
   }

   private SpawnItemsManager._b buildCachedItem(ConfigurationSection var1) {
      String var2 = var1.getString("material", "STONE");
      SpawnItemsManager._c var3 = this.parseMaterial(var2);
      Material var4 = var3.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super();
      String var5 = var3.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new();
      String var6 = var1.getString("display-name", "");
      String var7 = var1.getString("command", "");
      int var8 = var1.getInt("slot", 0);
      int var9 = var1.getInt("amount", 1);
      boolean var10 = var1.getBoolean("party", false);
      boolean var11 = var1.getBoolean("party-owner", false);
      boolean var12 = var1.getBoolean("glow", false);
      int var13 = var1.getInt("custom-model-data", -1);
      boolean var14 = var1.getBoolean("manual-only", false);
      ItemStack var15 = new ItemStack(var4, var9);
      ItemMeta var16 = var15.getItemMeta();
      String var17 = "";
      if (var16 != null) {
         if (!var6.isEmpty()) {
            Component var18 = this.parseText(var6);
            var16.displayName(var18);
            var17 = LEGACY_SERIALIZER.serialize(var18);
         }

         List var26 = var1.getStringList("lore");
         if (!var26.isEmpty()) {
            ArrayList var19 = new ArrayList();

            for (String var21 : var26) {
               var19.add(this.parseText(var21));
            }

            var16.lore(var19);
         }

         for (ItemFlag var31 : this.globalItemFlags) {
            var16.addItemFlags(new ItemFlag[]{var31});
         }

         for (String var35 : var1.getStringList("item-flags")) {
            try {
               ItemFlag var22 = ItemFlag.valueOf(var35.toUpperCase().replace("-", "_"));
               var16.addItemFlags(new ItemFlag[]{var22});
            } catch (IllegalArgumentException var25) {
            }
         }

         if (var13 > 0) {
            var16.setCustomModelData(var13);
         }

         if (!var7.isBlank()) {
            var16.getPersistentDataContainer().set(this.commandKey, PersistentDataType.STRING, var7);
         }

         if (var12) {
            var16.setEnchantmentGlintOverride(true);
         }

         ConfigurationSection var33 = var1.getConfigurationSection("enchantments");
         if (var33 != null) {
            for (String var38 : var33.getKeys(false)) {
               int var23 = var33.getInt(var38, 1);
               Enchantment var24 = Enchantment.getByKey(NamespacedKey.minecraft(var38.toLowerCase()));
               if (var24 != null) {
                  var16.addEnchant(var24, var23, true);
               }
            }
         }

         var15.setItemMeta(var16);
      }

      List var27 = var1.getStringList("lore");
      boolean var30 = containsPlaceholder(var6) || containsPlaceholder(var5);
      if (!var30) {
         for (String var37 : var27) {
            if (containsPlaceholder(var37)) {
               var30 = true;
               break;
            }
         }
      }

      return new SpawnItemsManager._b(var15, var8, var10, var11, var14, var6, var17, var6, var27, var30, var5);
   }

   private SpawnItemsManager._c parseMaterial(String var1) {
      if (var1 != null && !var1.isBlank()) {
         Matcher var2 = MATERIAL_PROFILE_PATTERN.matcher(var1.trim());
         if (!var2.matches()) {
            return new SpawnItemsManager._c(Material.valueOf(var1.trim().toUpperCase(Locale.ROOT)), null);
         } else {
            Material var3 = Material.valueOf(var2.group(1).toUpperCase(Locale.ROOT));
            String var4 = var2.group(2);
            if (var4 == null) {
               var4 = var2.group(3);
            }

            if (var4 == null) {
               var4 = var2.group(4);
            }

            if (var4 != null) {
               var4 = var4.trim();
               if (var4.isEmpty()) {
                  var4 = null;
               }
            }

            return new SpawnItemsManager._c(var3, var4);
         }
      } else {
         return new SpawnItemsManager._c(Material.STONE, null);
      }
   }

   private static boolean containsPlaceholder(String var0) {
      return var0 != null && var0.contains("%");
   }

   private Component parseText(String var1) {
      Matcher var4 = HEX_PATTERN.matcher(var1);
      StringBuilder var5 = new StringBuilder();

      while (var4.find()) {
         var4.appendReplacement(var5, "<#" + var4.group(1) + ">");
      }

      var4.appendTail(var5);
      String var3 = var5.toString();
      var3 = var3.replace("&0", "<black>")
         .replace("&1", "<dark_blue>")
         .replace("&2", "<dark_green>")
         .replace("&3", "<dark_aqua>")
         .replace("&4", "<dark_red>")
         .replace("&5", "<dark_purple>")
         .replace("&6", "<gold>")
         .replace("&7", "<gray>")
         .replace("&8", "<dark_gray>")
         .replace("&9", "<blue>")
         .replace("&a", "<green>")
         .replace("&b", "<aqua>")
         .replace("&c", "<red>")
         .replace("&d", "<light_purple>")
         .replace("&e", "<yellow>")
         .replace("&f", "<white>")
         .replace("&k", "<obfuscated>")
         .replace("&l", "<bold>")
         .replace("&m", "<strikethrough>")
         .replace("&n", "<underlined>")
         .replace("&o", "<italic>")
         .replace("&r", "<reset>");

      Object var2;
      try {
         var2 = MINI_MESSAGE.deserialize(var3);
      } catch (Exception var7) {
         var2 = LEGACY_SERIALIZER.deserialize(var1);
      }

      return var2.decoration(TextDecoration.ITALIC, false);
   }

   public void giveSpawnItems(Player var1, String var2, boolean var3, boolean var4) {
      Bukkit.getScheduler()
         .runTaskAsynchronously(
            this.plugin,
            () -> {
               HashMap var5 = new HashMap();
               if (var2 == null || var2.equals("default")) {
                  List var6 = this.itemCache.get("default");
                  if (var6 != null) {
                     for (SpawnItemsManager._b var8 : var6) {
                        if (!var8.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
                           && var8.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if
                              >= 0
                           && var8.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if
                              < 36) {
                           var5.put(
                              var8.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if,
                              this.applyPlaceholders(var8, var1)
                           );
                        }
                     }
                  }
               }

               if (var2 != null && !var2.equals("default")) {
                  List var9 = this.itemCache.get(var2);
                  if (var9 != null) {
                     for (SpawnItemsManager._b var13 : var9) {
                        if (!var13.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
                           && (
                              !"party".equalsIgnoreCase(var2)
                                 || !var13.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return
                                    && !var13.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                           )
                           && var13.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if
                              >= 0
                           && var13.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if
                              < 36) {
                           var5.put(
                              var13.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if,
                              this.applyPlaceholders(var13, var1)
                           );
                        }
                     }
                  }
               }

               if (var3) {
                  List var10 = this.itemCache.get("party");
                  if (var10 != null) {
                     for (SpawnItemsManager._b var14 : var10) {
                        if (!var14.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
                           && (
                              !var14.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                                 || var4
                           )
                           && (
                              !var14.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return
                                 || !var4
                           )
                           && (
                              var14.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return
                                 || var14.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                           )
                           && var14.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if
                              >= 0
                           && var14.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if
                              < 36) {
                           var5.put(
                              var14.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if,
                              this.applyPlaceholders(var14, var1)
                           );
                        }
                     }
                  }
               }

               Bukkit.getScheduler()
                  .runTask(
                     this.plugin,
                     () -> {
                        if (var1.isOnline()) {
                           if (this.plugin.getKitEditor() != null
                              && this.plugin
                                 .getKitEditor()
                                 .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                                    var1
                                 )) {
                              if (this.plugin.isDebug()) {
                                 this.plugin
                                    .getLogger()
                                    .info(
                                       "[KitEditor Debug] Skipped async giveSpawnItems for "
                                          + var1.getName()
                                          + " — player entered kit editor while task was queued (state="
                                          + var2
                                          + ", kit="
                                          + this.plugin
                                             .getKitEditor()
                                             .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
                                                var1
                                             )
                                          + ")."
                                    );
                              }
                           } else {
                              var1.getInventory().clear();

                              for (Entry var5x : var5.entrySet()) {
                                 var1.getInventory().setItem((Integer)var5x.getKey(), (ItemStack)var5x.getValue());
                              }

                              this.playersWithSpawnItems.add(var1.getUniqueId());
                           }
                        }
                     }
                  );
            }
         );
   }

   public void giveSpawnItemsSync(Player var1, String var2, boolean var3, boolean var4) {
      if (this.plugin.getKitEditor() != null
         && this.plugin
            .getKitEditor()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var1
            )) {
         if (this.plugin.isDebug()) {
            this.plugin
               .getLogger()
               .info("[KitEditor Debug] Skipped giveSpawnItemsSync for " + var1.getName() + " — player is in kit editor (state=" + var2 + ").");
         }
      } else {
         var1.getInventory().clear();
         if (var2 == null || var2.equals("default")) {
            List var5 = this.itemCache.get("default");
            if (var5 != null) {
               for (SpawnItemsManager._b var7 : var5) {
                  if (!var7.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
                     && var7.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if
                        >= 0
                     && var7.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if
                        < 36) {
                     var1.getInventory()
                        .setItem(
                           var7.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if,
                           this.applyPlaceholders(var7, var1)
                        );
                  }
               }
            }
         }

         if (var2 != null && !var2.equals("default")) {
            List var8 = this.itemCache.get(var2);
            if (var8 != null) {
               for (SpawnItemsManager._b var12 : var8) {
                  if (!var12.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
                     && (
                        !"party".equalsIgnoreCase(var2)
                           || !var12.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return
                              && !var12.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                     )
                     && var12.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if
                        >= 0
                     && var12.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if
                        < 36) {
                     var1.getInventory()
                        .setItem(
                           var12.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if,
                           this.applyPlaceholders(var12, var1)
                        );
                  }
               }
            }
         }

         if (var3) {
            List var9 = this.itemCache.get("party");
            if (var9 != null) {
               for (SpawnItemsManager._b var13 : var9) {
                  if (!var13.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object
                     && (
                        !var13.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                           || var4
                     )
                     && (
                        !var13.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return
                           || !var4
                     )
                     && (
                        var13.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return
                           || var13.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                     )
                     && var13.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if
                        >= 0
                     && var13.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if
                        < 36) {
                     var1.getInventory()
                        .setItem(
                           var13.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if,
                           this.applyPlaceholders(var13, var1)
                        );
                  }
               }
            }
         }

         this.playersWithSpawnItems.add(var1.getUniqueId());
      }
   }

   public void removeSpawnItems(Player var1) {
      var1.getInventory().clear();
      this.playersWithSpawnItems.remove(var1.getUniqueId());
   }

   public void clearTracking(Player var1) {
      this.playersWithSpawnItems.remove(var1.getUniqueId());
   }

   public boolean hasSpawnItems(Player var1) {
      return this.playersWithSpawnItems.contains(var1.getUniqueId());
   }

   public String getCommandForItem(ItemStack var1) {
      if (var1 == null) {
         return null;
      } else {
         ItemMeta var2 = var1.getItemMeta();
         if (var2 == null) {
            return null;
         } else {
            String var3 = (String)var2.getPersistentDataContainer().get(this.commandKey, PersistentDataType.STRING);
            if (var3 != null && !var3.isBlank()) {
               return var3;
            } else {
               Component var4 = var2.displayName();
               return var4 == null ? null : this.commandCache.get(LEGACY_SERIALIZER.serialize(var4));
            }
         }
      }
   }

   public void reload() {
      this.loadConfig();
      this.buildCache();
      this.refreshAllOnlinePlayers();
   }

   public void refreshAllOnlinePlayers() {
      Bukkit.getScheduler().runTask(this.plugin, () -> {
         for (Player var2 : Bukkit.getOnlinePlayers()) {
            if (this.playersWithSpawnItems.contains(var2.getUniqueId())) {
               UUID var3 = var2.getUniqueId();
               String var4 = this.resolvePlayerState(var3);
               boolean var5 = this.plugin.getPartyManager() != null && this.plugin.getPartyManager().isInParty(var3);
               boolean var6 = var5 && this.plugin.getPartyManager().isPartyOwner(var3);
               this.giveSpawnItems(var2, var4, var5, var6);
            }
         }
      });
   }

   public String resolvePlayerState(UUID var1) {
      if (this.plugin.getDuelManager().isInMatch(var1)) {
         return null;
      } else if (this.plugin.getFFAManager().isInFFA(var1)) {
         return null;
      } else if (this.plugin
         .getSpectatorManager()
         .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
            var1
         )) {
         return "spectating";
      } else if (this.plugin.getTournamentManager() != null
         && this.plugin
            .getTournamentManager()
            .ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if(
               var1
            )) {
         return "spectating";
      } else if (this.plugin.getEventManager() != null
         && this.plugin
            .getEventManager()
            .Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String(
               var1
            )) {
         return "spectating";
      } else if (this.plugin.getTournamentManager() != null
         && this.plugin
            .getTournamentManager()
            .Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
               var1
            )) {
         return "tournament";
      } else if (this.plugin.getEventManager() != null
         && this.plugin
            .getEventManager()
            .Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object(
               var1
            )) {
         return "event";
      } else {
         return this.plugin.getQueueManager().isInAnyQueue(var1) ? "queue" : "default";
      }
   }

   private ItemStack applyPlaceholders(SpawnItemsManager._b var1, Player var2) {
      if (!var1.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super()
         )
       {
         return var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float
            .clone();
      } else {
         ItemStack var3 = var1.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float
            .clone();
         Object var4 = var3.getItemMeta();
         if (var4 == null) {
            return var3;
         } else {
            boolean var5 = this.cachedHasPapi;
            if (var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int
                  != null
               && var3.getType() == Material.PLAYER_HEAD
               && var4 instanceof SkullMeta var6) {
               String var7 = this.resolveProfileTarget(
                  var1.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int,
                  var2,
                  var5
               );
               PlayerProfile var8 = this.resolvePlayerHeadProfile(var7);
               if (var8 != null) {
                  var6.setOwnerProfile(var8);
               }

               var4 = var6;
            }

            if (var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void
                  != null
               && !var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void
                  .isEmpty()) {
               String var10 = this.resolveInternalPlaceholders(
                  var1.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void,
                  var2
               );
               if (var5) {
                  var10 = PlaceholderAPI.setPlaceholders(var2, var10);
               }

               var4.displayName(this.parseText(var10));
            }

            if (var1.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class
                  != null
               && !var1.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class
                  .isEmpty()) {
               ArrayList var11 = new ArrayList();

               for (String var13 : var1.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class) {
                  String var9 = this.resolveInternalPlaceholders(var13, var2);
                  if (var5) {
                     var9 = PlaceholderAPI.setPlaceholders(var2, var9);
                  }

                  var11.add(this.parseText(var9));
               }

               var4.lore(var11);
            }

            var3.setItemMeta((ItemMeta)var4);
            return var3;
         }
      }
   }

   private String resolveProfileTarget(String var1, Player var2, boolean var3) {
      if (var1 != null && !var1.isBlank()) {
         String var4 = var1.replace("%player%", var2.getName());
         var4 = this.resolveInternalPlaceholders(var4, var2);
         if (var3) {
            var4 = PlaceholderAPI.setPlaceholders(var2, var4);
         }

         return var4.trim();
      } else {
         return var2.getName();
      }
   }

   private PlayerProfile resolvePlayerHeadProfile(String var1) {
      if (var1 != null && !var1.isBlank()) {
         String var2 = var1.toLowerCase(Locale.ROOT);
         PlayerProfile var3 = this.playerHeadProfileCache.get(var2);
         if (var3 != null) {
            return var3;
         } else {
            PlayerProfile var4 = this.loadPlayerHeadProfile(var1);
            if (var4 != null) {
               if (this.playerHeadProfileCache.size() >= 512) {
                  this.playerHeadProfileCache.clear();
               }

               this.playerHeadProfileCache.put(var2, var4);
            }

            return var4;
         }
      } else {
         return null;
      }
   }

   private PlayerProfile loadPlayerHeadProfile(String var1) {
      try {
         PlayerProfile var2;
         try {
            UUID var3 = UUID.fromString(var1);
            var2 = Bukkit.createPlayerProfile(var3);
         } catch (IllegalArgumentException var4) {
            var2 = Bukkit.createPlayerProfile(var1);
         }

         var2.update().get(3L, TimeUnit.SECONDS);
         return var2;
      } catch (Exception var5) {
         this.plugin.getLogger().warning("[SpawnItems] Failed to load player head profile for '" + var1 + "': " + var5.getMessage());
         return null;
      }
   }

   private String resolveInternalPlaceholders(String var1, Player var2) {
      PlayerScoreboardData var3 = this.plugin.getScoreboardManager().getData(var2);
      if (var3 == null) {
         return var1;
      } else {
         Matcher var4 = PLACEHOLDER_PATTERN.matcher(var1);
         StringBuilder var5 = new StringBuilder();

         while (var4.find()) {
            String var6 = var4.group(1);
            if (!var6.contains("_") || !var6.startsWith("swiftcore")) {
               String var7 = var3.getPlaceholder(var6);
               if (var7 != null && !var7.isEmpty()) {
                  var4.appendReplacement(var5, Matcher.quoteReplacement(var7));
               }
            }
         }

         var4.appendTail(var5);
         return var5.toString();
      }
   }

   public boolean isGiveOnLobbyWorldJoin() {
      return this.cachedGiveOnLobbyWorldJoin;
   }

   public int getAutoGiveDelay() {
      return this.cachedAutoGiveDelay;
   }

   public FileConfiguration getSpawnItemsConfig() {
      return this.spawnItemsConfig;
   }

   private static class _b {
      final ItemStack ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float;
      final int ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if;
      final boolean Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return;
      final boolean o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
      final boolean Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object;
      final String ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null;
      final String Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new;
      final String Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void;
      final List<String> Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class;
      final String õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int;
      final boolean Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String;

      _b(
         ItemStack var1,
         int var2,
         boolean var3,
         boolean var4,
         boolean var5,
         String var6,
         String var7,
         String var8,
         List<String> var9,
         boolean var10,
         String var11
      ) {
         this.ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000float = var1;
         this.ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000if = var2;
         this.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return = var3;
         this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = var4;
         this.Ó000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000Object = var5;
         this.ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null = var6;
         this.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = var7;
         this.Ø000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000void = var8;
         this.Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class = var9;
         this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String = var10;
         this.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int = var11;
      }

      boolean o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super() {
         return this.Ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000String
            || this.õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000int
               != null;
      }
   }

   private static record _c(
      Material o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super,
      String Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
   ) {
   }
}
