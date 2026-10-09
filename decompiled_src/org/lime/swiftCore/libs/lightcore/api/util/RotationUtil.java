package org.lime.swiftCore.libs.lightcore.api.util;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RotationUtil {
   private RotationUtil() {
   }

   @Nullable
   public static <T> T current(@NotNull List<T> var0, @NotNull AtomicInteger var1) {
      return (T)(var0.isEmpty() ? null : var0.get(Math.floorMod(var1.get(), var0.size())));
   }

   @Nullable
   public static <T> T next(@NotNull List<T> var0, @NotNull AtomicInteger var1) {
      return (T)(var0.isEmpty() ? null : var0.get(Math.floorMod(var1.incrementAndGet(), var0.size())));
   }

   @Nullable
   public static <T> T previous(@NotNull List<T> var0, @NotNull AtomicInteger var1) {
      return (T)(var0.isEmpty() ? null : var0.get(Math.floorMod(var1.decrementAndGet(), var0.size())));
   }

   @Nullable
   public static <T> T peekNext(@NotNull List<T> var0, @NotNull AtomicInteger var1) {
      return (T)(var0.isEmpty() ? null : var0.get(Math.floorMod(var1.get() + 1, var0.size())));
   }

   @Nullable
   public static <T> T peekPrevious(@NotNull List<T> var0, @NotNull AtomicInteger var1) {
      return (T)(var0.isEmpty() ? null : var0.get(Math.floorMod(var1.get() - 1, var0.size())));
   }

   public static void reset(@NotNull AtomicInteger var0) {
      var0.set(0);
   }

   public static <T> void setPosition(@NotNull List<T> var0, @NotNull AtomicInteger var1, int var2) {
      if (!var0.isEmpty()) {
         var1.set(Math.floorMod(var2, var0.size()));
      }
   }
}
