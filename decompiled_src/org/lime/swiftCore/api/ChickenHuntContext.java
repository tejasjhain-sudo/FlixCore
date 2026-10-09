package org.lime.swiftCore.api;

import java.util.List;
import java.util.UUID;

public record ChickenHuntContext(
   String key,
   ChickenHuntContext.Mode mode,
   String partyType,
   String kitName,
   String arenaName,
   int currentRound,
   int totalRounds,
   boolean countdown,
   List<UUID> players,
   List<UUID> team1,
   List<UUID> team2
) {
   public static enum Mode {
      DUEL,
      PARTY;
   }
}
