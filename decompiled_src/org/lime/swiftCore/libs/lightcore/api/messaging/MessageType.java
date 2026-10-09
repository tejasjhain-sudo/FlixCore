package org.lime.swiftCore.libs.lightcore.api.messaging;

import org.jetbrains.annotations.NotNull;

public enum MessageType {
   CHAT,
   ACTIONBAR,
   TITLE,
   BOSSBAR;

   @NotNull
   public static MessageType get(@NotNull String var0) {
      if (var0.isEmpty()) {
         return CHAT;
      } else {
         try {
            return valueOf(var0.trim().toUpperCase());
         } catch (IllegalArgumentException var2) {
            return CHAT;
         }
      }
   }
}
