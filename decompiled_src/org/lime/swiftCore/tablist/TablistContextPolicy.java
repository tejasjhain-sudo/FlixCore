package org.lime.swiftCore.tablist;

final class TablistContextPolicy {
   private TablistContextPolicy() {
   }

   static boolean canTransition(TablistContext var0, TablistContext var1) {
      return var0 == null || var0 == var1 || priority(var1) >= priority(var0);
   }

   static int priority(TablistContext var0) {
      if (var0 == null || var0 == TablistContext.DEFAULT) {
         return 0;
      } else if (var0 == TablistContext.GLOBAL) {
         return 1;
      } else if (var0 == TablistContext.QUEUE || var0 == TablistContext.PARTY) {
         return 2;
      } else {
         return var0 == TablistContext.LOBBY_FRIENDS ? 3 : 4;
      }
   }
}
