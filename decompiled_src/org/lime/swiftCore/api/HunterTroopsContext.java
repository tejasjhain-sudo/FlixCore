package org.lime.swiftCore.api;

import java.util.List;
import java.util.UUID;

public record HunterTroopsContext(
   String key,
   HunterTroopsContext.Mode mode,
   String partyType,
   String kitName,
   List<UUID> players,
   List<UUID> team1,
   List<UUID> team2,
   UUID fighter1,
   UUID fighter2
) {
   public static enum Mode {
      PARTY,
      EVENT;
   }
}
