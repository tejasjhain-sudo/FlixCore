package com.lime.practicebot.api;

import org.bukkit.entity.Player;
import java.util.UUID;

public interface BotHandle {
    UUID getUniqueId();
    Player getPlayer();
    void despawn();
}
