package org.lime.swiftCore.b;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class g {
   public static String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
      ItemStack var0
   ) {
      try {
         String var3;
         try (ByteArrayOutputStream var1 = new ByteArrayOutputStream()) {
            BukkitObjectOutputStream var2 = new BukkitObjectOutputStream(var1);

            try {
               var2.writeObject(var0);
               var3 = Base64.getEncoder().encodeToString(var1.toByteArray());
            } catch (Throwable var7) {
               try {
                  var2.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            var2.close();
         }

         return var3;
      } catch (Exception var9) {
         throw new RuntimeException("Failed to serialize item", var9);
      }
   }

   public static ItemStack Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new(
      String var0
   ) {
      try {
         ItemStack var3;
         try (ByteArrayInputStream var1 = new ByteArrayInputStream(Base64.getDecoder().decode(var0))) {
            BukkitObjectInputStream var2 = new BukkitObjectInputStream(var1);

            try {
               var3 = (ItemStack)var2.readObject();
            } catch (Throwable var7) {
               try {
                  var2.close();
               } catch (Throwable var6) {
                  var7.addSuppressed(var6);
               }

               throw var7;
            }

            var2.close();
         }

         return var3;
      } catch (Exception var9) {
         throw new RuntimeException("Failed to deserialize item", var9);
      }
   }

   public static String o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
      ItemStack[] var0
   ) {
      try {
         String var12;
         try (ByteArrayOutputStream var1 = new ByteArrayOutputStream()) {
            BukkitObjectOutputStream var2 = new BukkitObjectOutputStream(var1);

            try {
               var2.writeInt(var0.length);

               for (ItemStack var6 : var0) {
                  var2.writeObject(var6);
               }

               var12 = Base64.getEncoder().encodeToString(var1.toByteArray());
            } catch (Throwable var9) {
               try {
                  var2.close();
               } catch (Throwable var8) {
                  var9.addSuppressed(var8);
               }

               throw var9;
            }

            var2.close();
         }

         return var12;
      } catch (Exception var11) {
         throw new RuntimeException("Failed to serialize items", var11);
      }
   }

   public static ItemStack[] o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
      String var0
   ) {
      try {
         ItemStack[] var11;
         try (ByteArrayInputStream var1 = new ByteArrayInputStream(Base64.getDecoder().decode(var0))) {
            BukkitObjectInputStream var2 = new BukkitObjectInputStream(var1);

            try {
               int var3 = var2.readInt();
               ItemStack[] var4 = new ItemStack[var3];

               for (int var5 = 0; var5 < var3; var5++) {
                  var4[var5] = (ItemStack)var2.readObject();
               }

               var11 = var4;
            } catch (Throwable var8) {
               try {
                  var2.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }

               throw var8;
            }

            var2.close();
         }

         return var11;
      } catch (Exception var10) {
         throw new RuntimeException("Failed to deserialize items", var10);
      }
   }
}
