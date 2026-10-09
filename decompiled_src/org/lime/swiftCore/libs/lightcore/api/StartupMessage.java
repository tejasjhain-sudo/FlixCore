package org.lime.swiftCore.libs.lightcore.api;

import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class StartupMessage {
   public static void print(String var0, String var1) {
      Object var2 = translateHex(var1) + var0;
      Bukkit.getConsoleSender().sendMessage("");
      Bukkit.getConsoleSender().sendMessage(var2 + ChatColor.GRAY + " has been enabled!");
      Bukkit.getConsoleSender().sendMessage("");
   }

   public static void printWithAscii(String var0, String var1) {
      JavaPlugin var2 = getCallingPlugin();
      String var3 = null;
      String var4 = null;
      if (var2 != null) {
         var3 = var2.getDescription().getVersion();
         List var5 = var2.getDescription().getAuthors();
         var4 = var5.isEmpty() ? null : String.join(", ", var5);
      }

      printWithAscii(var0, var1, var3, var4);
   }

   public static void printWithAscii(String var0, String var1, String var2, String var3) {
      String var4 = translateHex(var1);
      String[] var5 = AsciiArt.generate(var0);
      Bukkit.getConsoleSender().sendMessage("");

      for (String var9 : var5) {
         Bukkit.getConsoleSender().sendMessage(var4 + var9);
      }

      Bukkit.getConsoleSender().sendMessage("");
      if (var2 != null || var3 != null) {
         Bukkit.getConsoleSender().sendMessage(var4 + "Plugin Information:");
         if (var2 != null) {
            Bukkit.getConsoleSender().sendMessage(var4 + " • " + ChatColor.WHITE + "Version: " + var4 + var2);
         }

         if (var3 != null) {
            Bukkit.getConsoleSender().sendMessage(var4 + " • " + ChatColor.WHITE + "Authors: " + var4 + var3);
         }

         Bukkit.getConsoleSender().sendMessage("");
         Bukkit.getConsoleSender().sendMessage(var4 + "Server Information:");
         Bukkit.getConsoleSender().sendMessage(var4 + " • " + ChatColor.WHITE + "Software: " + var4 + Bukkit.getName());
         Bukkit.getConsoleSender().sendMessage(var4 + " • " + ChatColor.WHITE + "Version: " + var4 + Bukkit.getVersion());
         Bukkit.getConsoleSender().sendMessage("");
      }
   }

   public static void printWithCustomAscii(String var0, String var1, String... var2) {
      String var3 = translateHex(var1);
      Bukkit.getConsoleSender().sendMessage("");

      for (String var7 : var2) {
         Bukkit.getConsoleSender().sendMessage(var3 + var7);
      }

      Bukkit.getConsoleSender().sendMessage("");
      Bukkit.getConsoleSender().sendMessage(var3 + var0 + ChatColor.GRAY + " has been enabled!");
      Bukkit.getConsoleSender().sendMessage("");
   }

   private static JavaPlugin getCallingPlugin() {
      try {
         StackTraceElement[] var0 = Thread.currentThread().getStackTrace();

         for (StackTraceElement var4 : var0) {
            String var5 = var4.getClassName();
            if (!var5.startsWith("org.lime.swiftCore.libs.lightcore.api.")) {
               try {
                  Class var6 = Class.forName(var5);
                  if (JavaPlugin.class.isAssignableFrom(var6)) {
                     return JavaPlugin.getProvidingPlugin(var6.asSubclass(JavaPlugin.class));
                  }
               } catch (ClassNotFoundException var7) {
               }
            }
         }
      } catch (Exception var8) {
      }

      return null;
   }

   private static String translateHex(String var0) {
      if (var0 != null && var0.startsWith("&#")) {
         var0 = var0.substring(2);
         return ChatColor.of("#" + var0).toString();
      } else {
         return ChatColor.WHITE.toString();
      }
   }
}
