package org.lime.swiftCore.libs.lightcore.api.messaging;

import net.kyori.adventure.bossbar.BossBar.Color;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Message {
   private Message() {
   }

   public static void chat(@NotNull CommandSender var0, @NotNull String var1) {
      MessageBuilder.create().to(var0).message(var1).sendChat();
   }

   public static void chat(@NotNull CommandSender var0, @NotNull String var1, String... var2) {
      MessageBuilder.create().to(var0).message(var1).placeholders(var2).sendChat();
   }

   public static void actionbar(@NotNull Player var0, @NotNull String var1) {
      MessageBuilder.create().to(var0).message(var1).sendActionbar();
   }

   public static void actionbar(@NotNull Player var0, @NotNull String var1, String... var2) {
      MessageBuilder.create().to(var0).message(var1).placeholders(var2).sendActionbar();
   }

   public static void title(@NotNull Player var0, @NotNull String var1, String var2) {
      MessageBuilder.create().to(var0).title(var1, var2).sendTitle();
   }

   public static void title(@NotNull Player var0, @NotNull String var1, String var2, int var3, int var4, int var5) {
      MessageBuilder.create().to(var0).title(var1, var2).titleTimes(var3, var4, var5).sendTitle();
   }

   public static void bossbar(@NotNull Player var0, @NotNull JavaPlugin var1, @NotNull String var2, @NotNull Color var3, int var4) {
      MessageBuilder.create().to(var0).plugin(var1).bossbar(var2).bossbarColor(var3).bossbarDuration(var4).sendBossbar();
   }

   public static void bossbar(@NotNull Player var0, @NotNull JavaPlugin var1, @NotNull String var2, float var3, @NotNull Color var4, int var5) {
      MessageBuilder.create().to(var0).plugin(var1).bossbar(var2).bossbarProgress(var3).bossbarColor(var4).bossbarDuration(var5).sendBossbar();
   }

   public static void sound(@NotNull Player var0, @NotNull Sound var1) {
      MessageBuilder.create().to(var0).sound(var1).playSound();
   }

   public static void sound(@NotNull Player var0, @NotNull Sound var1, float var2, float var3) {
      MessageBuilder.create().to(var0).sound(var1).volume(var2).pitch(var3).playSound();
   }
}
