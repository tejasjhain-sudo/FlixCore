package org.lime.swiftCore.libs.lightcore.api.util;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PDCUtil {
   private static JavaPlugin plugin;

   private PDCUtil() {
   }

   public static void init(@NotNull JavaPlugin var0) {
      plugin = var0;
   }

   public static <T, Z> void set(@NotNull PersistentDataContainer var0, @NotNull String var1, @NotNull PersistentDataType<T, Z> var2, @NotNull Z var3) {
      var0.set(createKey(var1), var2, var3);
   }

   @Nullable
   public static <T, Z> Z get(@NotNull PersistentDataContainer var0, @NotNull String var1, @NotNull PersistentDataType<T, Z> var2) {
      return (Z)var0.get(createKey(var1), var2);
   }

   public static boolean has(@NotNull PersistentDataContainer var0, @NotNull String var1, @NotNull PersistentDataType<?, ?> var2) {
      return var0.has(createKey(var1), var2);
   }

   public static void remove(@NotNull PersistentDataContainer var0, @NotNull String var1) {
      var0.remove(createKey(var1));
   }

   public static boolean getBoolean(@NotNull PersistentDataContainer var0, @NotNull String var1) {
      Byte var2 = get(var0, var1, PersistentDataType.BYTE);
      return var2 != null && var2 == 1;
   }

   public static void setBoolean(@NotNull PersistentDataContainer var0, @NotNull String var1, boolean var2) {
      set(var0, var1, PersistentDataType.BYTE, (byte)(var2 ? 1 : 0));
   }

   @Nullable
   public static String getString(@NotNull PersistentDataContainer var0, @NotNull String var1) {
      return get(var0, var1, PersistentDataType.STRING);
   }

   public static void setString(@NotNull PersistentDataContainer var0, @NotNull String var1, @NotNull String var2) {
      set(var0, var1, PersistentDataType.STRING, var2);
   }

   public static int getInt(@NotNull PersistentDataContainer var0, @NotNull String var1, int var2) {
      Integer var3 = get(var0, var1, PersistentDataType.INTEGER);
      return var3 != null ? var3 : var2;
   }

   public static void setInt(@NotNull PersistentDataContainer var0, @NotNull String var1, int var2) {
      set(var0, var1, PersistentDataType.INTEGER, var2);
   }

   public static long getLong(@NotNull PersistentDataContainer var0, @NotNull String var1, long var2) {
      Long var4 = get(var0, var1, PersistentDataType.LONG);
      return var4 != null ? var4 : var2;
   }

   public static void setLong(@NotNull PersistentDataContainer var0, @NotNull String var1, long var2) {
      set(var0, var1, PersistentDataType.LONG, var2);
   }

   public static double getDouble(@NotNull PersistentDataContainer var0, @NotNull String var1, double var2) {
      Double var4 = get(var0, var1, PersistentDataType.DOUBLE);
      return var4 != null ? var4 : var2;
   }

   public static void setDouble(@NotNull PersistentDataContainer var0, @NotNull String var1, double var2) {
      set(var0, var1, PersistentDataType.DOUBLE, var2);
   }

   @NotNull
   private static NamespacedKey createKey(@NotNull String var0) {
      if (plugin == null) {
         throw new IllegalStateException("PDCUtil not initialized! Call PDCUtil.init(plugin) first.");
      } else {
         return new NamespacedKey(plugin, var0);
      }
   }
}
