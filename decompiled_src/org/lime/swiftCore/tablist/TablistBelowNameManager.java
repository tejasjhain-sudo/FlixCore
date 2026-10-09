package org.lime.swiftCore.tablist;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.score.FixedScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResetScore;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective.ObjectiveMode;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective.RenderType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore.Action;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lime.swiftCore.SwiftCore;
import org.lime.swiftCore.kit.n;

final class TablistBelowNameManager {
   private static final String OBJECTIVE_NAME = "swiftbn";
   private static final int DISPLAY_SLOT_BELOW_NAME = 2;
   private final SwiftCore plugin;
   private final TablistManager tablistManager;
   private final Map<UUID, Set<String>> activeEntries = new ConcurrentHashMap<>();
   private final Map<UUID, Map<String, String>> sentSignatures = new ConcurrentHashMap<>();
   private final Set<UUID> objectiveViewers = ConcurrentHashMap.newKeySet();

   TablistBelowNameManager(SwiftCore var1, TablistManager var2) {
      this.plugin = var1;
      this.tablistManager = var2;
   }

   void reload() {
      if (!this.tablistManager.isBelowNameEnabled()) {
         this.clearAll();
      } else {
         for (Player var2 : Bukkit.getOnlinePlayers()) {
            this.syncViewer(var2);
         }
      }
   }

   void shutdown() {
      this.clearAll();
   }

   void syncViewer(Player var1) {
      if (this.tablistManager.isEnabled() && var1 != null && var1.isOnline()) {
         if (this.tablistManager.isBelowNameEnabled() && !this.shouldDeferToHpIndicator(var1)) {
            this.ensureObjective(var1);
            Set var2 = this.activeEntries.computeIfAbsent(var1.getUniqueId(), var0 -> ConcurrentHashMap.newKeySet());
            Map var3 = this.sentSignatures.computeIfAbsent(var1.getUniqueId(), var0 -> new ConcurrentHashMap<>());
            KeySetView var4 = ConcurrentHashMap.newKeySet();

            for (Player var6 : Bukkit.getOnlinePlayers()) {
               if (!var6.equals(var1) && !this.shouldSkipTarget(var6)) {
                  var4.add(var6.getName());
                  this.updateEntry(var1, var6, var2, var3);
               }
            }

            for (String var8 : Set.copyOf(var2)) {
               if (!var4.contains(var8)) {
                  this.removeEntry(var1, var8, var2, var3);
               }
            }
         } else {
            this.clearViewer(var1);
         }
      }
   }

   void refreshTarget(Player var1) {
      if (this.tablistManager.isEnabled() && var1 != null && var1.isOnline() && this.tablistManager.isBelowNameEnabled()) {
         if (this.shouldSkipTarget(var1)) {
            this.clearTarget(var1);
         } else {
            for (Player var3 : Bukkit.getOnlinePlayers()) {
               if (!var3.equals(var1) && !this.shouldDeferToHpIndicator(var3)) {
                  Set var4 = this.activeEntries.computeIfAbsent(var3.getUniqueId(), var0 -> ConcurrentHashMap.newKeySet());
                  Map var5 = this.sentSignatures.computeIfAbsent(var3.getUniqueId(), var0 -> new ConcurrentHashMap<>());
                  this.ensureObjective(var3);
                  this.updateEntry(var3, var1, var4, var5);
               }
            }
         }
      }
   }

   void clearViewer(Player var1) {
      if (var1 != null) {
         UUID var2 = var1.getUniqueId();
         Set var3 = this.activeEntries.remove(var2);
         this.sentSignatures.remove(var2);
         if (var3 != null) {
            for (String var5 : var3) {
               this.sendResetScore(var1, var5);
            }
         }

         this.sendObjectiveRemove(var1);
      }
   }

   void clearTarget(Player var1) {
      if (var1 != null) {
         String var2 = var1.getName();

         for (Player var4 : Bukkit.getOnlinePlayers()) {
            Set var5 = this.activeEntries.get(var4.getUniqueId());
            Map var6 = this.sentSignatures.get(var4.getUniqueId());
            if (var5 != null) {
               var5.remove(var2);
            }

            if (var6 != null) {
               var6.remove(var2);
            }

            this.sendResetScore(var4, var2);
         }
      }
   }

   private void updateEntry(Player var1, Player var2, Set<String> var3, Map<String, String> var4) {
      Component var5 = this.tablistManager.buildBelowNameComponent(var2);
      if (var5 != null && !Component.empty().equals(var5)) {
         String var6 = var5.toString();
         String var7 = var2.getName();
         if (var6.equals(var4.get(var7))) {
            var3.add(var7);
         } else {
            WrapperPlayServerUpdateScore var8 = new WrapperPlayServerUpdateScore(
               var7, Action.CREATE_OR_UPDATE_ITEM, "swiftbn", 0, null, new FixedScoreFormat(var5)
            );
            PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var8);
            var3.add(var7);
            var4.put(var7, var6);
         }
      } else {
         this.removeEntry(var1, var2.getName(), var3, var4);
      }
   }

   private void removeEntry(Player var1, String var2, Set<String> var3, Map<String, String> var4) {
      if (var3.remove(var2)) {
         var4.remove(var2);
         this.sendResetScore(var1, var2);
      }
   }

   private void ensureObjective(Player var1) {
      if (!this.objectiveViewers.contains(var1.getUniqueId())) {
         this.sendObjectiveCreate(var1);
         this.sendDisplaySlot(var1);
         this.objectiveViewers.add(var1.getUniqueId());
      }
   }

   private boolean shouldDeferToHpIndicator(Player var1) {
      n var2 = this.plugin.getHPIndicatorManager();
      return var2 != null && var2.usesBelowNameSlot(var1.getUniqueId());
   }

   private boolean shouldSkipTarget(Player var1) {
      n var2 = this.plugin.getHPIndicatorManager();
      return var2 != null && var2.isActive(var1.getUniqueId());
   }

   private void clearAll() {
      for (Player var2 : Bukkit.getOnlinePlayers()) {
         this.clearViewer(var2);
      }

      this.activeEntries.clear();
      this.sentSignatures.clear();
      this.objectiveViewers.clear();
   }

   private void sendObjectiveCreate(Player var1) {
      WrapperPlayServerScoreboardObjective var2 = new WrapperPlayServerScoreboardObjective(
         "swiftbn", ObjectiveMode.REMOVE, Component.empty(), RenderType.INTEGER
      );
      PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var2);
      WrapperPlayServerScoreboardObjective var3 = new WrapperPlayServerScoreboardObjective(
         "swiftbn", ObjectiveMode.CREATE, Component.empty(), RenderType.INTEGER
      );
      PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var3);
   }

   private void sendDisplaySlot(Player var1) {
      WrapperPlayServerDisplayScoreboard var2 = new WrapperPlayServerDisplayScoreboard(2, "swiftbn");
      PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var2);
   }

   private void sendObjectiveRemove(Player var1) {
      this.objectiveViewers.remove(var1.getUniqueId());
      WrapperPlayServerScoreboardObjective var2 = new WrapperPlayServerScoreboardObjective(
         "swiftbn", ObjectiveMode.REMOVE, Component.empty(), RenderType.INTEGER
      );
      PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var2);
   }

   private void sendResetScore(Player var1, String var2) {
      WrapperPlayServerResetScore var3 = new WrapperPlayServerResetScore(var2, "swiftbn");
      PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var3);
   }
}
