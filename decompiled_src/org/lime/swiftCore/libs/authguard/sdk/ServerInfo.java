package org.lime.swiftCore.libs.authguard.sdk;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerInfo {
   private final String serverIp;
   private final int serverPort;
   private final String serverVersion;
   private final String pluginVersion;
   private final String hwid;
   private final String macAddress;
   private final String operatingSystem;
   private final String osVersion;
   private final String osArch;
   private final String javaVersion;
   private static final ConcurrentHashMap<String, String> hwidCache = new ConcurrentHashMap<>();

   private ServerInfo(String var1, int var2, String var3, String var4, String var5, String var6, String var7, String var8, String var9, String var10) {
      this.serverIp = var1;
      this.serverPort = var2;
      this.serverVersion = var3;
      this.pluginVersion = var4;
      this.hwid = var5;
      this.macAddress = var6;
      this.operatingSystem = var7;
      this.osVersion = var8;
      this.osArch = var9;
      this.javaVersion = var10;
   }

   public static ServerInfo collect(JavaPlugin var0) {
      String var1 = resolveServerIp();
      int var2 = Bukkit.getPort();
      String var3 = Bukkit.getVersion();
      String var4 = var0.getDescription().getVersion();
      String var5 = resolveMacAddress();
      String var6 = generateHwid(var1, var2);
      String var7 = System.getProperty("os.name");
      String var8 = System.getProperty("os.version");
      String var9 = System.getProperty("os.arch");
      String var10 = System.getProperty("java.version");
      return new ServerInfo(var1, var2, var3, var4, var6, var5, var7, var8, var9, var10);
   }

   public String toQueryString(String var1, String var2) {
      StringBuilder var3 = new StringBuilder();
      var3.append("key=").append(encode(var1));
      var3.append("&product=").append(encode(var2));
      var3.append("&ip=").append(encode(this.serverIp + ":" + this.serverPort));
      var3.append("&version=").append(encode(this.pluginVersion));
      var3.append("&hwid=").append(encode(this.hwid));
      var3.append("&mac=").append(encode(this.macAddress));
      var3.append("&os=").append(encode(this.operatingSystem));
      var3.append("&osVersion=").append(encode(this.osVersion));
      var3.append("&osArch=").append(encode(this.osArch));
      var3.append("&javaVersion=").append(encode(this.javaVersion));
      var3.append("&serverVersion=").append(encode(this.serverVersion));
      var3.append("&serverPort=").append(this.serverPort);
      return var3.toString();
   }

   private static String encode(String var0) {
      return var0 == null ? "" : URLEncoder.encode(var0, StandardCharsets.UTF_8);
   }

   private static String resolveServerIp() {
      try {
         String var0 = Bukkit.getIp();
         return var0 != null && !var0.isEmpty() && !var0.equals("0.0.0.0") ? var0 : InetAddress.getLocalHost().getHostAddress();
      } catch (Exception var1) {
         return "unknown";
      }
   }

   private static String resolveMacAddress() {
      try {
         ArrayList var0 = new ArrayList();
         Enumeration var1 = NetworkInterface.getNetworkInterfaces();

         while (var1.hasMoreElements()) {
            NetworkInterface var2 = (NetworkInterface)var1.nextElement();
            if (!var2.isLoopback() && !var2.isVirtual() && var2.isUp()) {
               byte[] var3 = var2.getHardwareAddress();
               if (var3 != null && var3.length > 0) {
                  StringBuilder var4 = new StringBuilder();

                  for (int var5 = 0; var5 < var3.length; var5++) {
                     var4.append(String.format("%02X", var3[var5]));
                     if (var5 < var3.length - 1) {
                        var4.append(":");
                     }
                  }

                  var0.add(var4.toString());
               }
            }
         }

         if (!var0.isEmpty()) {
            Collections.sort(var0);
            return (String)var0.get(0);
         }
      } catch (Exception var6) {
      }

      return "unknown";
   }

   private static String resolveAllMacs() {
      try {
         ArrayList var0 = new ArrayList();
         Enumeration var1 = NetworkInterface.getNetworkInterfaces();

         while (var1.hasMoreElements()) {
            NetworkInterface var2 = (NetworkInterface)var1.nextElement();
            if (!var2.isLoopback() && !var2.isVirtual()) {
               byte[] var3 = var2.getHardwareAddress();
               if (var3 != null && var3.length > 0) {
                  StringBuilder var4 = new StringBuilder();

                  for (int var5 = 0; var5 < var3.length; var5++) {
                     var4.append(String.format("%02X", var3[var5]));
                     if (var5 < var3.length - 1) {
                        var4.append(":");
                     }
                  }

                  var0.add(var4.toString());
               }
            }
         }

         if (!var0.isEmpty()) {
            Collections.sort(var0);
            return String.join("+", var0);
         }
      } catch (Exception var6) {
      }

      return "unknown";
   }

   private static String generateHwid(String var0, int var1) {
      String var2 = (var0 != null ? var0 : "unknown") + ":" + var1;
      return hwidCache.computeIfAbsent(var2, var0x -> {
         try {
            MessageDigest var1x = MessageDigest.getInstance("SHA-256");
            byte[] var2x = var1x.digest(var0x.getBytes(StandardCharsets.UTF_8));
            StringBuilder var3 = new StringBuilder();

            for (int var4 = 0; var4 < 8 && var4 < var2x.length; var4++) {
               var3.append(String.format("%02x", var2x[var4]));
            }

            return var3.toString();
         } catch (Exception var5) {
            return "unknown";
         }
      });
   }

   public String getServerIp() {
      return this.serverIp;
   }

   public int getServerPort() {
      return this.serverPort;
   }

   public String getServerVersion() {
      return this.serverVersion;
   }

   public String getPluginVersion() {
      return this.pluginVersion;
   }

   public String getHwid() {
      return this.hwid;
   }

   public String getMacAddress() {
      return this.macAddress;
   }

   public String getOperatingSystem() {
      return this.operatingSystem;
   }

   public String getOsVersion() {
      return this.osVersion;
   }

   public String getOsArch() {
      return this.osArch;
   }

   public String getJavaVersion() {
      return this.javaVersion;
   }
}
