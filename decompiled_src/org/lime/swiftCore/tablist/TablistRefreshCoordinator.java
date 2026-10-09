package org.lime.swiftCore.tablist;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class TablistRefreshCoordinator {
   private final Set<UUID> headerFooterViewers = ConcurrentHashMap.newKeySet();
   private final Set<UUID> identityTargets = ConcurrentHashMap.newKeySet();
   private final Set<UUID> socialViewers = ConcurrentHashMap.newKeySet();

   void requestHeaderFooter(UUID var1) {
      if (var1 != null) {
         this.headerFooterViewers.add(var1);
      }
   }

   void requestIdentity(UUID var1) {
      if (var1 != null) {
         this.identityTargets.add(var1);
      }
   }

   void requestSocial(UUID var1) {
      if (var1 != null) {
         this.socialViewers.add(var1);
      }
   }

   TablistRefreshCoordinator.Snapshot drain() {
      return new TablistRefreshCoordinator.Snapshot(this.drain(this.headerFooterViewers), this.drain(this.identityTargets), this.drain(this.socialViewers));
   }

   void remove(UUID var1) {
      this.headerFooterViewers.remove(var1);
      this.identityTargets.remove(var1);
      this.socialViewers.remove(var1);
   }

   private Set<UUID> drain(Set<UUID> var1) {
      HashSet var2 = new HashSet();

      for (UUID var4 : var1) {
         if (var1.remove(var4)) {
            var2.add(var4);
         }
      }

      return var2;
   }

   static record Snapshot(Set<UUID> headerFooterViewers, Set<UUID> identityTargets, Set<UUID> socialViewers) {
      static final TablistRefreshCoordinator.Snapshot EMPTY = new TablistRefreshCoordinator.Snapshot(Set.of(), Set.of(), Set.of());
   }
}
