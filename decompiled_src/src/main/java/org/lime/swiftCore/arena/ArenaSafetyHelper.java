package org.lime.swiftCore.arena;

import org.bukkit.Location;

/**
 * Safety helper to prevent false-positive arena boundary deaths.
 * Guarantees that any player near pos1, pos2, or centre is always treated
 * as inside the arena boundaries, even if the admin configured corner1/corner2
 * slightly too tight or inaccurately.
 */
public class ArenaSafetyHelper {

    public static boolean isInsideOrNearArena(d arena, Location loc) {
        if (loc == null || loc.getWorld() == null || arena == null) {
            return false;
        }
        try {
            Location pos1 = arena.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper();
            if (pos1 != null && loc.getWorld().equals(pos1.getWorld())) {
                if (loc.distanceSquared(pos1) <= 225.0) { // Within 15 blocks of spawn 1
                    return true;
                }
            }

            Location pos2 = arena.ôO00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000newsuper();
            if (pos2 != null && loc.getWorld().equals(pos2.getWorld())) {
                if (loc.distanceSquared(pos2) <= 225.0) { // Within 15 blocks of spawn 2
                    return true;
                }
            }

            Location centre = arena.öo00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000privatesuper();
            if (centre != null && loc.getWorld().equals(centre.getWorld())) {
                if (loc.distanceSquared(centre) <= 225.0) { // Within 15 blocks of centre
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
