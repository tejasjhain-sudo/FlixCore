package org.lime.swiftCore.libs.lightcore.api.color;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Text {
   private Text() {
   }

   @NotNull
   public static Component color(@Nullable String var0) {
      return TextService.format(var0);
   }

   @NotNull
   public static Component color(@Nullable Component var0) {
      return TextService.format(var0);
   }

   @NotNull
   public static String colorLegacy(@Nullable String var0) {
      return TextService.formatLegacy(var0);
   }

   @NotNull
   public static List<Component> colorList(@Nullable List<String> var0) {
      return TextService.formatList(var0);
   }

   @NotNull
   public static List<Component> colorComponentList(@Nullable List<Component> var0) {
      return TextService.formatComponentList(var0);
   }

   @NotNull
   public static List<String> colorLegacyList(@Nullable List<String> var0) {
      return TextService.formatLegacyList(var0);
   }

   @NotNull
   public static String toLegacy(@Nullable Component var0) {
      return TextService.toLegacyString(var0);
   }

   @NotNull
   public static String toMiniMessage(@Nullable Component var0) {
      return TextService.toMiniMessageString(var0);
   }
}
