package org.checkerframework.checker.i18nformatter.qual;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import org.checkerframework.checker.nullness.qual.Nullable;

public enum I18nConversionCategory {
   UNUSED(null, null),
   GENERAL(null, null),
   DATE(new Class[]{Date.class, Number.class}, new String[]{"date", "time"}),
   NUMBER(new Class[]{Number.class}, new String[]{"number", "choice"});

   @Nullable
   public final Class<?>[] types;
   @Nullable
   public final String[] strings;
   private static final I18nConversionCategory[] namedCategories = new I18nConversionCategory[]{DATE, NUMBER};
   private static final I18nConversionCategory[] conversionCategoriesForIntersect = new I18nConversionCategory[]{DATE, NUMBER};

   private I18nConversionCategory(@Nullable Class<?>[] var3, @Nullable String[] var4) {
      this.types = var3;
      this.strings = var4;
   }

   public static I18nConversionCategory stringToI18nConversionCategory(String var0) {
      var0 = var0.toLowerCase();

      for (I18nConversionCategory var4 : namedCategories) {
         for (String var8 : var4.strings) {
            if (var8.equals(var0)) {
               return var4;
            }
         }
      }

      throw new IllegalArgumentException("Invalid format type " + var0);
   }

   private static <E> Set<E> arrayToSet(E[] var0) {
      return new HashSet<>(Arrays.asList((E[])var0));
   }

   public static boolean isSubsetOf(I18nConversionCategory var0, I18nConversionCategory var1) {
      return intersect(var0, var1) == var0;
   }

   public static I18nConversionCategory intersect(I18nConversionCategory var0, I18nConversionCategory var1) {
      if (var0 == UNUSED) {
         return var1;
      } else if (var1 == UNUSED) {
         return var0;
      } else if (var0 == GENERAL) {
         return var1;
      } else if (var1 == GENERAL) {
         return var0;
      } else {
         Set var2 = arrayToSet(var0.types);
         Set var3 = arrayToSet(var1.types);
         var2.retainAll(var3);

         for (I18nConversionCategory var7 : conversionCategoriesForIntersect) {
            Set var8 = arrayToSet(var7.types);
            if (var8.equals(var2)) {
               return var7;
            }
         }

         throw new RuntimeException();
      }
   }

   public static I18nConversionCategory union(I18nConversionCategory var0, I18nConversionCategory var1) {
      if (var0 == UNUSED || var1 == UNUSED) {
         return UNUSED;
      } else if (var0 == GENERAL || var1 == GENERAL) {
         return GENERAL;
      } else {
         return var0 != DATE && var1 != DATE ? NUMBER : DATE;
      }
   }

   public boolean isAssignableFrom(Class<?> var1) {
      if (this.types == null) {
         return true;
      } else if (var1 == void.class) {
         return true;
      } else {
         for (Class var5 : this.types) {
            if (var5.isAssignableFrom(var1)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public String toString() {
      StringBuilder var1 = new StringBuilder(this.name());
      if (this.types == null) {
         var1.append(" conversion category (all types)");
      } else {
         StringJoiner var2 = new StringJoiner(", ", " conversion category (one of: ", ")");

         for (Class var6 : this.types) {
            var2.add(var6.getCanonicalName());
         }

         var1.append(var2);
      }

      return var1.toString();
   }
}
