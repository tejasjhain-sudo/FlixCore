package org.lime.swiftCore.scoreboard;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;

public class PlayerScoreboardData {
   private final UUID playerId;
   private ScoreboardState state;
   private SwiftBoard board;
   private final Map<String, String> placeholders;
   private final Map<String, PlayerScoreboardData.CachedValue> papiCache;
   private long lastUpdate;
   private long lastUpdateTick;
   private int previousLinesHash;
   private int previousTitleHash;
   private String cachedWorldName;
   private volatile boolean dirty;
   private List<String> previousLineStrings;
   private List<Component> previousLineComponents;
   private static final long PAPI_CACHE_DURATION_MS = 500L;
   private static final long PAPI_CACHE_SLOW_MS = 2000L;
   private static final long PAPI_CACHE_FAST_MS = 200L;

   public PlayerScoreboardData(UUID var1) {
      this.playerId = var1;
      this.state = ScoreboardState.DEFAULT;
      this.placeholders = new ConcurrentHashMap<>();
      this.papiCache = new ConcurrentHashMap<>();
      this.lastUpdate = 0L;
      this.lastUpdateTick = 0L;
      this.previousLinesHash = 0;
      this.previousTitleHash = 0;
      this.cachedWorldName = null;
      this.dirty = false;
   }

   public UUID getPlayerId() {
      return this.playerId;
   }

   public ScoreboardState getState() {
      return this.state;
   }

   public void setState(ScoreboardState var1) {
      if (this.state != var1) {
         this.dirty = true;
      }

      this.state = var1;
   }

   public SwiftBoard getBoard() {
      return this.board;
   }

   public void setBoard(SwiftBoard var1) {
      this.board = var1;
   }

   public Map<String, String> getPlaceholders() {
      return this.placeholders;
   }

   public boolean setPlaceholder(String var1, String var2) {
      String var3 = this.placeholders.put(var1, var2);
      boolean var4 = !Objects.equals(var3, var2);
      if (var4) {
         this.dirty = true;
      }

      return var4;
   }

   public String getPlaceholder(String var1) {
      return this.placeholders.getOrDefault(var1, "");
   }

   public long getLastUpdate() {
      return this.lastUpdate;
   }

   public void setLastUpdate(long var1) {
      this.lastUpdate = var1;
   }

   public long getLastUpdateTick() {
      return this.lastUpdateTick;
   }

   public void setLastUpdateTick(long var1) {
      this.lastUpdateTick = var1;
   }

   public String getCachedPapi(String var1) {
      PlayerScoreboardData.CachedValue var2 = this.papiCache.get(var1);
      return var2 != null && !var2.isExpired() ? var2.value : null;
   }

   public void setCachedPapi(String var1, String var2) {
      this.papiCache.put(var1, new PlayerScoreboardData.CachedValue(var2, System.currentTimeMillis() + getPlaceholderTtl(var1)));
   }

   private static long getPlaceholderTtl(String var0) {
      if (var0.contains("player_health") || var0.contains("player_food")) {
         return 200L;
      } else {
         return !var0.contains("server_tps")
               && !var0.contains("server_online")
               && !var0.contains("server_max")
               && !var0.contains("luckperms_")
               && !var0.contains("player_world")
               && !var0.contains("player_gamemode")
            ? 500L
            : 2000L;
      }
   }

   public void clearPapiCache() {
      this.papiCache.clear();
   }

   public void clearExpiredPapi() {
      this.papiCache.entrySet().removeIf(var0 -> var0.getValue().isExpired());
   }

   public int getPreviousLinesHash() {
      return this.previousLinesHash;
   }

   public void setPreviousLinesHash(int var1) {
      this.previousLinesHash = var1;
   }

   public int getPreviousTitleHash() {
      return this.previousTitleHash;
   }

   public void setPreviousTitleHash(int var1) {
      this.previousTitleHash = var1;
   }

   public String getCachedWorldName() {
      return this.cachedWorldName;
   }

   public void setCachedWorldName(String var1) {
      this.cachedWorldName = var1;
   }

   public boolean isDirty() {
      return this.dirty;
   }

   public void setDirty(boolean var1) {
      this.dirty = var1;
   }

   public void markDirty() {
      this.dirty = true;
   }

   public void clearDirty() {
      this.dirty = false;
   }

   public void resetHashes() {
      this.previousLinesHash = Integer.MIN_VALUE;
      this.previousTitleHash = Integer.MIN_VALUE;
      this.previousLineStrings = null;
      this.previousLineComponents = null;
   }

   public List<String> getPreviousLineStrings() {
      return this.previousLineStrings;
   }

   public List<Component> getPreviousLineComponents() {
      return this.previousLineComponents;
   }

   public void setPreviousLines(List<String> var1, List<Component> var2) {
      this.previousLineStrings = var1;
      this.previousLineComponents = var2;
   }

   private static class CachedValue {
      final String value;
      final long expiresAt;

      CachedValue(String var1, long var2) {
         this.value = var1;
         this.expiresAt = var2;
      }

      boolean isExpired() {
         return System.currentTimeMillis() > this.expiresAt;
      }
   }
}
