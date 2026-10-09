package org.lime.swiftCore.libs.lightcore.api.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CooldownUtil {
   private static final Map<String, Map<UUID, Long>> COOLDOWNS = new ConcurrentHashMap<>();

   private CooldownUtil() {
   }

   public static boolean isOnCooldown(@NotNull String var0, @NotNull UUID var1) {
      Map var2 = COOLDOWNS.get(var0);
      if (var2 == null) {
         return false;
      } else {
         Long var3 = (Long)var2.get(var1);
         if (var3 == null) {
            return false;
         } else if (var3 <= System.currentTimeMillis()) {
            var2.remove(var1);
            return false;
         } else {
            return true;
         }
      }
   }

   public static boolean isOnCooldown(@NotNull String var0, @NotNull Player var1) {
      return isOnCooldown(var0, var1.getUniqueId());
   }

   public static long getRemaining(@NotNull String var0, @NotNull UUID var1) {
      Map var2 = COOLDOWNS.get(var0);
      if (var2 == null) {
         return 0L;
      } else {
         Long var3 = (Long)var2.get(var1);
         return var3 == null ? 0L : Math.max(0L, var3 - System.currentTimeMillis());
      }
   }

   public static long getRemainingSeconds(@NotNull String var0, @NotNull UUID var1) {
      return getRemaining(var0, var1) / 1000L;
   }

   public static void setCooldown(@NotNull String var0, @NotNull UUID var1, long var2) {
      COOLDOWNS.computeIfAbsent(var0, var0x -> new ConcurrentHashMap<>()).put(var1, System.currentTimeMillis() + var2);
   }

   public static void setCooldown(@NotNull String var0, @NotNull Player var1, long var2) {
      setCooldown(var0, var1.getUniqueId(), var2);
   }

   public static void setCooldownSeconds(@NotNull String var0, @NotNull UUID var1, long var2) {
      setCooldown(var0, var1, var2 * 1000L);
   }

   public static void clear(@NotNull String var0, @NotNull UUID var1) {
      Map var2 = COOLDOWNS.get(var0);
      if (var2 != null) {
         var2.remove(var1);
      }
   }

   public static void clearAll(@NotNull UUID var0) {
      COOLDOWNS.values().forEach(var1 -> var1.remove(var0));
   }

   public static void clearKey(@NotNull String var0) {
      COOLDOWNS.remove(var0);
   }

   @NotNull
   public static CooldownUtil.CooldownBuilder of(@NotNull String var0) {
      return new CooldownUtil.CooldownBuilder(var0);
   }

   public static final class CooldownBuilder {
      private final String key;
      private UUID uuid;
      private long duration;

      private CooldownBuilder(@NotNull String var1) {
         this.key = var1;
      }

      @NotNull
      public CooldownUtil.CooldownBuilder player(@Nullable Player var1) {
         this.uuid = var1 != null ? var1.getUniqueId() : null;
         return this;
      }

      @NotNull
      public CooldownUtil.CooldownBuilder uuid(@NotNull UUID var1) {
         this.uuid = var1;
         return this;
      }

      @NotNull
      public CooldownUtil.CooldownBuilder duration(long var1) {
         this.duration = var1;
         return this;
      }

      @NotNull
      public CooldownUtil.CooldownBuilder durationSeconds(long var1) {
         this.duration = var1 * 1000L;
         return this;
      }

      public void start() {
         if (this.uuid != null && this.duration > 0L) {
            CooldownUtil.setCooldown(this.key, this.uuid, this.duration);
         }
      }

      public boolean check() {
         return this.uuid != null && CooldownUtil.isOnCooldown(this.key, this.uuid);
      }

      public long remaining() {
         return this.uuid == null ? 0L : CooldownUtil.getRemaining(this.key, this.uuid);
      }

      public long remainingSeconds() {
         return this.remaining() / 1000L;
      }

      public void clear() {
         if (this.uuid != null) {
            CooldownUtil.clear(this.key, this.uuid);
         }
      }
   }
}
