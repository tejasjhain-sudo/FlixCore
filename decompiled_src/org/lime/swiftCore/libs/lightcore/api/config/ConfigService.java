package org.lime.swiftCore.libs.lightcore.api.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ConfigService {
   private final JavaPlugin plugin;
   private final String fileName;
   private final Map<String, Object> cache;
   private FileConfiguration config;
   private Path path;
   private File file;

   private ConfigService(@NotNull JavaPlugin var1, @NotNull String var2) {
      this.plugin = var1;
      this.fileName = var2;
      this.cache = new ConcurrentHashMap<>();
   }

   @NotNull
   public static ConfigService create(@NotNull JavaPlugin var0, @NotNull String var1) {
      ConfigService var2 = new ConfigService(var0, var1);
      var2.load();
      return var2;
   }

   @NotNull
   public static ConfigService create(@NotNull JavaPlugin var0) {
      return create(var0, "config.yml");
   }

   @NotNull
   public ConfigService load() {
      try {
         this.path = this.plugin.getDataFolder().toPath().resolve(this.fileName);
         this.file = this.path.toFile();
         Files.createDirectories(this.path.getParent());
         if (!Files.exists(this.path)) {
            String var1 = this.fileName.replace("\\", "/");
            if (this.plugin.getResource(var1) != null) {
               this.plugin.saveResource(var1, false);
            } else {
               Files.createFile(this.path);
            }
         }

         this.config = YamlConfiguration.loadConfiguration(this.file);
         this.cache.clear();
         this.config.getKeys(true).forEach(var1x -> this.cache.put(var1x, this.config.get(var1x)));
      } catch (IOException var2) {
         this.plugin.getLogger().severe("Failed to load config: " + this.fileName);
         var2.printStackTrace();
      }

      return this;
   }

   public void reload() {
      this.config = YamlConfiguration.loadConfiguration(this.file);
      this.cache.clear();
      this.config.getKeys(true).forEach(var1 -> this.cache.put(var1, this.config.get(var1)));
   }

   public void save() {
      try {
         this.config.save(this.file);
      } catch (IOException var2) {
         this.plugin.getLogger().severe("Failed to save config: " + this.fileName);
         var2.printStackTrace();
      }
   }

   @NotNull
   public FileConfiguration getConfig() {
      return this.config;
   }

   @NotNull
   public File getFile() {
      return this.file;
   }

   @Nullable
   public <T> T get(@NotNull String var1) {
      return (T)this.cache.get(var1);
   }

   @NotNull
   public <T> T get(@NotNull String var1, @NotNull T var2) {
      Object var3 = this.cache.get(var1);
      return (T)(var3 != null ? var3 : var2);
   }

   @Nullable
   public Object getRaw(@NotNull String var1) {
      return this.config.get(var1);
   }

   @NotNull
   public ConfigService set(@NotNull String var1, @Nullable Object var2) {
      this.cache.put(var1, var2);
      this.config.set(var1, var2);
      return this;
   }

   public boolean contains(@NotNull String var1) {
      return this.config.contains(var1);
   }

   @NotNull
   public Set<String> getKeys(boolean var1) {
      return this.config.getKeys(var1);
   }

   @Nullable
   public String getString(@NotNull String var1) {
      Object var2 = this.getRaw(var1);
      if (var2 instanceof String) {
         return (String)var2;
      } else if (var2 instanceof List) {
         return ((List)var2).stream().map(Object::toString).collect(Collectors.joining(", "));
      } else {
         return var2 != null ? var2.toString() : null;
      }
   }

   @NotNull
   public String getString(@NotNull String var1, @NotNull String var2) {
      String var3 = this.getString(var1);
      return var3 != null ? var3 : var2;
   }

   public int getInt(@NotNull String var1) {
      return this.get(var1, 0);
   }

   public int getInt(@NotNull String var1, int var2) {
      return this.get(var1, var2);
   }

   public double getDouble(@NotNull String var1) {
      return this.get(var1, 0.0);
   }

   public double getDouble(@NotNull String var1, double var2) {
      return this.get(var1, var2);
   }

   public float getFloat(@NotNull String var1) {
      Object var2 = this.getRaw(var1);
      return var2 instanceof Number ? ((Number)var2).floatValue() : 0.0F;
   }

   public float getFloat(@NotNull String var1, float var2) {
      Object var3 = this.getRaw(var1);
      return var3 instanceof Number ? ((Number)var3).floatValue() : var2;
   }

   public long getLong(@NotNull String var1) {
      Object var2 = this.getRaw(var1);
      return var2 instanceof Number ? ((Number)var2).longValue() : 0L;
   }

   public long getLong(@NotNull String var1, long var2) {
      Object var4 = this.getRaw(var1);
      return var4 instanceof Number ? ((Number)var4).longValue() : var2;
   }

   public short getShort(@NotNull String var1) {
      Object var2 = this.getRaw(var1);
      return var2 instanceof Number ? ((Number)var2).shortValue() : 0;
   }

   public short getShort(@NotNull String var1, short var2) {
      Object var3 = this.getRaw(var1);
      return var3 instanceof Number ? ((Number)var3).shortValue() : var2;
   }

   public byte getByte(@NotNull String var1) {
      Object var2 = this.getRaw(var1);
      return var2 instanceof Number ? ((Number)var2).byteValue() : 0;
   }

   public byte getByte(@NotNull String var1, byte var2) {
      Object var3 = this.getRaw(var1);
      return var3 instanceof Number ? ((Number)var3).byteValue() : var2;
   }

   public boolean getBoolean(@NotNull String var1) {
      return this.get(var1, false);
   }

   public boolean getBoolean(@NotNull String var1, boolean var2) {
      return this.get(var1, var2);
   }

   @Nullable
   public List<?> getList(@NotNull String var1) {
      return this.get(var1);
   }

   @NotNull
   public List<?> getList(@NotNull String var1, @NotNull List<?> var2) {
      return this.get(var1, var2);
   }

   @NotNull
   public List<String> getStringList(@NotNull String var1) {
      return this.config.getStringList(var1);
   }

   @NotNull
   public List<Integer> getIntegerList(@NotNull String var1) {
      return this.config.getIntegerList(var1);
   }

   @NotNull
   public List<Double> getDoubleList(@NotNull String var1) {
      return this.config.getDoubleList(var1);
   }

   @NotNull
   public List<Long> getLongList(@NotNull String var1) {
      return this.config.getLongList(var1);
   }

   @NotNull
   public List<Boolean> getBooleanList(@NotNull String var1) {
      return this.config.getBooleanList(var1);
   }

   @Nullable
   public ConfigurationSection getSection(@NotNull String var1) {
      return this.config.getConfigurationSection(var1);
   }

   @Nullable
   public Map<String, Object> getMap(@NotNull String var1) {
      Object var2 = this.get(var1);
      return var2 instanceof Map ? (Map)var2 : null;
   }

   @Nullable
   public Location getLocation(@NotNull String var1) {
      return this.config.getLocation(var1);
   }

   @Nullable
   public Vector getVector(@NotNull String var1) {
      return this.config.getVector(var1);
   }

   @Nullable
   public ItemStack getItemStack(@NotNull String var1) {
      return this.config.getItemStack(var1);
   }

   @Nullable
   public OfflinePlayer getOfflinePlayer(@NotNull String var1) {
      return this.config.getOfflinePlayer(var1);
   }

   public void clearCache() {
      this.cache.clear();
   }
}
