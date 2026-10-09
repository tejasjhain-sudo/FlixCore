package org.lime.swiftCore.libs.lightcore.api.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CommandHelper {
   private static final Map<String, Supplier<Collection<String>>> COMPLETIONS = new HashMap<>();

   private CommandHelper() {
   }

   public static void registerCompletion(@NotNull String var0, @NotNull Supplier<Collection<String>> var1) {
      COMPLETIONS.put(var0.toLowerCase(), var1);
   }

   @NotNull
   public static Collection<String> getCompletion(@NotNull String var0) {
      Supplier var1 = COMPLETIONS.get(var0.toLowerCase());
      return (Collection<String>)(var1 != null ? (Collection)var1.get() : Collections.emptyList());
   }

   public static void unregisterCompletion(@NotNull String var0) {
      COMPLETIONS.remove(var0.toLowerCase());
   }

   public static void clearCompletions() {
      COMPLETIONS.clear();
   }

   @NotNull
   public static List<String> getOnlinePlayers(@Nullable Player var0) {
      return Bukkit.getOnlinePlayers().stream().filter(var1 -> var0 == null || var0.canSee(var1)).<String>map(Player::getName).collect(Collectors.toList());
   }

   @NotNull
   public static List<String> getOnlinePlayers() {
      return getOnlinePlayers(null);
   }

   @NotNull
   public static List<String> getWorlds() {
      return Bukkit.getWorlds().stream().<String>map(WorldInfo::getName).collect(Collectors.toList());
   }

   @NotNull
   public static List<String> getBooleans() {
      return Arrays.asList("true", "false");
   }

   @NotNull
   public static List<String> getNumbers(int var0, int var1) {
      ArrayList var2 = new ArrayList();

      for (int var3 = var0; var3 <= var1; var3++) {
         var2.add(String.valueOf(var3));
      }

      return var2;
   }

   @NotNull
   public static List<String> filter(@NotNull Collection<String> var0, @Nullable String var1) {
      if (var1 != null && !var1.isEmpty()) {
         String var2 = var1.toLowerCase();
         return var0.stream().filter(var1x -> var1x.toLowerCase().startsWith(var2)).collect(Collectors.toList());
      } else {
         return new ArrayList<>(var0);
      }
   }

   @NotNull
   public static List<String> filterPlayers(@Nullable Player var0, @Nullable String var1) {
      return filter(getOnlinePlayers(var0), var1);
   }

   @NotNull
   public static List<String> filterWorlds(@Nullable String var0) {
      return filter(getWorlds(), var0);
   }

   public static boolean isPlayer(@NotNull CommandSender var0) {
      return var0 instanceof Player;
   }

   @Nullable
   public static Player asPlayer(@NotNull CommandSender var0) {
      return var0 instanceof Player ? (Player)var0 : null;
   }

   public static boolean hasPermission(@NotNull CommandSender var0, @NotNull String var1) {
      return var0.hasPermission(var1);
   }

   @NotNull
   public static TabCompleter createTabCompleter(@NotNull Map<Integer, Supplier<List<String>>> var0) {
      return (var1, var2, var3, var4) -> {
         int var5 = var4.length - 1;
         Supplier var6 = (Supplier)var0.get(var5);
         return var6 == null ? Collections.emptyList() : filter((Collection<String>)var6.get(), var4[var5]);
      };
   }

   @NotNull
   public static TabCompleter playerTabCompleter() {
      return (var0, var1, var2, var3) -> {
         if (var3.length == 1) {
            Player var4 = asPlayer(var0);
            return filterPlayers(var4, var3[0]);
         } else {
            return Collections.emptyList();
         }
      };
   }
}
