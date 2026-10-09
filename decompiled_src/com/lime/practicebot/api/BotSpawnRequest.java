package com.lime.practicebot.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.function.Consumer;

public class BotSpawnRequest {
    public BotSpawnRequest(String name, Location location, Player opponent, BotMode mode, BotEquipmentSpec equipment, String kit, Consumer<BotHandle> callback) {
    }
}
