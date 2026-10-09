package org.lime.swiftCore.libs.lightcore.api.world;

import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BlockUtil {
   private static final Set<Material> LIQUIDS = EnumSet.of(Material.WATER, Material.LAVA);
   private static final Set<Material> DANGEROUS = EnumSet.of(
      Material.LAVA,
      Material.FIRE,
      Material.CAMPFIRE,
      Material.SOUL_CAMPFIRE,
      Material.MAGMA_BLOCK,
      Material.CACTUS,
      Material.SWEET_BERRY_BUSH,
      Material.WITHER_ROSE
   );
   private static final Set<Material> CONTAINERS = EnumSet.of(
      Material.CHEST,
      Material.BARREL,
      Material.SHULKER_BOX,
      Material.HOPPER,
      Material.DROPPER,
      Material.DISPENSER,
      Material.FURNACE,
      Material.BLAST_FURNACE,
      Material.SMOKER
   );

   private BlockUtil() {
   }

   public static boolean isAir(@Nullable Block var0) {
      return var0 == null || var0.isEmpty() || var0.getType() == Material.AIR;
   }

   public static boolean isSolid(@Nullable Block var0) {
      if (var0 == null) {
         return false;
      } else {
         Material var1 = var0.getType();
         return var1.isSolid() && !LIQUIDS.contains(var1);
      }
   }

   public static boolean isLiquid(@Nullable Block var0) {
      return var0 != null && LIQUIDS.contains(var0.getType());
   }

   public static boolean isDangerous(@Nullable Block var0) {
      return var0 != null && DANGEROUS.contains(var0.getType());
   }

   public static boolean isSafe(@Nullable Block var0) {
      if (var0 == null) {
         return false;
      } else {
         Material var1 = var0.getType();
         return var1.isSolid() && !LIQUIDS.contains(var1) && !DANGEROUS.contains(var1);
      }
   }

   public static boolean isContainer(@Nullable Block var0) {
      return var0 != null && CONTAINERS.contains(var0.getType());
   }

   @NotNull
   public static String getTypeName(@Nullable Block var0) {
      return var0 != null ? var0.getType().name() : "UNKNOWN";
   }

   public static boolean isSameType(@Nullable Block var0, @Nullable Block var1) {
      return var0 != null && var1 != null && var0.getType() == var1.getType();
   }

   public static boolean isLiquid(@Nullable Material var0) {
      return var0 != null && LIQUIDS.contains(var0);
   }

   public static boolean isDangerous(@Nullable Material var0) {
      return var0 != null && DANGEROUS.contains(var0);
   }

   public static boolean isContainer(@Nullable Material var0) {
      return var0 != null && CONTAINERS.contains(var0);
   }
}
