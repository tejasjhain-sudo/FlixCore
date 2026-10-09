package org.lime.swiftCore.e;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.lime.swiftCore.SwiftCore;

public class g implements CommandExecutor, TabCompleter {
   private final SwiftCore o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;

   public g(SwiftCore var1) {
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = var1;
   }

   public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
      if (var1 instanceof Player var5) {
         if (!var5.hasPermission("swiftcore.matchhistory")) {
            this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
               .getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var5, "no-permission"
               );
            return true;
         } else {
            UUID var6;
            String var7;
            if (var4.length >= 1) {
               Player var8 = Bukkit.getPlayer(var4[0]);
               if (var8 != null && var8.isOnline()) {
                  var6 = var8.getUniqueId();
                  var7 = var8.getName();
               } else {
                  OfflinePlayer var9 = Bukkit.getOfflinePlayer(var4[0]);
                  if (!var9.hasPlayedBefore() && !var9.isOnline()) {
                     this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
                        .getMessagesManager()
                        .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                           var5, "player-not-found"
                        );
                     return true;
                  }

                  var6 = var9.getUniqueId();
                  var7 = var9.getName() != null ? var9.getName() : var4[0];
               }
            } else {
               var6 = var5.getUniqueId();
               var7 = var5.getName();
            }

            this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
               .getMatchHistoryGUI()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var5, var6, var7
               );
            return true;
         }
      } else {
         var1.sendMessage("Only players can use this command.");
         return true;
      }
   }

   public List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4) {
      return (List<String>)(var4.length == 1
         ? Bukkit.getOnlinePlayers()
            .stream()
            .<String>map(Player::getName)
            .filter(var1x -> var1x.toLowerCase().startsWith(var4[0].toLowerCase()))
            .collect(Collectors.toList())
         : new ArrayList<>());
   }
}
