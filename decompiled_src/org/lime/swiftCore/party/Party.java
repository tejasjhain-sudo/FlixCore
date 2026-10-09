package org.lime.swiftCore.party;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Party {
   private final UUID owner;
   private final Set<UUID> members;
   private final Map<UUID, Long> invites;
   private boolean publicMode;
   private int maxSize;
   private boolean broadcasting;
   private PartyMode mode;
   private boolean allowImbalance;

   public Party(UUID var1, int var2) {
      this.owner = var1;
      this.members = ConcurrentHashMap.newKeySet();
      this.invites = new ConcurrentHashMap<>();
      this.members.add(var1);
      this.publicMode = false;
      this.maxSize = var2;
      this.broadcasting = false;
      this.mode = PartyMode.IDLE;
      this.allowImbalance = false;
   }

   public UUID getOwner() {
      return this.owner;
   }

   public Set<UUID> getMembers() {
      return new HashSet<>(this.members);
   }

   public boolean isMember(UUID var1) {
      return this.members.contains(var1);
   }

   public boolean isOwner(UUID var1) {
      return this.owner.equals(var1);
   }

   public void addMember(UUID var1) {
      this.members.add(var1);
      this.invites.remove(var1);
   }

   public void removeMember(UUID var1) {
      this.members.remove(var1);
   }

   public void invite(UUID var1) {
      this.invites.put(var1, System.currentTimeMillis());
   }

   public boolean hasInvite(UUID var1) {
      return this.hasInvite(var1, 60000L);
   }

   public boolean hasInvite(UUID var1, long var2) {
      Long var4 = this.invites.get(var1);
      if (var4 == null) {
         return false;
      } else {
         long var5 = System.currentTimeMillis() - var4;
         if (var5 > var2) {
            this.invites.remove(var1);
            return false;
         } else {
            return true;
         }
      }
   }

   public void removeInvite(UUID var1) {
      this.invites.remove(var1);
   }

   public boolean isPublic() {
      return this.publicMode;
   }

   public void setPublic(boolean var1) {
      this.publicMode = var1;
   }

   public int getMaxSize() {
      return this.maxSize;
   }

   public void setMaxSize(int var1) {
      this.maxSize = Math.max(2, var1);
   }

   public boolean isFull() {
      return this.members.size() >= this.maxSize;
   }

   public int getSize() {
      return this.members.size();
   }

   public boolean isBroadcasting() {
      return this.broadcasting;
   }

   public void setBroadcasting(boolean var1) {
      this.broadcasting = var1;
   }

   public PartyMode getMode() {
      return this.mode;
   }

   public void setMode(PartyMode var1) {
      this.mode = var1;
   }

   public boolean isAllowImbalance() {
      return this.allowImbalance;
   }

   public void setAllowImbalance(boolean var1) {
      this.allowImbalance = var1;
   }
}
