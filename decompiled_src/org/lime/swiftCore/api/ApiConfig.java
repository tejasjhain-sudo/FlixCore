package org.lime.swiftCore.api;

import org.bukkit.configuration.file.FileConfiguration;

public class ApiConfig {
   private final boolean enabled;
   private final int port;
   private final String apiKey;
   private final int rateLimit;
   private final String corsOrigin;

   public ApiConfig(FileConfiguration var1) {
      this.enabled = var1.getBoolean("api.enabled", false);
      this.port = var1.getInt("api.port", 8080);
      this.apiKey = var1.getString("api.api-key", "");
      this.rateLimit = var1.getInt("api.rate-limit", 60);
      this.corsOrigin = var1.getString("api.cors-origin", "*");
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public int getPort() {
      return this.port;
   }

   public String getApiKey() {
      return this.apiKey;
   }

   public int getRateLimit() {
      return this.rateLimit;
   }

   public String getCorsOrigin() {
      return this.corsOrigin;
   }
}
