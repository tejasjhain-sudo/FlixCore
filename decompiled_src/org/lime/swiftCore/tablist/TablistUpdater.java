package org.lime.swiftCore.tablist;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.lime.swiftCore.SwiftCore;

public class TablistUpdater extends BukkitRunnable {
   private final SwiftCore plugin;
   private final TablistManager manager;
   private long lastPlayerNameUpdate;
   private int tickCounter = 0;
   private static final int HEADER_FOOTER_UPDATE_INTERVAL = 20;
   private static final long PLAYER_NAME_UPDATE_INTERVAL = 1000L;

   public TablistUpdater(SwiftCore var1, TablistManager var2, int var3) {
      this.plugin = var1;
      this.manager = var2;
      this.lastPlayerNameUpdate = 0L;
   }

   public void run() {
      long var1 = System.currentTimeMillis();
      this.tickCounter++;
      boolean var3 = var1 - this.lastPlayerNameUpdate >= 1000L;
      boolean var4 = this.tickCounter % 20 == 0;
      TablistRefreshCoordinator.Snapshot var5 = this.manager.drainRefreshRequests();

      for (UUID var7 : this.manager.getTrackedPlayerIdsView()) {
         Player var8 = Bukkit.getPlayer(var7);
         if (var8 == null) {
            this.cleanup(var7);
         } else if (var8.isOnline()) {
            TablistPlayerData var9 = this.manager.getData(var8);
            if (var9 != null) {
               if (var3 || var9.isNameDirty() || var5.identityTargets().contains(var7)) {
                  this.manager.updatePlayerTabName(var8);
                  this.manager.applyTeamSorting(var8);
                  var9.setNameDirty(false);
               }

               boolean var10 = var9.getAnimationInterval() > 0;
               boolean var11 = var5.headerFooterViewers().contains(var7);
               if (TablistUpdatePolicy.isDue(var9.isDirty(), var11, var10, var4, var1, var9.getNextUpdateAt())) {
                  this.manager.updateTablist(var8, false);
               }

               if ((var5.socialViewers().contains(var7) || var4) && var9.getContext() == TablistContext.LOBBY_FRIENDS) {
                  this.manager.refreshLobbySocialView(var8);
               }
            }
         }
      }

      if (var3) {
         this.lastPlayerNameUpdate = var1;
      }
   }

   public void start(int var1) {
      int var2 = this.getMinimumAnimationInterval();
      int var3 = var2 > 0 ? Math.max(1, var2 / 50) : Math.max(1, var1);
      this.runTaskTimer(this.plugin, (long)var3, (long)var3);
   }

   private int getMinimumAnimationInterval() {
      int var1 = Integer.MAX_VALUE;
      boolean var2 = false;

      for (TablistContext var6 : TablistContext.values()) {
         int var7 = this.manager.getProvider().getAnimationInterval(var6);
         if (var7 > 0) {
            var1 = Math.min(var1, var7);
            var2 = true;
         }
      }

      return var2 ? var1 : 0;
   }

   public void markPlayerNameDirty(UUID var1) {
      TablistPlayerData var2 = this.manager.getData(var1);
      if (var2 != null) {
         var2.setNameDirty(true);
      }
   }

   public void cleanup(UUID var1) {
   }
}
