package org.checkerframework.checker.formatter.qual;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;

public enum ConversionCategory {
   GENERAL("bBhHsS", (Class<?>[])null),
   CHAR("cC", Character.class, Byte.class, Short.class, Integer.class),
   INT("doxX", Byte.class, Short.class, Integer.class, Long.class, BigInteger.class),
   FLOAT("eEfgGaA", Float.class, Double.class, BigDecimal.class),
   TIME("tT", Long.class, Calendar.class, Date.class),
   CHAR_AND_INT(null, Byte.class, Short.class, Integer.class),
   INT_AND_TIME(null, Long.class),
   NULL(null),
   UNUSED(null, (Class<?>[])null);

   @Nullable
   public final Class<?>[] types;
   @Nullable
   public final String chars;
   private static final ConversionCategory[] conversionCategoriesWithChar = new ConversionCategory[]{GENERAL, CHAR, INT, FLOAT, TIME};
   private static final ConversionCategory[] conversionCategoriesForIntersect = new ConversionCategory[]{
      CHAR, INT, FLOAT, TIME, CHAR_AND_INT, INT_AND_TIME, NULL
   };
   private static final ConversionCategory[] conversionCategoriesForUnion = new ConversionCategory[]{NULL, CHAR_AND_INT, INT_AND_TIME, CHAR, INT, FLOAT, TIME};

   private ConversionCategory(@Nullable String var3, @Nullable Class<?>... var4) {
      this.chars = var3;
      if (var4 == null) {
         this.types = var4;
      } else {
         ArrayList var5 = new ArrayList(var4.length);

         for (Class var9 : var4) {
            var5.add(var9);
            Class var10 = unwrapPrimitive(var9);
            if (var10 != null) {
               var5.add(var10);
            }
         }

         this.types = var5.toArray(new Class[var5.size()]);
      }
   }

   @Nullable
   private static Class<? extends Object> unwrapPrimitive(Class<?> var0) {
      if (var0 == Byte.class) {
         return byte.class;
      } else if (var0 == Character.class) {
         return char.class;
      } else if (var0 == Short.class) {
         return short.class;
      } else if (var0 == Integer.class) {
         return int.class;
      } else if (var0 == Long.class) {
         return long.class;
      } else if (var0 == Float.class) {
         return float.class;
      } else if (var0 == Double.class) {
         return double.class;
      } else {
         return var0 == Boolean.class ? boolean.class : null;
      }
   }

   public static ConversionCategory fromConversionChar(char var0) {
      for (ConversionCategory var4 : conversionCategoriesWithChar) {
         if (var4.chars.contains(String.valueOf(var0))) {
            return var4;
         }
      }

      throw new IllegalArgumentException("Bad conversion character " + var0);
   }

   private static <E> Set<E> arrayToSet(E[] var0) {
      return new HashSet<>(Arrays.asList((E[])var0));
   }

   public static boolean isSubsetOf(ConversionCategory var0, ConversionCategory var1) {
      return intersect(var0, var1) == var0;
   }

   public static ConversionCategory intersect(ConversionCategory var0, ConversionCategory var1) {
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

         for (ConversionCategory var7 : conversionCategoriesForIntersect) {
            Set var8 = arrayToSet(var7.types);
            if (var8.equals(var2)) {
               return var7;
            }
         }

         throw new RuntimeException();
      }
   }

   public static ConversionCategory union(ConversionCategory var0, ConversionCategory var1) {
      if (var0 == UNUSED || var1 == UNUSED) {
         return UNUSED;
      } else if (var0 != GENERAL && var1 != GENERAL) {
         if ((var0 != CHAR_AND_INT || var1 != INT_AND_TIME) && (var0 != INT_AND_TIME || var1 != CHAR_AND_INT)) {
            Set var2 = arrayToSet(var0.types);
            Set var3 = arrayToSet(var1.types);
            var2.addAll(var3);

            for (ConversionCategory var7 : conversionCategoriesForUnion) {
               Set var8 = arrayToSet(var7.types);
               if (var8.equals(var2)) {
                  return var7;
               }
            }

            return GENERAL;
         } else {
            return INT;
         }
      } else {
         return GENERAL;
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

   @Pure
   @Override
   public String toString() {
      StringBuilder var1 = new StringBuilder();
      var1.append(this.name());
      var1.append(" conversion category");
      if (this.types != null && this.types.length != 0) {
         StringJoiner var2 = new StringJoiner(", ", "(one of: ", ")");

         for (Class var6 : this.types) {
            var2.add(var6.getSimpleName());
         }

         var1.append(" ");
         var1.append(var2);
         return var1.toString();
      } else {
         return var1.toString();
      }
   }
}
