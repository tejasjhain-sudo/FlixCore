package org.lime.swiftCore.libs.lightcore.api.util;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StringUtil {
   private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-ORX]");
   private static final Pattern ALT_COLOR_PATTERN = Pattern.compile("(?i)&([0-9A-FK-ORX])");
   private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-zA-Z0-9]");
   private static final Pattern MULTISPACE = Pattern.compile("\\s+");
   private static final Pattern NON_DIGITS = Pattern.compile("\\D+");
   private static final SecureRandom RANDOM = new SecureRandom();

   private StringUtil() {
   }

   public static boolean isNullOrEmpty(@Nullable String var0) {
      return var0 == null || var0.trim().isEmpty();
   }

   @NotNull
   public static String defaultIfNull(@Nullable String var0, @NotNull String var1) {
      return var0 == null ? var1 : var0;
   }

   @NotNull
   public static String capitalize(@NotNull String var0) {
      return var0.isEmpty() ? var0 : var0.substring(0, 1).toUpperCase(Locale.ROOT) + var0.substring(1);
   }

   @NotNull
   public static String capitalizeFully(@NotNull String var0) {
      if (var0.isEmpty()) {
         return var0;
      } else {
         String[] var1 = var0.toLowerCase(Locale.ROOT).split(" ");
         StringBuilder var2 = new StringBuilder(var0.length());

         for (String var6 : var1) {
            if (!var6.isEmpty()) {
               var2.append(capitalize(var6)).append(' ');
            }
         }

         return var2.toString().trim();
      }
   }

   @NotNull
   public static String reverse(@NotNull String var0) {
      return new StringBuilder(var0).reverse().toString();
   }

   public static boolean equalsIgnoreCase(@Nullable String var0, @Nullable String var1) {
      return Objects.equals(var0 == null ? null : var0.toLowerCase(Locale.ROOT), var1 == null ? null : var1.toLowerCase(Locale.ROOT));
   }

   @NotNull
   public static String join(@NotNull Collection<String> var0, @NotNull String var1) {
      return String.join(var1, var0);
   }

   @NotNull
   public static List<String> split(@NotNull String var0) {
      return Arrays.stream(MULTISPACE.split(var0.trim())).filter(var0x -> !var0x.isEmpty()).toList();
   }

   @NotNull
   public static String[] splitByLength(@NotNull String var0, int var1) {
      if (var1 <= 0) {
         return new String[]{var0};
      } else {
         int var2 = (int)Math.ceil((double)var0.length() / (double)var1);
         String[] var3 = new String[var2];

         for (int var4 = 0; var4 < var2; var4++) {
            int var5 = var4 * var1;
            int var6 = Math.min(var0.length(), (var4 + 1) * var1);
            var3[var4] = var0.substring(var5, var6);
         }

         return var3;
      }
   }

   @NotNull
   public static String parsePlaceholders(@NotNull String var0, String... var1) {
      if (var0.isEmpty()) {
         return var0;
      } else {
         String var2 = var0;

         for (byte var3 = 0; var3 + 1 < var1.length; var3 += 2) {
            var2 = var2.replace(var1[var3], var1[var3 + 1]);
         }

         return var2;
      }
   }

   @NotNull
   public static String parsePlaceholders(@NotNull String var0, @NotNull Map<String, String> var1) {
      String var2 = var0;

      for (Entry var4 : var1.entrySet()) {
         var2 = var2.replace((CharSequence)var4.getKey(), (CharSequence)var4.getValue());
      }

      return var2;
   }

   @NotNull
   public static String stripColor(@NotNull String var0) {
      return STRIP_COLOR_PATTERN.matcher(var0).replaceAll("");
   }

   public static int countColors(@NotNull String var0) {
      Matcher var1 = STRIP_COLOR_PATTERN.matcher(var0);
      int var2 = 0;

      while (var1.find()) {
         var2++;
      }

      return var2;
   }

   @NotNull
   public static String slugify(@NotNull String var0) {
      String var1 = Normalizer.normalize(var0, Form.NFD);
      String var2 = var1.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
      return NON_ALPHANUMERIC.matcher(var2.toLowerCase(Locale.ROOT)).replaceAll("-").replaceAll("-{2,}", "-").replaceAll("^-|-$", "");
   }

   @NotNull
   public static String repeat(@NotNull String var0, int var1) {
      return var1 <= 0 ? "" : var0.repeat(var1);
   }

   @NotNull
   public static String truncate(@NotNull String var0, int var1, @NotNull String var2) {
      return var0.length() <= var1 ? var0 : var0.substring(0, Math.max(0, var1 - var2.length())) + var2;
   }

   public static boolean isNumeric(@NotNull String var0) {
      return NON_DIGITS.matcher(var0).replaceAll("").equals(var0);
   }

   @NotNull
   public static String padLeft(@NotNull String var0, int var1, char var2) {
      return var0.length() >= var1 ? var0 : String.valueOf(var2).repeat(var1 - var0.length()) + var0;
   }

   @NotNull
   public static String padRight(@NotNull String var0, int var1, char var2) {
      return var0.length() >= var1 ? var0 : var0 + String.valueOf(var2).repeat(var1 - var0.length());
   }

   @NotNull
   public static String randomString(int var0) {
      String var1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
      StringBuilder var2 = new StringBuilder(var0);

      for (int var3 = 0; var3 < var0; var3++) {
         var2.append(var1.charAt(RANDOM.nextInt(var1.length())));
      }

      return var2.toString();
   }

   @NotNull
   public static String pickRandom(@NotNull List<String> var0) {
      return var0.isEmpty() ? "" : (String)var0.get(RANDOM.nextInt(var0.size()));
   }

   @NotNull
   public static String shuffle(@NotNull String var0) {
      ArrayList var1 = new ArrayList();

      for (char var5 : var0.toCharArray()) {
         var1.add(var5);
      }

      Collections.shuffle(var1);
      StringBuilder var6 = new StringBuilder(var1.size());

      for (char var8 : var1) {
         var6.append(var8);
      }

      return var6.toString();
   }

   public static boolean containsIgnoreCase(@NotNull String var0, @NotNull String var1) {
      return var0.toLowerCase(Locale.ROOT).contains(var1.toLowerCase(Locale.ROOT));
   }

   public static boolean startsWithIgnoreCase(@NotNull String var0, @NotNull String var1) {
      return var0.toLowerCase(Locale.ROOT).startsWith(var1.toLowerCase(Locale.ROOT));
   }

   public static boolean endsWithIgnoreCase(@NotNull String var0, @NotNull String var1) {
      return var0.toLowerCase(Locale.ROOT).endsWith(var1.toLowerCase(Locale.ROOT));
   }

   public static int levenshtein(@NotNull String var0, @NotNull String var1) {
      int[][] var2 = new int[var0.length() + 1][var1.length() + 1];

      for (int var3 = 0; var3 <= var0.length(); var3++) {
         for (int var4 = 0; var4 <= var1.length(); var4++) {
            if (var3 == 0) {
               var2[var3][var4] = var4;
            } else if (var4 == 0) {
               var2[var3][var4] = var3;
            } else {
               var2[var3][var4] = Math.min(
                  Math.min(var2[var3 - 1][var4] + 1, var2[var3][var4 - 1] + 1),
                  var2[var3 - 1][var4 - 1] + (var0.charAt(var3 - 1) == var1.charAt(var4 - 1) ? 0 : 1)
               );
            }
         }
      }

      return var2[var0.length()][var1.length()];
   }

   public static double similarity(@NotNull String var0, @NotNull String var1) {
      if (var0.isEmpty() && var1.isEmpty()) {
         return 1.0;
      } else {
         int var2 = Math.max(var0.length(), var1.length());
         int var3 = levenshtein(var0, var1);
         return 1.0 - (double)var3 / (double)var2;
      }
   }
}
