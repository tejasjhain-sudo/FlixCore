package org.lime.swiftCore.tablist;

import java.util.Objects;

final class TablistUpdatePolicy {
   private TablistUpdatePolicy() {
   }

   static boolean isDue(boolean var0, boolean var1, boolean var2, boolean var3, long var4, long var6) {
      if (var0 || var1) {
         return true;
      } else {
         return var2 ? var4 >= var6 : var3 && var4 >= var6;
      }
   }

   static long nextUpdateAt(long var0, int var2, int var3) {
      return var2 > 0 ? var0 + (long)var2 : var0 + (long)Math.max(1, var3) * 50L;
   }

   static boolean renderedOutputChanged(Object var0, Object var1, Object var2, Object var3) {
      return !Objects.equals(var0, var2) || !Objects.equals(var1, var3);
   }
}
