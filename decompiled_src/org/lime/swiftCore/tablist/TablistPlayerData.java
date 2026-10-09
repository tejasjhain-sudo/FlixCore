package org.lime.swiftCore.tablist;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;

public class TablistPlayerData {
   private final UUID playerId;
   private TablistContext context;
   private final Map<String, String> placeholders;
   private final Map<String, TablistPlayerData.CachedValue> papiCache;
   private long lastUpdate;
   private String previousHeader;
   private String previousFooter;
   private Component previousHeaderComponent;
   private Component previousFooterComponent;
   private String cachedWorldName;
   private boolean dirty;
   private boolean nameDirty;
   private long nextUpdateAt;
   private int animationInterval;
   private int lastHeaderFooterSignature;
   private int lastHeaderFooterSignatureTick;
   private final Map<String, Component> renderedComponentCache;
   private static final long PAPI_CACHE_DURATION_MS = 500L;
   private static final int RENDERED_COMPONENT_CACHE_SIZE = 64;

   public TablistPlayerData(UUID var1) {
      this.playerId = var1;
      this.context = TablistContext.DEFAULT;
      this.placeholders = new ConcurrentHashMap<>();
      this.papiCache = new ConcurrentHashMap<>();
      this.renderedComponentCache = new LinkedHashMap<String, Component>(64, 0.75F, true) {
         @Override
         protected boolean removeEldestEntry(Entry<String, Component> var1) {
            return this.size() > 64;
         }
      };
      this.lastUpdate = 0L;
      this.previousHeader = "";
      this.previousFooter = "";
      this.previousHeaderComponent = null;
      this.previousFooterComponent = null;
      this.cachedWorldName = null;
      this.dirty = true;
      this.nameDirty = true;
      this.nextUpdateAt = 0L;
      this.animationInterval = 0;
      this.lastHeaderFooterSignatureTick = Integer.MIN_VALUE;
   }

   public UUID getPlayerId() {
      return this.playerId;
   }

   public TablistContext getContext() {
      return this.context;
   }

   public void setContext(TablistContext var1) {
      this.context = var1;
      this.dirty = true;
      this.invalidateHeaderFooterSignature();
   }

   public String getPlaceholder(String var1) {
      return this.placeholders.get(var1);
   }

   public void setPlaceholder(String var1, String var2) {
      this.placeholders.put(var1, var2);
      this.dirty = true;
   }

   public void removePlaceholder(String var1) {
      this.placeholders.remove(var1);
      this.dirty = true;
   }

   public void clearPlaceholders() {
      this.placeholders.clear();
      this.dirty = true;
   }

   public Map<String, String> getPlaceholders() {
      return this.placeholders;
   }

   public long getLastUpdate() {
      return this.lastUpdate;
   }

   public void setLastUpdate(long var1) {
      this.lastUpdate = var1;
   }

   public String getPreviousHeader() {
      return this.previousHeader;
   }

   public void setPreviousHeader(String var1) {
      this.previousHeader = var1;
   }

   public Component getPreviousHeaderComponent() {
      return this.previousHeaderComponent;
   }

   public void setPreviousHeaderComponent(Component var1) {
      this.previousHeaderComponent = var1;
   }

   public String getPreviousFooter() {
      return this.previousFooter;
   }

   public void setPreviousFooter(String var1) {
      this.previousFooter = var1;
   }

   public Component getPreviousFooterComponent() {
      return this.previousFooterComponent;
   }

   public void setPreviousFooterComponent(Component var1) {
      this.previousFooterComponent = var1;
   }

   public synchronized Component getCachedRenderedComponent(String var1) {
      return var1 == null ? null : this.renderedComponentCache.get(var1);
   }

   public synchronized void cacheRenderedComponent(String var1, Component var2) {
      if (var1 != null && var2 != null) {
         this.renderedComponentCache.put(var1, var2);
      }
   }

   public synchronized void clearRenderedComponentCache() {
      this.renderedComponentCache.clear();
      this.previousHeaderComponent = null;
      this.previousFooterComponent = null;
   }

   synchronized int getRenderedComponentCacheSize() {
      return this.renderedComponentCache.size();
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

   public boolean wasHeaderFooterProcessed(int var1, int var2) {
      return this.lastHeaderFooterSignatureTick == var1 && this.lastHeaderFooterSignature == var2;
   }

   public void recordHeaderFooterProcessed(int var1, int var2) {
      this.lastHeaderFooterSignatureTick = var1;
      this.lastHeaderFooterSignature = var2;
   }

   public void invalidateHeaderFooterSignature() {
      this.lastHeaderFooterSignatureTick = Integer.MIN_VALUE;
   }

   public boolean isNameDirty() {
      return this.nameDirty;
   }

   public void setNameDirty(boolean var1) {
      this.nameDirty = var1;
   }

   public void markNameDirty() {
      this.nameDirty = true;
   }

   public long getNextUpdateAt() {
      return this.nextUpdateAt;
   }

   public void setNextUpdateAt(long var1) {
      this.nextUpdateAt = var1;
   }

   public int getAnimationInterval() {
      return this.animationInterval;
   }

   public void setAnimationInterval(int var1) {
      this.animationInterval = var1;
   }

   public String getCachedPapi(String var1) {
      TablistPlayerData.CachedValue var2 = this.papiCache.get(var1);
      return var2 != null && !var2.isExpired() ? var2.value : null;
   }

   public void setCachedPapi(String var1, String var2) {
      this.papiCache.put(var1, new TablistPlayerData.CachedValue(var2, System.currentTimeMillis() + 500L));
   }

   public void clearPapiCache() {
      this.papiCache.clear();
   }

   public void clearExpiredPapi() {
      this.papiCache.entrySet().removeIf(var0 -> var0.getValue().isExpired());
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
