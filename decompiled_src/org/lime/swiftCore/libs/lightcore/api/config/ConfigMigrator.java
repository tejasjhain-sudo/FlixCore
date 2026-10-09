package org.lime.swiftCore.libs.lightcore.api.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lime.swiftCore.libs.lightcore.api.logging.ConsoleLogger;

public final class ConfigMigrator {
   private final JavaPlugin plugin;
   private final String fileName;
   private final String versionPath;
   private final String backupPath;
   private final boolean makeBackup;
   private final File file;

   private ConfigMigrator(@NotNull JavaPlugin var1, @NotNull String var2, @NotNull String var3, @NotNull String var4, boolean var5) {
      this.plugin = var1;
      this.fileName = var2;
      this.versionPath = var3;
      this.backupPath = var4;
      this.makeBackup = var5;
      this.file = new File(var1.getDataFolder(), var2);
   }

   @NotNull
   public static ConfigMigrator.Builder builder(@NotNull JavaPlugin var0) {
      return new ConfigMigrator.Builder(var0);
   }

   @NotNull
   public static ConfigMigrator of(@NotNull JavaPlugin var0, @NotNull String var1, @NotNull String var2) {
      return builder(var0).fileName(var1).versionPath(var2).build();
   }

   public boolean migrate() {
      YamlConfiguration var1 = this.loadLatestConfig();
      YamlConfiguration var2 = this.loadCurrentConfig();
      int var3 = var2.getInt(this.versionPath, 0);
      int var4 = var1.getInt(this.versionPath, 0);
      if (var3 >= var4) {
         ConsoleLogger.info(this.plugin.getName(), "Config is up to date (v" + var3 + ")");
         return false;
      } else {
         ConsoleLogger.warn(this.plugin.getName(), "Config outdated: v" + var3 + " -> v" + var4);
         if (this.makeBackup) {
            this.backup(var3);
         }

         Map var5 = this.extractPreserved(var2, var1);
         this.overrideConfig();
         this.restoreValues(var5);
         ConsoleLogger.success(this.plugin.getName(), "Config migrated successfully to v" + var4);
         return true;
      }
   }

   public boolean needsMigration() {
      YamlConfiguration var1 = this.loadLatestConfig();
      YamlConfiguration var2 = this.loadCurrentConfig();
      int var3 = var2.getInt(this.versionPath, 0);
      int var4 = var1.getInt(this.versionPath, 0);
      return var3 < var4;
   }

   public int getCurrentVersion() {
      return this.loadCurrentConfig().getInt(this.versionPath, 0);
   }

   public int getLatestVersion() {
      return this.loadLatestConfig().getInt(this.versionPath, 0);
   }

   private void backup(int var1) {
      File var2 = new File(this.plugin.getDataFolder(), this.backupPath);
      if (!var2.exists() && !var2.mkdirs()) {
         ConsoleLogger.warn(this.plugin.getName(), "Failed to create backup directory");
      } else {
         try {
            File var3 = new File(var2, this.fileName.replace(".yml", "") + "-v" + var1 + ".yml");
            Files.copy(this.file.toPath(), var3.toPath(), StandardCopyOption.REPLACE_EXISTING);
            ConsoleLogger.info(this.plugin.getName(), "Backup created: " + var3.getName());
         } catch (IOException var4) {
            ConsoleLogger.error(this.plugin.getName(), "Failed to create backup: " + var4.getMessage());
         }
      }
   }

   @NotNull
   private Map<String, Object> extractPreserved(@NotNull YamlConfiguration var1, @NotNull YamlConfiguration var2) {
      HashMap var3 = new HashMap();

      for (String var5 : var2.getKeys(true)) {
         if (!var5.equals(this.versionPath) && var1.contains(var5) && !var2.isConfigurationSection(var5)) {
            var3.put(var5, var1.get(var5));
         }
      }

      return var3;
   }

   private void restoreValues(@NotNull Map<String, Object> var1) {
      try {
         YamlConfiguration var2 = YamlConfiguration.loadConfiguration(this.file);
         var1.forEach(var2::set);
         var2.save(this.file);
      } catch (IOException var3) {
         ConsoleLogger.error(this.plugin.getName(), "Failed to restore config values: " + var3.getMessage());
      }
   }

   private void overrideConfig() {
      try (InputStream var1 = this.getLatestStream()) {
         Files.copy(var1, this.file.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException var6) {
         ConsoleLogger.error(this.plugin.getName(), "Failed to override config: " + var6.getMessage());
      }
   }

   @NotNull
   private YamlConfiguration loadLatestConfig() {
      try {
         YamlConfiguration var2;
         try (InputStreamReader var1 = new InputStreamReader(this.getLatestStream())) {
            var2 = YamlConfiguration.loadConfiguration(var1);
         }

         return var2;
      } catch (IOException var6) {
         ConsoleLogger.error(this.plugin.getName(), "Failed to load latest config: " + var6.getMessage());
         return new YamlConfiguration();
      }
   }

   @NotNull
   private InputStream getLatestStream() {
      return this.plugin.getResource(this.fileName);
   }

   @NotNull
   private YamlConfiguration loadCurrentConfig() {
      if (!this.file.exists()) {
         this.plugin.saveResource(this.fileName, false);
      }

      return YamlConfiguration.loadConfiguration(this.file);
   }

   public static final class Builder {
      private final JavaPlugin plugin;
      private String fileName = "config.yml";
      private String versionPath = "config-version";
      private String backupPath = "backups";
      private boolean makeBackup = true;

      private Builder(@NotNull JavaPlugin var1) {
         this.plugin = var1;
      }

      @NotNull
      public ConfigMigrator.Builder fileName(@NotNull String var1) {
         this.fileName = var1;
         return this;
      }

      @NotNull
      public ConfigMigrator.Builder versionPath(@NotNull String var1) {
         this.versionPath = var1;
         return this;
      }

      @NotNull
      public ConfigMigrator.Builder backupPath(@NotNull String var1) {
         this.backupPath = var1;
         return this;
      }

      @NotNull
      public ConfigMigrator.Builder makeBackup(boolean var1) {
         this.makeBackup = var1;
         return this;
      }

      @NotNull
      public ConfigMigrator build() {
         return new ConfigMigrator(this.plugin, this.fileName, this.versionPath, this.backupPath, this.makeBackup);
      }
   }
}
