package org.lime.swiftCore.libs.lightcore.api.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CommandDispatchUtil {
   private CommandDispatchUtil() {
   }

   @NotNull
   public static CommandDispatchUtil.Builder command(@NotNull String var0) {
      return new CommandDispatchUtil.Builder(var0);
   }

   public static void console(@NotNull String var0) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), var0);
   }

   public static void player(@NotNull Player var0, @NotNull String var1) {
      var0.performCommand(var1);
   }

   public static final class Builder {
      private final String template;
      private boolean console = false;
      private long delay = 0L;

      private Builder(@NotNull String var1) {
         this.template = var1;
      }

      @NotNull
      public CommandDispatchUtil.Builder asConsole() {
         this.console = true;
         return this;
      }

      @NotNull
      public CommandDispatchUtil.Builder asPlayer() {
         this.console = false;
         return this;
      }

      @NotNull
      public CommandDispatchUtil.Builder withDelay(long var1) {
         this.delay = var1;
         return this;
      }

      public void execute(@NotNull Player var1) {
         String var2 = this.template.replace("<player>", var1.getName());
         Runnable var3 = () -> {
            if (this.console) {
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), var2);
            } else {
               var1.performCommand(var2);
            }
         };
         if (this.delay > 0L) {
            SchedulerUtil.syncLater(var3, this.delay);
         } else {
            SchedulerUtil.sync(var3);
         }
      }

      public void execute() {
         Runnable var1 = () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), this.template);
         if (this.delay > 0L) {
            SchedulerUtil.syncLater(var1, this.delay);
         } else {
            SchedulerUtil.sync(var1);
         }
      }
   }
}
