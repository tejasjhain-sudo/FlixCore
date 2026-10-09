package org.lime.swiftCore.libs.lightcore.api.util;

import java.util.List;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lime.swiftCore.libs.lightcore.api.logging.ConsoleLogger;
import org.lime.swiftCore.libs.lightcore.api.messaging.Message;

public final class ActionUtil {
   private static boolean papiEnabled = false;

   private ActionUtil() {
   }

   public static void checkPAPI() {
      papiEnabled = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
   }

   @NotNull
   private static String parse(@Nullable OfflinePlayer var0, @Nullable String var1) {
      if (var1 == null || var1.isEmpty()) {
         return "";
      } else {
         return papiEnabled && var0 != null ? PlaceholderAPI.setPlaceholders(var0, var1) : var1;
      }
   }

   public static void execute(@NotNull Player var0, @NotNull List<String> var1) {
      if (!var1.isEmpty()) {
         for (String var3 : var1) {
            perform(var3, var0);
         }
      }
   }

   public static void perform(@Nullable String var0, @NotNull Player var1) {
      if (var0 != null && !var0.isEmpty()) {
         int var2 = var0.indexOf(" ");
         if (var2 != -1) {
            String var3 = var0.substring(0, var2).toLowerCase();
            String var4 = var0.substring(var2 + 1);
            switch (var3) {
               case "[playercommand]":
                  String var21 = parse(var1, var4.replace("<player>", var1.getName()));
                  if (var21.startsWith("/")) {
                     var21 = var21.substring(1);
                  }

                  String var26 = var21;
                  SchedulerUtil.sync(() -> var1.performCommand(var26));
                  break;
               case "[consolecommand]":
                  String var20 = parse(var1, var4.replace("<player>", var1.getName()));
                  if (var20.startsWith("/")) {
                     var20 = var20.substring(1);
                  }

                  String var25 = var20;
                  SchedulerUtil.sync(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), var25));
                  break;
               case "[consolecommandchance]":
                  String[] var19 = var4.split(";", 2);
                  if (var19.length != 2) {
                     return;
                  }

                  try {
                     double var24 = Double.parseDouble(var19[0].trim());
                     String var29 = parse(var1, var19[1].replace("<player>", var1.getName()));
                     if (var29.startsWith("/")) {
                        var29 = var29.substring(1);
                     }

                     if (MathUtil.chance(var24)) {
                        String var11 = var29;
                        SchedulerUtil.sync(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), var11));
                     }
                  } catch (NumberFormatException var13) {
                     ConsoleLogger.warn("Invalid chance format in action: " + var4);
                  }
                  break;
               case "[title]":
                  String[] var18 = var4.split(";", 2);
                  String var23 = parse(var1, var18[0].replace("<player>", var1.getName()));
                  String var28 = var18.length > 1 ? parse(var1, var18[1].replace("<player>", var1.getName())) : "";
                  Message.title(var1, var23, var28);
                  break;
               case "[subtitle]":
                  String var17 = parse(var1, var4.replace("<player>", var1.getName()));
                  Message.title(var1, "", var17);
                  break;
               case "[actionbar]":
                  String var16 = parse(var1, var4.replace("<player>", var1.getName()));
                  Message.actionbar(var1, var16);
                  break;
               case "[message]":
                  String var15 = parse(var1, var4.replace("<player>", var1.getName()));
                  Message.chat(var1, var15);
                  break;
               case "[broadcast]":
                  for (Player var22 : Bukkit.getOnlinePlayers()) {
                     String var27 = parse(var22, var4.replace("<player>", var22.getName()));
                     Message.chat(var22, var27);
                  }
                  break;
               case "[sound]":
                  try {
                     String[] var7 = var4.split(";");
                     Sound var8 = Sound.valueOf(var7[0].toUpperCase().trim());
                     float var9 = var7.length > 1 ? Float.parseFloat(var7[1].trim()) : 1.0F;
                     float var10 = var7.length > 2 ? Float.parseFloat(var7[2].trim()) : 1.0F;
                     Message.sound(var1, var8, var9, var10);
                  } catch (IllegalArgumentException var12) {
                     ConsoleLogger.warn("Invalid sound in action: " + var4);
                  }
                  break;
               default:
                  ConsoleLogger.warn("Unknown action type: " + var3);
            }
         }
      }
   }
}
