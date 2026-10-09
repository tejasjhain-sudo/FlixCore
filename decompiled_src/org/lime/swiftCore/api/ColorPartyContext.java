package org.lime.swiftCore.api;

import java.util.List;
import java.util.UUID;

public record ColorPartyContext(
   String key,
   String eventId,
   String eventType,
   String eventState,
   String kitName,
   String arenaName,
   String worldName,
   int centerX,
   int centerY,
   int centerZ,
   int minX,
   int minY,
   int minZ,
   int maxX,
   int maxY,
   int maxZ,
   List<UUID> alivePlayers,
   List<UUID> spectators,
   UUID currentFighter1,
   UUID currentFighter2
) {
}
