package com.lime.practicebot.api;

public interface PracticeBotApi {
    boolean isReady();
    BotHandle spawnDuelBot(BotSpawnRequest request);
}
