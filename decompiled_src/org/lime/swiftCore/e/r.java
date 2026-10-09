package org.lime.swiftCore.e;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.lime.swiftCore.SwiftCore;

public class r implements CommandExecutor, TabCompleter {
   private final SwiftCore o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super;

   public r(SwiftCore var1) {
      this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super = var1;
   }

   public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4) {
      if (var1 instanceof Player var5) {
         String var9 = var3.toLowerCase();
         long var6;
         String var8;
         switch (var9) {
            case "day":
               var6 = 1000L;
               var8 = "Day";
               break;
            case "night":
               var6 = 13000L;
               var8 = "Night";
               break;
            case "sunset":
               var6 = 12000L;
               var8 = "Sunset";
               break;
            default:
               var6 = 1000L;
               var8 = "Day";
         }

         var5.setPlayerTime(var6, false);
         this.o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super
            .getMessagesManager()
            .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
               var5, "environment-changed", Map.of("time", var8)
            );
         return true;
      } else {
         var1.sendMessage("Only players can use this command.");
         return true;
      }
   }

   public List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4) {
      return new ArrayList<>();
   }
}
