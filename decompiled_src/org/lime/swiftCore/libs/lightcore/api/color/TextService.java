package org.lime.swiftCore.libs.lightcore.api.color;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TextService {
   private static final Map<String, String> LEGACY_COLOR_CODES = Map.ofEntries(
      Map.entry("black", "0"),
      Map.entry("dark_blue", "1"),
      Map.entry("dark_green", "2"),
      Map.entry("dark_aqua", "3"),
      Map.entry("dark_red", "4"),
      Map.entry("dark_purple", "5"),
      Map.entry("gold", "6"),
      Map.entry("gray", "7"),
      Map.entry("dark_gray", "8"),
      Map.entry("blue", "9"),
      Map.entry("green", "a"),
      Map.entry("aqua", "b"),
      Map.entry("red", "c"),
      Map.entry("light_purple", "d"),
      Map.entry("yellow", "e"),
      Map.entry("white", "f"),
      Map.entry("bold", "l"),
      Map.entry("italic", "o"),
      Map.entry("underlined", "n"),
      Map.entry("strikethrough", "m"),
      Map.entry("obfuscated", "k"),
      Map.entry("reset", "r")
   );
   private static final Pattern MINIMESSAGE_TAG = Pattern.compile("<(/?)(#[a-fA-F0-9]{6}|[a-zA-Z_]+)>");
   private static final Pattern HEX_TAG = Pattern.compile("&#([a-fA-F0-9]{6})");
   private static final Pattern MINIMESSAGE_FEATURES = Pattern.compile("<(click|hover|insertion|gradient|rainbow|head|/|#)", 2);
   private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
   private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
      .character('&')
      .hexCharacter('#')
      .useUnusualXRepeatedCharacterHexFormat()
      .build();
   private static final int MAX_CACHE_SIZE = 1000;
   private static final Map<String, Component> COMPONENT_CACHE = new ConcurrentHashMap<>();
   private static final Map<String, String> STRING_CACHE = new ConcurrentHashMap<>();

   private TextService() {
   }

   @NotNull
   public static Component format(@Nullable String var0) {
      if (var0 != null && !var0.isBlank()) {
         Component var1 = COMPONENT_CACHE.get(var0);
         if (var1 != null) {
            return var1;
         } else {
            Matcher var2 = MINIMESSAGE_FEATURES.matcher(var0);
            Component var3 = (Component)(var2.find() ? MINI_MESSAGE.deserialize(var0) : LEGACY_SERIALIZER.deserialize(var0));
            var3 = var3.decoration(TextDecoration.ITALIC, false);
            if (COMPONENT_CACHE.size() < 1000) {
               COMPONENT_CACHE.put(var0, var3);
            }

            return var3;
         }
      } else {
         return Component.empty();
      }
   }

   @NotNull
   public static Component format(@Nullable Component var0) {
      if (var0 != null && !var0.equals(Component.empty())) {
         String var1 = (String)MINI_MESSAGE.serialize(var0);
         Matcher var2 = MINIMESSAGE_FEATURES.matcher(var1);
         Object var3 = var2.find() ? MINI_MESSAGE.deserialize(var1) : LEGACY_SERIALIZER.deserialize(LEGACY_SERIALIZER.serialize(var0));
         return var3.decoration(TextDecoration.ITALIC, false);
      } else {
         return Component.empty();
      }
   }

   @NotNull
   public static String formatLegacy(@Nullable String var0) {
      if (var0 != null && !var0.isBlank()) {
         String var1 = STRING_CACHE.get(var0);
         if (var1 != null) {
            return var1;
         } else {
            String var2 = ChatColor.translateAlternateColorCodes('&', var0);
            Matcher var3 = HEX_TAG.matcher(var2);
            if (var3.find()) {
               int var4 = var2.length();
               StringBuilder var5 = new StringBuilder(var4 + 24);
               int var6 = 0;

               do {
                  var5.append(var2, var6, var3.start()).append(ChatColor.of("#" + var3.group(1)));
                  var6 = var3.end();
               } while (var3.find());

               var5.append(var2, var6, var4);
               var2 = var5.toString();
            }

            Matcher var14 = MINIMESSAGE_TAG.matcher(var2);
            int var15 = var2.length();
            StringBuilder var16 = new StringBuilder(var15 + 24);
            ArrayDeque var7 = new ArrayDeque(4);

            int var8;
            for (var8 = 0; var14.find(); var8 = var14.end()) {
               var16.append(var2, var8, var14.start());
               boolean var9 = var14.group(1).length() == 1;
               String var10 = var14.group(2);
               String var11 = var10.isEmpty() ? var10 : var10.toLowerCase(Locale.ROOT);
               if (var9 && !var7.isEmpty() && (var7.contains(var11) || var11.length() == 7 && var11.charAt(0) == '#')) {
                  var16.append(ChatColor.RESET);
                  var7.remove(var11);

                  for (String var13 : var7) {
                     var16.append(resolve(var13));
                  }
               } else if (!var9) {
                  var7.push(var11);
                  var16.append(resolve(var11));
               }
            }

            String var17 = var16.append(var2, var8, var15).toString();
            if (STRING_CACHE.size() < 1000) {
               STRING_CACHE.put(var0, var17);
            }

            return var17;
         }
      } else {
         return "";
      }
   }

   @NotNull
   public static List<Component> formatList(@Nullable List<String> var0) {
      return var0 != null && !var0.isEmpty() ? var0.stream().map(TextService::format).toList() : List.of();
   }

   @NotNull
   public static List<Component> formatComponentList(@Nullable List<Component> var0) {
      return var0 != null && !var0.isEmpty() ? var0.stream().map(TextService::format).toList() : List.of();
   }

   @NotNull
   public static List<String> formatLegacyList(@Nullable List<String> var0) {
      return var0 != null && !var0.isEmpty() ? var0.stream().map(TextService::formatLegacy).toList() : List.of();
   }

   @NotNull
   public static String toLegacyString(@Nullable Component var0) {
      return var0 == null ? "" : LEGACY_SERIALIZER.serialize(var0);
   }

   @NotNull
   public static String toMiniMessageString(@Nullable Component var0) {
      return var0 == null ? "" : (String)MINI_MESSAGE.serialize(var0);
   }

   @NotNull
   private static String resolve(@NotNull String var0) {
      if (var0.startsWith("#") && var0.length() == 7) {
         try {
            return ChatColor.of(var0).toString();
         } catch (IllegalArgumentException var2) {
            return "";
         }
      } else {
         String var1 = LEGACY_COLOR_CODES.get(var0);
         return var1 != null ? "§" + var1 : "";
      }
   }

   public static void clearCache() {
      COMPONENT_CACHE.clear();
      STRING_CACHE.clear();
   }
}
