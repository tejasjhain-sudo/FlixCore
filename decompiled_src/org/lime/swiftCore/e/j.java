package org.lime.swiftCore.e;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lime.swiftCore.SwiftCore;

public class j implements CommandExecutor {
   private final SwiftCore o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;

   public j(SwiftCore var1) {
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = var1;
   }

   public boolean onCommand(@NotNull CommandSender var1, @NotNull Command var2, @NotNull String var3, @NotNull String[] var4) {
      if (var1 instanceof Player var5) {
         if (!var5.hasPermission("swiftcore.admin")) {
            var5.sendMessage(Component.text("You don't have permission to use this command!").color(NamedTextColor.RED));
            return true;
         } else {
            this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
               .getLobbyManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var5.getLocation()
               );
            var5.sendMessage(Component.text("Lobby location set successfully!").color(NamedTextColor.GREEN));
            return true;
         }
      } else {
         var1.sendMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED));
         return true;
      }
   }
}
