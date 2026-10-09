package org.lime.swiftCore.libs.lightcore.api.util;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public final class MathUtil {
   private static final ThreadLocalRandom RNG = ThreadLocalRandom.current();

   private MathUtil() {
   }

   public static int randomInt(int var0, int var1) {
      return RNG.nextInt(var0, var1 + 1);
   }

   public static double randomDouble(double var0, double var2) {
      return RNG.nextDouble(var0, var2);
   }

   public static boolean chance(double var0) {
      return RNG.nextDouble(100.0) < var0;
   }

   public static double pow(double var0, int var2) {
      return Math.pow(var0, (double)var2);
   }

   public static int clamp(int var0, int var1, int var2) {
      return Math.max(var1, Math.min(var2, var0));
   }

   public static double clamp(double var0, double var2, double var4) {
      return Math.max(var2, Math.min(var4, var0));
   }

   public static double map(double var0, double var2, double var4, double var6, double var8) {
      return var4 == var2 ? var6 : (var0 - var2) * (var8 - var6) / (var4 - var2) + var6;
   }

   public static double distance2D(@NotNull Location var0, @NotNull Location var1) {
      if (!sameWorld(var0, var1)) {
         return -1.0;
      } else {
         double var2 = var0.getX() - var1.getX();
         double var4 = var0.getZ() - var1.getZ();
         return Math.sqrt(var2 * var2 + var4 * var4);
      }
   }

   public static double distance3D(@NotNull Location var0, @NotNull Location var1) {
      return !sameWorld(var0, var1) ? -1.0 : var0.distance(var1);
   }

   public static double distanceSquared2D(@NotNull Location var0, @NotNull Location var1) {
      double var2 = var0.getX() - var1.getX();
      double var4 = var0.getZ() - var1.getZ();
      return var2 * var2 + var4 * var4;
   }

   public static double distanceSquared3D(@NotNull Location var0, @NotNull Location var1) {
      double var2 = var0.getX() - var1.getX();
      double var4 = var0.getY() - var1.getY();
      double var6 = var0.getZ() - var1.getZ();
      return var2 * var2 + var4 * var4 + var6 * var6;
   }

   @NotNull
   public static Vector direction(@NotNull Location var0, @NotNull Location var1) {
      return var1.toVector().subtract(var0.toVector()).normalize();
   }

   @NotNull
   public static Vector randomDirection() {
      double var0 = RNG.nextDouble(0.0, 360.0);
      double var2 = RNG.nextDouble(-90.0, 90.0);
      double var4 = -Math.sin(Math.toRadians(var0)) * Math.cos(Math.toRadians(var2));
      double var6 = -Math.sin(Math.toRadians(var2));
      double var8 = Math.cos(Math.toRadians(var0)) * Math.cos(Math.toRadians(var2));
      return new Vector(var4, var6, var8).normalize();
   }

   @NotNull
   public static Location offset(@NotNull Location var0, double var1, double var3, double var5) {
      return var0.clone().add(var1, var3, var5);
   }

   @NotNull
   public static Location randomOffset(@NotNull Location var0, double var1) {
      double var3 = RNG.nextDouble(0.0, Math.PI * 2);
      double var5 = Math.cos(var3) * var1;
      double var7 = Math.sin(var3) * var1;
      return var0.clone().add(var5, 0.0, var7);
   }

   public static boolean within(double var0, double var2, double var4) {
      return var0 >= var2 && var0 <= var4;
   }

   public static boolean isSameBlock(@NotNull Location var0, @NotNull Location var1) {
      return sameWorld(var0, var1) && var0.getBlockX() == var1.getBlockX() && var0.getBlockY() == var1.getBlockY() && var0.getBlockZ() == var1.getBlockZ();
   }

   @NotNull
   public static Location centerBlock(@NotNull Location var0) {
      return new Location(var0.getWorld(), (double)var0.getBlockX() + 0.5, var0.getY(), (double)var0.getBlockZ() + 0.5);
   }

   public static int chunkX(double var0) {
      return (int)Math.floor(var0) >> 4;
   }

   public static int chunkZ(double var0) {
      return (int)Math.floor(var0) >> 4;
   }

   @NotNull
   public static Location centerOfChunk(@NotNull World var0, int var1, int var2) {
      int var3 = (var1 << 4) + 8;
      int var4 = (var2 << 4) + 8;
      return new Location(var0, (double)var3, (double)var0.getHighestBlockYAt(var3, var4), (double)var4);
   }

   @NotNull
   public static Vector chunkToWorld(int var0, int var1) {
      return new Vector(var0 << 4, 0, var1 << 4);
   }

   public static boolean inSameChunk(@NotNull Location var0, @NotNull Location var1) {
      return sameWorld(var0, var1) && var0.getBlockX() >> 4 == var1.getBlockX() >> 4 && var0.getBlockZ() >> 4 == var1.getBlockZ() >> 4;
   }

   public static boolean isInWorldBorder(@NotNull World var0, double var1, double var3) {
      WorldBorder var5 = var0.getWorldBorder();
      Location var6 = var5.getCenter();
      double var7 = var5.getSize() / 2.0;
      return Math.abs(var1 - var6.getX()) <= var7 && Math.abs(var3 - var6.getZ()) <= var7;
   }

   private static boolean sameWorld(@NotNull Location var0, @NotNull Location var1) {
      World var2 = var0.getWorld();
      World var3 = var1.getWorld();
      return var2 != null && var2.equals(var3);
   }
}
