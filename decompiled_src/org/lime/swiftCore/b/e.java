package org.lime.swiftCore.b;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.object.PlayerHeadObjectContents;

public final class e {
   public static final String Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new = "head_database";
   private static final MiniMessage o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = MiniMessage.builder()
      .tags(
         TagResolver.builder()
            .resolver(StandardTags.defaults())
            .resolver(
               TagResolver.resolver(
                  "head_database",
                  e::o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
               )
            )
            .build()
      )
      .build();

   private e() {
   }

   public static MiniMessage o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super() {
      return o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;
   }

   public static boolean Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
      String var0
   ) {
      if (var0 != null && !var0.isBlank()) {
         String var1 = var0.trim();
         if (var1.startsWith("<head_database")) {
            return true;
         } else {
            return var1.regionMatches(true, 0, "head_database:", 0, "head_database:".length()) ? true : var1.startsWith("eyJ") && var1.length() > 20;
         }
      } else {
         return false;
      }
   }

   public static String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
      String var0
   ) {
      if (var0 != null && !var0.isBlank()) {
         String var1 = var0.trim();
         if (var1.startsWith("<head_database")) {
            return var1;
         } else {
            if (var1.regionMatches(true, 0, "head_database:", 0, "head_database:".length())) {
               var1 = var1.substring("head_database:".length()).trim();
            }

            if (var1.startsWith("'") && var1.endsWith("'") || var1.startsWith("\"") && var1.endsWith("\"")) {
               var1 = var1.substring(1, var1.length() - 1);
            }

            return var1.isBlank() ? "" : "<head_database:'" + var1 + "'>";
         }
      } else {
         return "";
      }
   }

   private static Tag o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
      ArgumentQueue var0, Context var1
   ) {
      String var2 = var0.popOr("A base64 texture value is required").value();
      return var2.isBlank()
         ? Tag.selfClosingInserting(Component.empty())
         : Tag.selfClosingInserting(Component.object(ObjectContents.playerHead().profileProperty(PlayerHeadObjectContents.property("textures", var2)).build()));
   }
}
