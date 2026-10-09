package org.lime.swiftCore.libs.lightcore.api.world;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChunkUtil {
   private static final Set<Material> UNSAFE_BLOCKS = Set.of(
      Material.LAVA,
      Material.WATER,
      Material.CACTUS,
      Material.CAMPFIRE,
      Material.FIRE,
      Material.MAGMA_BLOCK,
      Material.SOUL_CAMPFIRE,
      Material.SOUL_FIRE,
      Material.SWEET_BERRY_BUSH,
      Material.WITHER_ROSE,
      Material.END_PORTAL,
      Material.NETHER_PORTAL
   );

   private ChunkUtil() {
   }

   @Nullable
   public static ChunkSnapshot snapshot(@Nullable Chunk var0) {
      return var0 == null ? null : var0.getChunkSnapshot(true, false, false);
   }

   @NotNull
   public static CompletableFuture<Chunk> getChunkAsync(@Nullable World var0, int var1, int var2) {
      return var0 == null ? CompletableFuture.completedFuture(null) : var0.getChunkAtAsync(var1, var2, true);
   }

   @NotNull
   public static CompletableFuture<ChunkSnapshot> getSnapshotAsync(@Nullable World var0, int var1, int var2) {
      return getChunkAsync(var0, var1, var2).thenApplyAsync(var0x -> var0x == null ? null : var0x.getChunkSnapshot(true, false, false));
   }

   public static boolean isSafe(@Nullable ChunkSnapshot var0, int var1, int var2, int var3, int var4, int var5) {
      if (var0 != null && var2 > var4 && var2 < var5 - 1) {
         Material var6 = var0.getBlockType(var1, var2 - 1, var3);
         return var6.isSolid() && !UNSAFE_BLOCKS.contains(var6)
            ? var0.getBlockType(var1, var2, var3).isAir() && var0.getBlockType(var1, var2 + 1, var3).isAir()
            : false;
      } else {
         return false;
      }
   }

   public static int getSafeYOverworld(@NotNull ChunkSnapshot var0, int var1, int var2, int var3, int var4) {
      int var5 = var0.getHighestBlockYAt(var1, var2) + 1;
      return isSafe(var0, var1, var5, var2, var3, var4) ? var5 : Integer.MIN_VALUE;
   }

   public static int getSafeYEnd(@NotNull ChunkSnapshot var0, int var1, int var2, int var3, int var4) {
      int var5 = var0.getHighestBlockYAt(var1, var2) + 1;
      return isSafe(var0, var1, var5, var2, var3, var4) ? var5 : Integer.MIN_VALUE;
   }

   public static int getSafeYNether(@NotNull ChunkSnapshot var0, int var1, int var2, int var3, int var4) {
      for (int var5 = 120; var5 >= 32; var5--) {
         if (isSafe(var0, var1, var5, var2, var3, var4)) {
            return var5;
         }
      }

      return Integer.MIN_VALUE;
   }

   public static int createSafeY(@Nullable ChunkSnapshot var0, @NotNull World var1, int var2, int var3) {
      int var4 = var2 & 15;
      int var5 = var3 & 15;
      int var6 = var1.getMinHeight();
      int var7 = var1.getMaxHeight();

      return switch (var1.getEnvironment()) {
         case NETHER -> getSafeYNether(Objects.requireNonNull(var0), var4, var5, var6, var7);
         case THE_END -> getSafeYEnd(Objects.requireNonNull(var0), var4, var5, var6, var7);
         default -> getSafeYOverworld(Objects.requireNonNull(var0), var4, var5, var6, var7);
      };
   }

   @NotNull
   public static CompletableFuture<Integer> createSafeYAsync(@NotNull World var0, int var1, int var2) {
      int var3 = var1 >> 4;
      int var4 = var2 >> 4;
      return getSnapshotAsync(var0, var3, var4).thenApplyAsync(var3x -> createSafeY(var3x, var0, var1, var2));
   }
}
