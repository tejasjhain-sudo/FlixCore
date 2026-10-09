package org.lime.swiftCore.libs.lightcore.api.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

public final class LockUtil {
   private static final Map<UUID, ReentrantLock> LOCKS = new ConcurrentHashMap<>();

   private LockUtil() {
   }

   @NotNull
   public static ReentrantLock acquire(@NotNull UUID var0) {
      return LOCKS.computeIfAbsent(var0, var0x -> new ReentrantLock());
   }

   public static void release(@NotNull UUID var0, ReentrantLock var1) {
      if (var1 != null && var1.isHeldByCurrentThread()) {
         var1.unlock();
         if (!var1.isLocked()) {
            LOCKS.remove(var0, var1);
         }
      }
   }

   public static boolean tryLock(@NotNull UUID var0, long var1, @NotNull TimeUnit var3) {
      ReentrantLock var4 = acquire(var0);

      try {
         return var4.tryLock(var1, var3);
      } catch (InterruptedException var6) {
         Thread.currentThread().interrupt();
         return false;
      }
   }

   public static void withLock(@NotNull UUID var0, @NotNull Runnable var1) {
      ReentrantLock var2 = acquire(var0);
      var2.lock();

      try {
         var1.run();
      } finally {
         release(var0, var2);
      }
   }

   public static <T> T withLock(@NotNull UUID var0, @NotNull Supplier<T> var1) {
      ReentrantLock var2 = acquire(var0);
      var2.lock();

      Object var3;
      try {
         var3 = var1.get();
      } finally {
         release(var0, var2);
      }

      return (T)var3;
   }

   public static boolean isLocked(@NotNull UUID var0) {
      ReentrantLock var1 = LOCKS.get(var0);
      return var1 != null && var1.isLocked();
   }

   public static void forceRelease(@NotNull UUID var0) {
      ReentrantLock var1 = LOCKS.remove(var0);
      if (var1 != null) {
         while (var1.isHeldByCurrentThread()) {
            var1.unlock();
         }
      }
   }

   public static void clearAll() {
      LOCKS.clear();
   }
}
