package org.lime.swiftCore.libs.lightcore.api.util;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public final class SchedulerUtil {
   private static JavaPlugin plugin;

   private SchedulerUtil() {
   }

   public static void init(@NotNull JavaPlugin var0) {
      plugin = var0;
   }

   private static void checkInit() {
      if (plugin == null) {
         throw new IllegalStateException("SchedulerUtil not initialized! Call SchedulerUtil.init(plugin) first.");
      }
   }

   @NotNull
   public static BukkitTask sync(@NotNull Runnable var0) {
      checkInit();
      return Bukkit.getScheduler().runTask(plugin, var0);
   }

   @NotNull
   public static BukkitTask syncLater(@NotNull Runnable var0, long var1) {
      checkInit();
      return Bukkit.getScheduler().runTaskLater(plugin, var0, var1);
   }

   @NotNull
   public static BukkitTask syncRepeating(@NotNull Runnable var0, long var1, long var3) {
      checkInit();
      return Bukkit.getScheduler().runTaskTimer(plugin, var0, var1, var3);
   }

   @NotNull
   public static BukkitTask async(@NotNull Runnable var0) {
      checkInit();
      return Bukkit.getScheduler().runTaskAsynchronously(plugin, var0);
   }

   @NotNull
   public static BukkitTask asyncLater(@NotNull Runnable var0, long var1) {
      checkInit();
      return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, var0, var1);
   }

   @NotNull
   public static BukkitTask asyncRepeating(@NotNull Runnable var0, long var1, long var3) {
      checkInit();
      return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, var0, var1, var3);
   }

   public static void cancel(@NotNull BukkitTask var0) {
      var0.cancel();
   }

   public static void cancel(int var0) {
      Bukkit.getScheduler().cancelTask(var0);
   }

   public static void cancelAll() {
      checkInit();
      Bukkit.getScheduler().cancelTasks(plugin);
   }
}
