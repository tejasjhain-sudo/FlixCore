package org.lime.swiftCore.tablist;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lime.swiftCore.SwiftCore;

public class LuckPermsListener {
   private final SwiftCore plugin;
   private final TablistManager manager;
   private final Map<UUID, LuckPermsListener.RankData> rankDataCache = new ConcurrentHashMap<>();
   private EventSubscription<?> subscription;
   private boolean enabled = false;

   public LuckPermsListener(SwiftCore var1, TablistManager var2) {
      this.plugin = var1;
      this.manager = var2;
   }

   public boolean setup() {
      if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
         return false;
      } else {
         try {
            LuckPerms var1 = LuckPermsProvider.get();
            this.subscription = var1.getEventBus().subscribe(this.plugin, UserDataRecalculateEvent.class, this::onUserDataRecalculate);
            this.enabled = true;

            for (Player var3 : Bukkit.getOnlinePlayers()) {
               this.cachePlayerRank(var3.getUniqueId());
            }

            return true;
         } catch (Exception var4) {
            this.plugin.getLogger().warning("Failed to setup LuckPerms listener: " + var4.getMessage());
            return false;
         }
      }
   }

   private void onUserDataRecalculate(UserDataRecalculateEvent var1) {
      UUID var2 = var1.getUser().getUniqueId();
      User var3 = var1.getUser();

      String var4;
      String var5;
      String var6;
      try {
         CachedMetaData var7 = var3.getCachedData().getMetaData();
         var4 = this.safePrimaryGroup(var3);
         var5 = var7.getPrefix() != null ? var7.getPrefix() : "";
         var6 = var7.getSuffix() != null ? var7.getSuffix() : "";
      } catch (Exception var13) {
         return;
      }

      int var15 = this.getWeightFromLuckPerms(var4);
      LuckPermsListener.RankData var8 = new LuckPermsListener.RankData(var4, var5, var6, var15, System.currentTimeMillis());
      boolean var9;
      synchronized (this.rankDataCache) {
         LuckPermsListener.RankData var11 = this.rankDataCache.get(var2);
         var9 = var11 == null
            || !var11.group().equals(var8.group())
            || !var11.prefix().equals(var8.prefix())
            || !var11.suffix().equals(var8.suffix())
            || var11.priority() != var8.priority();
         if (var9) {
            this.rankDataCache.put(var2, var8);
         }
      }

      if (var9) {
         PlaceholderCache var16 = this.manager.getPlaceholderCache();
         if (var16 != null) {
            var16.invalidateEventPlaceholders(var2);
         }

         Bukkit.getScheduler().runTask(this.plugin, () -> {
            Player var2x = Bukkit.getPlayer(var2);
            if (var2x != null && var2x.isOnline()) {
               this.manager.onRankChange(var2x);
            }
         });
      }
   }

   public void cachePlayerRank(UUID var1) {
      if (this.enabled) {
         try {
            LuckPerms var2 = LuckPermsProvider.get();
            User var3 = var2.getUserManager().getUser(var1);
            if (var3 == null) {
               return;
            }

            CachedMetaData var4 = var3.getCachedData().getMetaData();
            String var5 = this.safePrimaryGroup(var3);
            String var6 = var4.getPrefix() != null ? var4.getPrefix() : "";
            String var7 = var4.getSuffix() != null ? var4.getSuffix() : "";
            int var8 = this.getWeightFromLuckPerms(var5);
            this.rankDataCache.put(var1, new LuckPermsListener.RankData(var5, var6, var7, var8, System.currentTimeMillis()));
         } catch (Exception var9) {
            this.plugin.getLogger().warning("Failed to cache rank for " + var1 + ": " + var9.getMessage());
         }
      }
   }

   public LuckPermsListener.RankData getRankData(UUID var1) {
      return this.rankDataCache.get(var1);
   }

   public String getPrefix(UUID var1) {
      LuckPermsListener.RankData var2 = this.rankDataCache.get(var1);
      return var2 != null ? var2.prefix() : "";
   }

   public String getSuffix(UUID var1) {
      LuckPermsListener.RankData var2 = this.rankDataCache.get(var1);
      return var2 != null ? var2.suffix() : "";
   }

   public String getGroup(UUID var1) {
      LuckPermsListener.RankData var2 = this.rankDataCache.get(var1);
      return var2 != null ? var2.group() : "default";
   }

   public int getPriority(UUID var1) {
      LuckPermsListener.RankData var2 = this.rankDataCache.get(var1);
      return var2 != null ? var2.priority() : Integer.MAX_VALUE;
   }

   private int getWeightFromLuckPerms(String var1) {
      try {
         if (var1 == null) {
            return 9999;
         } else {
            Group var2 = LuckPermsProvider.get().getGroupManager().getGroup(var1);
            if (var2 == null) {
               return 9999;
            } else {
               int var3 = var2.getWeight().orElse(0);
               return Math.max(0, 1000 - var3);
            }
         }
      } catch (Exception var4) {
         return 9999;
      }
   }

   private String safePrimaryGroup(User var1) {
      try {
         String var2 = var1.getPrimaryGroup();
         return var2 != null ? var2 : "default";
      } catch (IllegalStateException | NullPointerException var3) {
         return "default";
      }
   }

   public void invalidate(UUID var1) {
      this.rankDataCache.remove(var1);
   }

   public void shutdown() {
      if (this.subscription != null) {
         this.subscription.close();
      }

      this.rankDataCache.clear();
      this.enabled = false;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public static record RankData(String group, String prefix, String suffix, int priority, long timestamp) {
   }
}
