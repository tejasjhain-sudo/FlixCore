package org.lime.swiftCore.kit;

import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.lime.swiftCore.SwiftCore;

public final class ColorPartyKitGuard {
   private ColorPartyKitGuard() {
   }

   public static boolean isColorPartyKit(SwiftCore var0, String var1) {
      return var0 != null && var1 != null && !var1.isBlank() && var0.getDataManager() != null
         ? var0.getDataManager()
            .Õ000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000class(
               var1
            )
            .getOrDefault(KitRule.COLOR_PARTY, KitRule.COLOR_PARTY.getDefaultValue())
         : false;
   }

   public static boolean blockQueueUse(SwiftCore var0, Player var1, String var2) {
      return block(var0, var1, var2, "color-party-kit-queue-blocked");
   }

   public static boolean blockRankedQueueUse(SwiftCore var0, Player var1, String var2) {
      return block(var0, var1, var2, "color-party-kit-ranked-queue-blocked");
   }

   public static boolean blockDuelUse(SwiftCore var0, Player var1, String var2) {
      return block(var0, var1, var2, "color-party-kit-duel-blocked");
   }

   public static boolean blockTournamentUse(SwiftCore var0, Player var1, String var2) {
      return block(var0, var1, var2, "color-party-kit-tournament-blocked");
   }

   public static boolean blockPartyUse(SwiftCore var0, Player var1, String var2) {
      return block(var0, var1, var2, "color-party-kit-party-blocked");
   }

   public static boolean blockEventUse(SwiftCore var0, Player var1, String var2) {
      return block(var0, var1, var2, "color-party-kit-event-blocked");
   }

   public static boolean blockFfaUse(SwiftCore var0, Player var1, String var2) {
      return block(var0, var1, var2, "color-party-kit-ffa-blocked");
   }

   public static boolean isActiveColorPartyEvent(SwiftCore var0, UUID var1) {
      return getActiveColorPartyKitName(var0, var1) != null;
   }

   public static String getActiveColorPartyKitName(SwiftCore var0, UUID var1) {
      if (var0 != null && var1 != null && var0.getPartyGameManager() != null) {
         org.lime.swiftCore.party.d var2 = var0.getPartyGameManager().getPlayerActiveGame(var1);
         return var2 != null
               && var2.Ö000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000return()
                  == org.lime.swiftCore.party.e.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new
               && !var2.Oo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000returnsuper()
                  .contains(var1)
               && !var2.õo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000publicsuper()
                  .contains(var1)
               && isColorPartyKit(
                  var0,
                  var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
               )
            ? var2.Ò000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000new()
            : null;
      } else {
         return null;
      }
   }

   private static boolean block(SwiftCore var0, Player var1, String var2, String var3) {
      if (!isColorPartyKit(var0, var2)) {
         return false;
      } else {
         if (var1 != null) {
            String var4 = var0.getDataManager()
               .ô000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000null(
                  var2
               );
            var0.getMessagesManager()
               .o000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000super(
                  var1, var3, Map.of("kit", var4)
               );
         }

         return true;
      }
   }
}
