package org.lime.swiftCore.b;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

public class d {
   public static void o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
      ItemMeta var0, ConfigurationSection var1
   ) {
      if (var0 != null && var1 != null) {
         int var2 = var1.getInt("custom-model-data", -1);
         if (var2 >= 0) {
            var0.setCustomModelData(var2);
         }
      }
   }

   public static void o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
      ItemMeta var0, YamlConfiguration var1, String var2
   ) {
      if (var0 != null && var1 != null) {
         int var3 = var1.getInt(var2 + "custom-model-data", -1);
         if (var3 >= 0) {
            var0.setCustomModelData(var3);
         }
      }
   }

   public static void o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
      ItemMeta var0, YamlConfiguration var1
   ) {
      if (var0 != null && var1 != null) {
         List var2 = var1.getStringList("gui.item-attributes");
         if (!var2.isEmpty()) {
            for (String var4 : var2) {
               try {
                  ItemFlag var5 = ItemFlag.valueOf(var4.toUpperCase().trim());
                  var0.addItemFlags(new ItemFlag[]{var5});
               } catch (IllegalArgumentException var6) {
               }
            }
         }
      }
   }

   public static void o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
      ItemMeta var0, FileConfiguration var1
   ) {
      if (var0 != null && var1 != null) {
         List var2 = var1.getStringList("gui.item-attributes");
         if (!var2.isEmpty()) {
            for (String var4 : var2) {
               try {
                  ItemFlag var5 = ItemFlag.valueOf(var4.toUpperCase().trim());
                  var0.addItemFlags(new ItemFlag[]{var5});
               } catch (IllegalArgumentException var6) {
               }
            }
         }
      }
   }

   public static void o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
      ItemMeta var0, List<String> var1
   ) {
      if (var0 != null && var1 != null && !var1.isEmpty()) {
         for (String var3 : var1) {
            try {
               ItemFlag var4 = ItemFlag.valueOf(var3.toUpperCase().trim());
               var0.addItemFlags(new ItemFlag[]{var4});
            } catch (IllegalArgumentException var5) {
            }
         }
      }
   }
}
