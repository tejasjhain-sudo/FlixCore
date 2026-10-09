package org.lime.swiftCore.libs.authguard.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AuthGuard {
   private static final Gson GSON = new Gson();
   private static final String[] BANNER = new String[]{
      "",
      "§b █████╗ ██╗   ██╗████████╗██╗  ██╗ §3 ██████╗ ██╗   ██╗ █████╗ ██████╗ ██████╗ ",
      "§b██╔══██╗██║   ██║╚══██╔══╝██║  ██║ §3██╔════╝ ██║   ██║██╔══██╗██╔══██╗██╔══██╗",
      "§b███████║██║   ██║   ██║   ███████║ §3██║  ███╗██║   ██║███████║██████╔╝██║  ██║",
      "§b██╔══██║██║   ██║   ██║   ██╔══██║ §3██║   ██║██║   ██║██╔══██║██╔══██╗██║  ██║",
      "§b██║  ██║╚██████╔╝   ██║   ██║  ██║ §3╚██████╔╝╚██████╔╝██║  ██║██║  ██║██████╔╝",
      "§b╚═╝  ╚═╝ ╚═════╝    ╚═╝   ╚═╝  ╚═╝ §3 ╚═════╝  ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═════╝ ",
      ""
   };

   private AuthGuard() {
   }

   public static VerificationResult verify(JavaPlugin var0, String var1, String var2, String var3, String var4) {
      Logger var5 = var0.getLogger();

      for (String var9 : BANNER) {
         Bukkit.getConsoleSender().sendMessage(var9);
      }

      Bukkit.getConsoleSender().sendMessage("§8§m                                                                          ");
      Bukkit.getConsoleSender().sendMessage("§b  Product: §f" + var3 + " §8| §bLicense Verification");
      Bukkit.getConsoleSender().sendMessage("§8§m                                                                          ");
      ServerInfo var11 = ServerInfo.collect(var0);
      Bukkit.getConsoleSender().sendMessage("§7  Server: §f" + var11.getServerVersion());
      Bukkit.getConsoleSender().sendMessage("§7  Address: §f" + var11.getServerIp() + ":" + var11.getServerPort());
      Bukkit.getConsoleSender().sendMessage("§7  Plugin Version: §f" + var11.getPluginVersion());
      Bukkit.getConsoleSender().sendMessage("§7  Java: §f" + var11.getJavaVersion() + " §8| §7OS: §f" + var11.getOperatingSystem());
      Bukkit.getConsoleSender().sendMessage("");
      Bukkit.getConsoleSender().sendMessage("§e  ⏳ Verifying license...");

      try {
         VerificationResult var12 = sendVerificationRequest(var4, var1, var2, var11);
         if (var12.isValid()) {
            Bukkit.getConsoleSender().sendMessage("");
            Bukkit.getConsoleSender().sendMessage("§a  ✅ License verified successfully!");
            Bukkit.getConsoleSender().sendMessage("§a  Product: §f" + var3);
            if (var12.getDiscordUsername() != null) {
               Bukkit.getConsoleSender().sendMessage("§a  Licensed to: §f" + var12.getDiscordUsername());
            }

            if (var12.getExpiresAt() != null) {
               Bukkit.getConsoleSender().sendMessage("§a  Expires: §f" + var12.getExpiresAt());
            }

            if (var12.getIpUsage() != null) {
               Bukkit.getConsoleSender().sendMessage("§a  IP Usage: §f" + var12.getIpUsage());
            }

            if (var12.getHwidUsage() != null) {
               Bukkit.getConsoleSender().sendMessage("§a  HWID Usage: §f" + var12.getHwidUsage());
            }

            Bukkit.getConsoleSender().sendMessage("§8§m                                                                          ");
            Bukkit.getConsoleSender().sendMessage("");
            return var12;
         } else {
            printFailure(var0, var3, var12.getMessage());
            return var12;
         }
      } catch (Exception var10) {
         var5.severe("License verification error: " + var10.getMessage());
         VerificationResult var13 = new VerificationResult(false, "Connection error: " + var10.getMessage());
         printFailure(var0, var3, var13.getMessage());
         return var13;
      }
   }

   public static VerificationResult verifyAndShutdown(JavaPlugin var0, String var1, String var2, String var3, String var4) {
      VerificationResult var5 = verify(var0, var1, var2, var3, var4);
      if (!var5.isValid()) {
         Bukkit.getScheduler().runTask(var0, () -> Bukkit.getPluginManager().disablePlugin(var0));
      }

      return var5;
   }

   private static void printFailure(JavaPlugin var0, String var1, String var2) {
      Bukkit.getConsoleSender().sendMessage("");
      Bukkit.getConsoleSender().sendMessage("§c  ❌ License verification failed!");
      Bukkit.getConsoleSender().sendMessage("§c  Product: §f" + var1);
      Bukkit.getConsoleSender().sendMessage("§c  Reason: §f" + var2);
      Bukkit.getConsoleSender().sendMessage("§8§m                                                                          ");
      Bukkit.getConsoleSender().sendMessage("§c  Please check your license key or contact support.");
      Bukkit.getConsoleSender().sendMessage("§8§m                                                                          ");
      Bukkit.getConsoleSender().sendMessage("");
   }

   private static VerificationResult sendVerificationRequest(String var0, String var1, String var2, ServerInfo var3) {
      HttpURLConnection var4 = null;

      VerificationResult var12;
      try {
         String var5 = var0.endsWith("/") ? var0.substring(0, var0.length() - 1) : var0;
         String var6 = var3.toQueryString(var1, var2);
         String var7 = generateNonce();
         String var8 = var6 + "&nonce=" + URLEncoder.encode(var7, StandardCharsets.UTF_8);
         URL var9 = new URL(var5 + "/api/v1/verify?" + var8);
         var4 = (HttpURLConnection)var9.openConnection();
         var4.setRequestMethod("GET");
         var4.setRequestProperty("User-Agent", "AuthGuard-SDK/1.0.0");
         var4.setRequestProperty("Accept", "application/json");
         var4.setConnectTimeout(10000);
         var4.setReadTimeout(10000);
         int var10 = var4.getResponseCode();
         String var11;
         if (var10 >= 200 && var10 < 300) {
            var11 = readStream(var4);
         } else {
            var11 = readErrorStream(var4);
         }

         var12 = parseResponse(var11, var7, var5);
      } catch (Exception var16) {
         throw new AuthGuardException("Failed to connect to auth server: " + var16.getMessage(), var16);
      } finally {
         if (var4 != null) {
            var4.disconnect();
         }
      }

      return var12;
   }

   private static String readStream(HttpURLConnection var0) throws Exception {
      String var4;
      try (BufferedReader var1 = new BufferedReader(new InputStreamReader(var0.getInputStream(), StandardCharsets.UTF_8))) {
         StringBuilder var2 = new StringBuilder();

         String var3;
         while ((var3 = var1.readLine()) != null) {
            var2.append(var3);
         }

         var4 = var2.toString();
      }

      return var4;
   }

   private static String readErrorStream(HttpURLConnection var0) {
      try {
         if (var0.getErrorStream() != null) {
            String var4;
            try (BufferedReader var1 = new BufferedReader(new InputStreamReader(var0.getErrorStream(), StandardCharsets.UTF_8))) {
               StringBuilder var2 = new StringBuilder();

               String var3;
               while ((var3 = var1.readLine()) != null) {
                  var2.append(var3);
               }

               var4 = var2.toString();
            }

            return var4;
         }
      } catch (Exception var7) {
      }

      return "{\"valid\":false,\"message\":\"HTTP " + getResponseCodeSafe(var0) + "\"}";
   }

   private static int getResponseCodeSafe(HttpURLConnection var0) {
      try {
         return var0.getResponseCode();
      } catch (Exception var2) {
         return 0;
      }
   }

   private static VerificationResult parseResponse(String var0, String var1, String var2) {
      try {
         JsonObject var3 = (JsonObject)GSON.fromJson(var0, JsonObject.class);
         boolean var4 = var3.has("valid") && var3.get("valid").getAsBoolean();
         String var5 = var3.has("message") ? var3.get("message").getAsString() : "Unknown";
         boolean var6 = false;
         if (var4) {
            SignatureResult var7 = SignatureVerifier.verify(var3, var1, var2, 0L);
            if (var7.isServerSupportsSignature()) {
               if (!var7.isVerified()) {
                  return new VerificationResult(false, "Response signature verification failed: " + var7.getFailureReason());
               }

               var6 = true;
            }
         }

         if (var3.has("license")) {
            JsonObject var16 = var3.getAsJsonObject("license");
            String var17 = getJsonString(var16, "productId");
            String var18 = getJsonString(var16, "expiresAt");
            if (var18 == null) {
               var18 = "Lifetime";
            }

            String var19 = getJsonString(var16, "discordUsername");
            String var11 = getJsonString(var16, "ipUsage");
            if (var11 == null && var16.has("activeIps") && var16.has("ipLimit")) {
               int var12 = 0;
               if (var16.get("activeIps").isJsonArray()) {
                  var12 = var16.getAsJsonArray("activeIps").size();
               }

               int var13 = var16.get("ipLimit").getAsInt();
               var11 = var12 + "/" + var13;
            }

            String var20 = getJsonString(var16, "hwidUsage");
            return new VerificationResult(var4, var5, var17, var18, var11, var20, var19, var6);
         } else {
            String var15 = getJsonString(var3, "product_id");
            String var8 = getJsonString(var3, "expires_at");
            String var9 = getJsonString(var3, "ip_usage");
            String var10 = getJsonString(var3, "hwid_usage");
            return new VerificationResult(var4, var5, var15, var8, var9, var10, null, var6);
         }
      } catch (Exception var14) {
         return new VerificationResult(false, "Failed to parse server response");
      }
   }

   private static String getJsonString(JsonObject var0, String var1) {
      return var0.has(var1) && !var0.get(var1).isJsonNull() ? var0.get(var1).getAsString() : null;
   }

   private static String generateNonce() {
      byte[] var0 = new byte[32];
      new SecureRandom().nextBytes(var0);
      return Base64.getUrlEncoder().withoutPadding().encodeToString(var0);
   }
}
