package org.lime.swiftCore.scoreboard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lime.swiftCore.SwiftCore;

public class AnimationManager {
   private final SwiftCore plugin;
   private final Map<String, AnimationManager.AnimationData> animations;
   private FileConfiguration animationConfig;
   private final long startTime;
   private static final Pattern HEX_PATTERN_1 = Pattern.compile("&x(&[0-9a-fA-F]){6}");
   private static final Pattern HEX_PATTERN_2 = Pattern.compile("&#([0-9a-fA-F]{6})");

   public AnimationManager(SwiftCore var1) {
      this.plugin = var1;
      this.animations = new HashMap<>();
      this.startTime = System.currentTimeMillis();
      this.loadAnimationConfig();
      this.loadAnimations();
   }

   private void loadAnimationConfig() {
      File var1 = new File(this.plugin.getDataFolder(), "animation.yml");
      if (!var1.exists()) {
         try {
            Files.createDirectories(var1.getParentFile().toPath());

            try (InputStream var2 = this.plugin.getResource("animation.yml")) {
               if (var2 != null) {
                  Files.copy(var2, var1.toPath());
               }
            }
         } catch (IOException var7) {
            this.plugin.getLogger().severe("Failed to save animation.yml: " + var7.getMessage());
         }
      }

      this.animationConfig = YamlConfiguration.loadConfiguration(var1);
   }

   private void loadAnimations() {
      if (this.animationConfig != null) {
         for (String var2 : this.animationConfig.getKeys(false)) {
            List var3 = this.animationConfig.getStringList(var2 + ".texts");
            int var4 = this.animationConfig.getInt(var2 + ".change-interval", 200);
            if (!var3.isEmpty()) {
               ArrayList var5 = new ArrayList(var3.size());

               for (String var7 : var3) {
                  var5.add(this.colorize(var7));
               }

               this.animations.put(var2, new AnimationManager.AnimationData(var5, var4));
            }
         }
      }
   }

   private String colorize(String var1) {
      Matcher var2 = HEX_PATTERN_1.matcher(var1);
      StringBuffer var3 = new StringBuffer();

      while (var2.find()) {
         String var4 = var2.group().replace("&", "");
         StringBuilder var5 = new StringBuilder("§x");

         for (char var9 : var4.toCharArray()) {
            if (var9 != 'x') {
               var5.append('§').append(var9);
            }
         }

         var2.appendReplacement(var3, var5.toString());
      }

      var2.appendTail(var3);
      var1 = var3.toString();
      Matcher var14 = HEX_PATTERN_2.matcher(var1);
      StringBuffer var15 = new StringBuffer();

      while (var14.find()) {
         String var16 = var14.group(1);
         StringBuilder var18 = new StringBuilder("§x");

         for (char var11 : var16.toCharArray()) {
            var18.append('§').append(var11);
         }

         var14.appendReplacement(var15, var18.toString());
      }

      var14.appendTail(var15);
      var1 = var15.toString();
      char[] var17 = var1.toCharArray();
      StringBuilder var19 = new StringBuilder();

      for (int var21 = 0; var21 < var17.length; var21++) {
         if (var17[var21] == '&' && var21 + 1 < var17.length) {
            char var23 = var17[var21 + 1];
            if (this.isValidColorCode(var23)) {
               var19.append('§').append(var23);
               var21++;
               continue;
            }
         }

         var19.append(var17[var21]);
      }

      return var19.toString();
   }

   private boolean isValidColorCode(char var1) {
      return var1 >= '0' && var1 <= '9'
         || var1 >= 'a' && var1 <= 'f'
         || var1 >= 'A' && var1 <= 'F'
         || var1 == 'r'
         || var1 == 'l'
         || var1 == 'm'
         || var1 == 'n'
         || var1 == 'o'
         || var1 == 'k'
         || var1 == 'x';
   }

   public String getAnimationFrame(String var1) {
      AnimationManager.AnimationData var2 = this.animations.get(var1);
      if (var2 == null) {
         return "%animation:" + var1 + "%";
      } else {
         long var3 = System.currentTimeMillis() - this.startTime;
         int var5 = (int)(var3 % (long)(var2.texts.size() * var2.interval) / (long)var2.interval);
         return var2.texts.get(var5);
      }
   }

   public long getLoopTime() {
      return System.currentTimeMillis() - this.startTime;
   }

   public long getStartTime() {
      return this.startTime;
   }

   public boolean hasAnimation(String var1) {
      return this.animations.containsKey(var1);
   }

   public int getAnimationInterval(String var1) {
      AnimationManager.AnimationData var2 = this.animations.get(var1);
      return var2 != null ? var2.interval : 0;
   }

   public void reload() {
      this.animations.clear();
      this.loadAnimationConfig();
      this.loadAnimations();
   }

   public Set<String> getAllAnimationNames() {
      return Collections.unmodifiableSet(this.animations.keySet());
   }

   private static class AnimationData {
      private final List<String> texts;
      private final int interval;

      public AnimationData(List<String> var1, int var2) {
         this.texts = var1;
         this.interval = var2;
      }
   }
}
