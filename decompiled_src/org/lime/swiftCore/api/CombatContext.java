package org.lime.swiftCore.api;

public record CombatContext(
   CombatContext.Mode mode, String kitName, boolean active, boolean countdown, boolean spectator, boolean invulnerable, boolean legacyCombat
) {
   public boolean canFight() {
      return this.active && !this.countdown && !this.spectator && !this.invulnerable;
   }

   public static enum Mode {
      DUEL,
      PARTY,
      FFA,
      TOURNAMENT,
      EVENT;
   }
}
