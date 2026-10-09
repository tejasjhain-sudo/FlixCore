package org.lime.swiftCore.api;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

public interface SwiftCoreApi {
   default int getApiVersion() {
      return 3;
   }

   Connection getConnection() throws SQLException;

   File getDataFolder();

   String getTierNameByPoints(int var1);

   String getKitTierNameByPoints(String var1, int var2);

   String resolveName(UUID var1);

   String getActiveKitName(UUID var1);

   boolean hasKitRule(String var1, String var2);

   CombatContext getCombatContext(UUID var1);

   HunterTroopsContext getHunterTroopsContext(UUID var1);

   boolean isHunterTroopsKit(String var1);

   void resolveHunterTroopsEventFight(UUID var1, UUID var2);

   ColorPartyContext getColorPartyContext(UUID var1);

   boolean isColorPartyKit(String var1);

   boolean isColorPartyPlayer(UUID var1);

   void eliminateColorPartyPlayer(UUID var1);

   void resolveColorPartyEventWinner(UUID var1);

   ChickenHuntContext getChickenHuntContext(UUID var1);

   boolean isChickenHuntKit(String var1);

   default boolean doesArenaExist(String var1) {
      return false;
   }

   void resolveChickenHuntIndividualRound(UUID var1, UUID var2);

   void resolveChickenHuntTeamRound(UUID var1, String var2);

   default String getPartyTeamName(UUID var1) {
      return "none";
   }

   default boolean isPlayerInPartyTeam(UUID var1, String var2) {
      return false;
   }

   default boolean arePlayersInSamePartyTeam(UUID var1, UUID var2) {
      return false;
   }

   void clearSpawnItemTracking(UUID var1);
}
