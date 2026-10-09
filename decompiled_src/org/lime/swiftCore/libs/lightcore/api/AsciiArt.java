package org.lime.swiftCore.libs.lightcore.api;

import java.util.HashMap;
import java.util.Map;

public class AsciiArt {
   private static final Map<Character, String[]> ASCII_LETTERS = new HashMap<>();

   public static String[] generate(String var0) {
      if (var0 != null && !var0.isEmpty()) {
         var0 = var0.toUpperCase();
         String[] var1 = new String[]{"", "", "", "", ""};

         for (char var5 : var0.toCharArray()) {
            String[] var6 = ASCII_LETTERS.get(var5);
            if (var6 != null) {
               for (int var7 = 0; var7 < 5; var7++) {
                  var1[var7] = var1[var7] + var6[var7];
               }
            }
         }

         return var1;
      } else {
         return new String[0];
      }
   }

   public static String generateString(String var0) {
      String[] var1 = generate(var0);
      return String.join("\n", var1);
   }

   static {
      ASCII_LETTERS.put('A', new String[]{"    _    ", "   / \\   ", "  / _ \\  ", " / ___ \\ ", "/_/   \\_\\"});
      ASCII_LETTERS.put('B', new String[]{" ____  ", "|  _ \\ ", "| |_) |", "|  _ < ", "|_| \\_\\"});
      ASCII_LETTERS.put('C', new String[]{"  ____ ", " / ___|", "| |    ", "| |___ ", " \\____|"});
      ASCII_LETTERS.put('D', new String[]{" ____  ", "|  _ \\ ", "| | | |", "| |_| |", "|____/ "});
      ASCII_LETTERS.put('E', new String[]{" _____ ", "| ____|", "|  _|  ", "| |___ ", "|_____|"});
      ASCII_LETTERS.put('F', new String[]{" _____ ", "|  ___|", "| |_   ", "|  _|  ", "|_|    "});
      ASCII_LETTERS.put('G', new String[]{"  ____ ", " / ___|", "| |  _ ", "| |_| |", " \\____|"});
      ASCII_LETTERS.put('H', new String[]{" _   _ ", "| | | |", "| |_| |", "|  _  |", "|_| |_|"});
      ASCII_LETTERS.put('I', new String[]{" ___ ", "|_ _|", " | | ", " | | ", "|___|"});
      ASCII_LETTERS.put('J', new String[]{"     _ ", "    | |", " _  | |", "| |_| |", " \\___/ "});
      ASCII_LETTERS.put('K', new String[]{" _  __", "| |/ /", "| ' / ", "| . \\ ", "|_|\\_\\"});
      ASCII_LETTERS.put('L', new String[]{" _     ", "| |    ", "| |    ", "| |___ ", "|_____|"});
      ASCII_LETTERS.put('M', new String[]{" __  __ ", "|  \\/  |", "| |\\/| |", "| |  | |", "|_|  |_|"});
      ASCII_LETTERS.put('N', new String[]{" _   _ ", "| \\ | |", "|  \\| |", "| |\\  |", "|_| \\_|"});
      ASCII_LETTERS.put('O', new String[]{"  ___  ", " / _ \\ ", "| | | |", "| |_| |", " \\___/ "});
      ASCII_LETTERS.put('P', new String[]{" ____  ", "|  _ \\ ", "| |_) |", "|  __/ ", "|_|    "});
      ASCII_LETTERS.put('Q', new String[]{"  ___  ", " / _ \\ ", "| | | |", "| |_| |", " \\__\\_\\"});
      ASCII_LETTERS.put('R', new String[]{" ____  ", "|  _ \\ ", "| |_) |", "|  _ < ", "|_| \\_\\"});
      ASCII_LETTERS.put('S', new String[]{" ____  ", "/ ___| ", "\\___ \\ ", " ___) |", "|____/ "});
      ASCII_LETTERS.put('T', new String[]{" _____ ", "|_   _|", "  | |  ", "  | |  ", "  |_|  "});
      ASCII_LETTERS.put('U', new String[]{" _   _ ", "| | | |", "| | | |", "| |_| |", " \\___/ "});
      ASCII_LETTERS.put('V', new String[]{"__     __", "\\ \\   / /", " \\ \\ / / ", "  \\ V /  ", "   \\_/   "});
      ASCII_LETTERS.put('W', new String[]{"__        __", "\\ \\      / /", " \\ \\ /\\ / / ", "  \\ V  V /  ", "   \\_/\\_/   "});
      ASCII_LETTERS.put('X', new String[]{"__  __", "\\ \\/ /", " \\  / ", " /  \\ ", "/_/\\_\\"});
      ASCII_LETTERS.put('Y', new String[]{"__   __", "\\ \\ / /", " \\ V / ", "  | |  ", "  |_|  "});
      ASCII_LETTERS.put('Z', new String[]{" _____", "|__  /", "  / / ", " / /_ ", "/____|"});
      ASCII_LETTERS.put(' ', new String[]{"   ", "   ", "   ", "   ", "   "});
   }
}
