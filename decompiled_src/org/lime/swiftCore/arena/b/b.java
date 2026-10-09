package org.lime.swiftCore.arena.b;

import org.bukkit.Location;

public class b {
   public static Location Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
      Location var0, Location var1
   ) {
      int var2 = var0.getBlockX();
      int var3 = var0.getBlockY();
      int var4 = var0.getBlockZ();
      int var5 = var1.getBlockX();
      int var6 = var1.getBlockY();
      int var7 = var1.getBlockZ();
      int var8 = Math.max(var2, var5);
      int var9 = Math.max(var3, var6);
      int var10 = Math.max(var4, var7);
      return new Location(var0.getWorld(), (double)var8, (double)var9, (double)var10);
   }

   public static Location o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
      Location var0, Location var1
   ) {
      int var2 = var0.getBlockX();
      int var3 = var0.getBlockY();
      int var4 = var0.getBlockZ();
      int var5 = var1.getBlockX();
      int var6 = var1.getBlockY();
      int var7 = var1.getBlockZ();
      int var8 = Math.min(var2, var5);
      int var9 = Math.min(var3, var6);
      int var10 = Math.min(var4, var7);
      return new Location(var0.getWorld(), (double)var8, (double)var9, (double)var10);
   }
}
