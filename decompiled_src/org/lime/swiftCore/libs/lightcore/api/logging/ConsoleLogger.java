package org.lime.swiftCore.libs.lightcore.api.logging;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

public final class ConsoleLogger {
   private ConsoleLogger() {
   }

   public static void info(@NotNull String var0) {
      log(var0, ConsoleLogger.LogLevel.INFO);
   }

   public static void warn(@NotNull String var0) {
      log(var0, ConsoleLogger.LogLevel.WARN);
   }

   public static void error(@NotNull String var0) {
      log(var0, ConsoleLogger.LogLevel.ERROR);
   }

   public static void debug(@NotNull String var0) {
      log(var0, ConsoleLogger.LogLevel.DEBUG);
   }

   public static void fatal(@NotNull String var0) {
      log(var0, ConsoleLogger.LogLevel.FATAL);
   }

   public static void success(@NotNull String var0) {
      log(var0, ConsoleLogger.LogLevel.SUCCESS);
   }

   public static void log(@NotNull String var0, @NotNull ConsoleLogger.LogLevel var1) {
      ConsoleCommandSender var2 = Bukkit.getConsoleSender();
      var2.sendMessage(format(var0, var1));
   }

   public static void log(@NotNull String var0, @NotNull String var1, @NotNull ConsoleLogger.LogLevel var2) {
      ConsoleCommandSender var3 = Bukkit.getConsoleSender();
      var3.sendMessage("§7[§f" + var0 + "§7] " + format(var1, var2));
   }

   public static void info(@NotNull String var0, @NotNull String var1) {
      log(var0, var1, ConsoleLogger.LogLevel.INFO);
   }

   public static void warn(@NotNull String var0, @NotNull String var1) {
      log(var0, var1, ConsoleLogger.LogLevel.WARN);
   }

   public static void error(@NotNull String var0, @NotNull String var1) {
      log(var0, var1, ConsoleLogger.LogLevel.ERROR);
   }

   public static void debug(@NotNull String var0, @NotNull String var1) {
      log(var0, var1, ConsoleLogger.LogLevel.DEBUG);
   }

   public static void fatal(@NotNull String var0, @NotNull String var1) {
      log(var0, var1, ConsoleLogger.LogLevel.FATAL);
   }

   public static void success(@NotNull String var0, @NotNull String var1) {
      log(var0, var1, ConsoleLogger.LogLevel.SUCCESS);
   }

   @NotNull
   private static String format(@NotNull String var0, @NotNull ConsoleLogger.LogLevel var1) {
      return var1.getColorCode() + var0;
   }

   public static enum LogLevel {
      INFO("§x§F§F§F§B§C§9"),
      WARN("§x§F§F§E§D§0§0"),
      ERROR("§x§F§F§5§7§5§7"),
      DEBUG("§x§8§A§F§F§0§0"),
      FATAL("§x§F§F§0§0§0§0"),
      SUCCESS("§x§0§0§F§F§0§0");

      private final String colorCode;

      private LogLevel(@NotNull String var3) {
         this.colorCode = var3;
      }

      @NotNull
      public String getColorCode() {
         return this.colorCode;
      }
   }
}
