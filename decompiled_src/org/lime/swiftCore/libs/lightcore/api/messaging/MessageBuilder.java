package org.lime.swiftCore.libs.lightcore.api.messaging;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageBuilder {
   private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
   private CommandSender target;
   private String message;
   private String[] placeholders = new String[0];
   private String titleMain;
   private String titleSub;
   private int fadeIn = 10;
   private int stay = 70;
   private int fadeOut = 20;
   private String bossbarTitle;
   private float bossbarProgress = 1.0F;
   private Color bossbarColor = Color.GREEN;
   private Overlay bossbarOverlay = Overlay.PROGRESS;
   private int bossbarDuration = 100;
   private JavaPlugin plugin;
   private Sound sound;
   private float volume = 1.0F;
   private float pitch = 1.0F;

   private MessageBuilder() {
   }

   @NotNull
   public static MessageBuilder create() {
      return new MessageBuilder();
   }

   @NotNull
   public MessageBuilder to(@NotNull CommandSender var1) {
      this.target = var1;
      return this;
   }

   @NotNull
   public MessageBuilder message(@NotNull String var1) {
      this.message = var1;
      return this;
   }

   @NotNull
   public MessageBuilder placeholders(String... var1) {
      this.placeholders = var1;
      return this;
   }

   @NotNull
   public MessageBuilder title(@NotNull String var1) {
      this.titleMain = var1;
      return this;
   }

   @NotNull
   public MessageBuilder title(@NotNull String var1, @Nullable String var2) {
      this.titleMain = var1;
      this.titleSub = var2;
      return this;
   }

   @NotNull
   public MessageBuilder titleTimes(int var1, int var2, int var3) {
      this.fadeIn = var1;
      this.stay = var2;
      this.fadeOut = var3;
      return this;
   }

   @NotNull
   public MessageBuilder bossbar(@NotNull String var1) {
      this.bossbarTitle = var1;
      return this;
   }

   @NotNull
   public MessageBuilder bossbarProgress(float var1) {
      this.bossbarProgress = Math.max(0.0F, Math.min(1.0F, var1));
      return this;
   }

   @NotNull
   public MessageBuilder bossbarColor(@NotNull Color var1) {
      this.bossbarColor = var1;
      return this;
   }

   @NotNull
   public MessageBuilder bossbarColor(@NotNull String var1) {
      try {
         this.bossbarColor = Color.valueOf(var1.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException var3) {
      }

      return this;
   }

   @NotNull
   public MessageBuilder bossbarOverlay(@NotNull Overlay var1) {
      this.bossbarOverlay = var1;
      return this;
   }

   @NotNull
   public MessageBuilder bossbarDuration(int var1) {
      this.bossbarDuration = var1;
      return this;
   }

   @NotNull
   public MessageBuilder plugin(@NotNull JavaPlugin var1) {
      this.plugin = var1;
      return this;
   }

   @NotNull
   public MessageBuilder sound(@NotNull Sound var1) {
      this.sound = var1;
      return this;
   }

   @NotNull
   public MessageBuilder sound(@NotNull String var1) {
      try {
         this.sound = Sound.valueOf(var1.toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException var3) {
      }

      return this;
   }

   @NotNull
   public MessageBuilder volume(float var1) {
      this.volume = var1;
      return this;
   }

   @NotNull
   public MessageBuilder pitch(float var1) {
      this.pitch = var1;
      return this;
   }

   public void sendChat() {
      if (this.target != null && this.message != null) {
         this.target.sendMessage(this.colorize(this.format(this.message)));
      }
   }

   public void sendActionbar() {
      if (this.target instanceof Player var1) {
         if (this.message != null) {
            var1.sendActionBar(this.toComponent(this.format(this.message)));
         }
      }
   }

   public void sendTitle() {
      if (this.target instanceof Player var1) {
         String var3 = this.titleMain != null ? this.titleMain : this.message;
         if (var3 != null) {
            var1.showTitle(
               Title.title(
                  this.toComponent(this.format(var3)),
                  this.toComponent(this.format(this.titleSub != null ? this.titleSub : "")),
                  Times.times(Duration.ofMillis((long)this.fadeIn * 50L), Duration.ofMillis((long)this.stay * 50L), Duration.ofMillis((long)this.fadeOut * 50L))
               )
            );
         }
      }
   }

   public void sendBossbar() {
      if (this.target instanceof Player var1) {
         String var4 = this.bossbarTitle != null ? this.bossbarTitle : this.message;
         if (var4 != null) {
            BossBar var3 = BossBar.bossBar(this.toComponent(this.format(var4)), this.bossbarProgress, this.bossbarColor, this.bossbarOverlay);
            var1.showBossBar(var3);
            if (this.bossbarDuration > 0 && this.plugin != null) {
               Bukkit.getScheduler().runTaskLater(this.plugin, () -> var1.hideBossBar(var3), (long)this.bossbarDuration);
            }
         }
      }
   }

   public void playSound() {
      if (this.target instanceof Player var1) {
         if (this.sound != null) {
            var1.playSound(var1.getLocation(), this.sound, this.volume, this.pitch);
         }
      }
   }

   public void send(@NotNull MessageType var1) {
      switch (var1) {
         case CHAT:
            this.sendChat();
            break;
         case ACTIONBAR:
            this.sendActionbar();
            break;
         case TITLE:
            this.sendTitle();
            break;
         case BOSSBAR:
            this.sendBossbar();
      }
   }

   public void send(@NotNull String var1) {
      for (String var5 : var1.split(",")) {
         this.send(MessageType.get(var5.trim()));
      }
   }

   @NotNull
   private String format(@NotNull String var1) {
      if (var1.isEmpty()) {
         return var1;
      } else {
         String var2 = var1;
         if (this.placeholders != null && this.placeholders.length > 0) {
            for (byte var3 = 0; var3 + 1 < this.placeholders.length; var3 += 2) {
               var2 = var2.replace(this.placeholders[var3], this.placeholders[var3 + 1]);
            }
         }

         return var2;
      }
   }

   @NotNull
   private String colorize(@NotNull String var1) {
      Matcher var2 = HEX_PATTERN.matcher(var1);
      StringBuilder var3 = new StringBuilder();

      while (var2.find()) {
         String var4 = var2.group(1);
         var2.appendReplacement(var3, ChatColor.of("#" + var4).toString());
      }

      var2.appendTail(var3);
      return ChatColor.translateAlternateColorCodes('&', var3.toString());
   }

   @NotNull
   private Component toComponent(@NotNull String var1) {
      String var2 = this.colorize(var1);
      return LegacyComponentSerializer.legacySection().deserialize(var2);
   }
}
