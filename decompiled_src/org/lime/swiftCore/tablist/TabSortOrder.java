package org.lime.swiftCore.tablist;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class TabSortOrder {
   private TabSortOrder() {
   }

   static List<TabSortOrder.Entry> sorted(List<TabSortOrder.Entry> var0, TabSortOrder.Mode var1, TabSortOrder.ValueType var2, TabSortOrder.Direction var3) {
      Comparator var4 = var1 == TabSortOrder.Mode.PLACEHOLDER ? placeholderComparator(var2, var3) : Comparator.comparingInt(TabSortOrder.Entry::priority);
      return var0.stream()
         .sorted(var4.thenComparing(TabSortOrder.Entry::playerName, String.CASE_INSENSITIVE_ORDER).thenComparing(TabSortOrder.Entry::playerName))
         .toList();
   }

   private static Comparator<TabSortOrder.Entry> placeholderComparator(TabSortOrder.ValueType var0, TabSortOrder.Direction var1) {
      if (var0 == TabSortOrder.ValueType.TEXT) {
         Comparator var3 = String.CASE_INSENSITIVE_ORDER;
         if (var1 == TabSortOrder.Direction.DESCENDING) {
            var3 = var3.reversed();
         }

         return Comparator.comparing(var0x -> safe(var0x.placeholderValue()), var3);
      } else {
         Comparator var2 = Comparator.naturalOrder();
         if (var1 == TabSortOrder.Direction.DESCENDING) {
            var2 = var2.reversed();
         }

         return Comparator.comparing(var0x -> number(var0x.placeholderValue()), Comparator.nullsLast(var2));
      }
   }

   private static String safe(String var0) {
      return var0 == null ? "" : var0;
   }

   private static BigDecimal number(String var0) {
      if (var0 == null) {
         return null;
      } else {
         try {
            return new BigDecimal(var0.trim().replace(",", ""));
         } catch (NumberFormatException var2) {
            return null;
         }
      }
   }

   static enum Direction {
      ASCENDING,
      DESCENDING;

      static TabSortOrder.Direction parse(String var0) {
         return var0 == null || !var0.equalsIgnoreCase("desc") && !var0.equalsIgnoreCase("descending") ? ASCENDING : DESCENDING;
      }
   }

   static record Entry(String playerName, int priority, String placeholderValue) {
   }

   static enum Mode {
      LUCKPERMS_WEIGHT,
      CONFIGURED_GROUPS,
      PLACEHOLDER;

      static TabSortOrder.Mode parse(String var0) {
         if (var0 == null) {
            return LUCKPERMS_WEIGHT;
         } else {
            String var1 = var0.trim().toLowerCase(Locale.ROOT).replace('_', '-');

            return switch (var1) {
               case "configured-groups", "group-order", "groups" -> CONFIGURED_GROUPS;
               case "placeholder" -> PLACEHOLDER;
               default -> LUCKPERMS_WEIGHT;
            };
         }
      }
   }

   static enum ValueType {
      NUMERIC,
      TEXT;

      static TabSortOrder.ValueType parse(String var0) {
         return var0 != null && var0.equalsIgnoreCase("text") ? TEXT : NUMERIC;
      }
   }
}
