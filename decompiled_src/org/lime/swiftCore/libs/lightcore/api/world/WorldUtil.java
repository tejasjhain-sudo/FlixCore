package org.lime.swiftCore.libs.lightcore.api.world;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WorldUtil {
   private WorldUtil() {
   }

   public static boolean isNether(@Nullable World var0) {
      return var0 != null && var0.getEnvironment() == Environment.NETHER;
   }

   public static boolean isEnd(@Nullable World var0) {
      return var0 != null && var0.getEnvironment() == Environment.THE_END;
   }

   public static boolean isOverworld(@Nullable World var0) {
      return var0 != null && var0.getEnvironment() == Environment.NORMAL;
   }

   public static boolean isEnvironment(@Nullable World var0, @Nullable Environment var1) {
      return var0 != null && var1 != null && var0.getEnvironment() == var1;
   }

   public static boolean isSameWorld(@Nullable Location var0, @Nullable Location var1) {
      if (var0 != null && var1 != null) {
         World var2 = var0.getWorld();
         World var3 = var1.getWorld();
         return var2 != null && var2.equals(var3);
      } else {
         return false;
      }
   }

   public static boolean isLocationInWorld(@Nullable Location var0, @Nullable World var1) {
      if (var0 != null && var1 != null) {
         World var2 = var0.getWorld();
         return var2 != null && var2.equals(var1);
      } else {
         return false;
      }
   }

   @Nullable
   public static Location getSpawn(@Nullable World var0) {
      return var0 != null ? var0.getSpawnLocation() : null;
   }

   public static boolean isLoaded(@Nullable World var0) {
      return var0 != null && var0.isChunkLoaded(var0.getSpawnLocation().getChunk());
   }

   @NotNull
   public static String getName(@Nullable World var0) {
      return var0 != null ? var0.getName() : "unknown";
   }

   @NotNull
   public static String getType(@Nullable World var0) {
      if (var0 == null) {
         return "unknown";
      } else {
         return switch (var0.getEnvironment()) {
            case NETHER -> "nether";
            case THE_END -> "end";
            case NORMAL -> "overworld";
            default -> "custom";
         };
      }
   }
}
