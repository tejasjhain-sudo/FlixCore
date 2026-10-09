package org.lime.swiftCore.scoreboard;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.score.BlankScoreFormat;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerResetScore;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective.ObjectiveMode;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective.RenderType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.CollisionRule;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.NameTagVisibility;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.OptionData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.ScoreBoardTeamInfo;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams.TeamMode;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore.Action;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SwiftBoard {
   private static final int MAX_LINES = 15;
   private static final String OBJECTIVE_NAME = "swiftboard";
   private final UUID playerId;
   private final List<BoardLine> lines;
   private final boolean hideNumbers;
   private int currentLineCount = 0;
   private boolean deleted = false;
   private String lastTitleRaw = null;

   public SwiftBoard(Player var1, boolean var2) {
      this.playerId = var1.getUniqueId();
      this.hideNumbers = var2;
      this.lines = new ArrayList<>(15);

      for (int var3 = 0; var3 < 15; var3++) {
         this.lines.add(new BoardLine(var3));
      }

      this.sendObjectiveRemove(var1);
      this.sendObjectiveCreate(var1, Component.empty());
      this.sendDisplaySlot(var1);
   }

   public synchronized void updateTitle(Component var1, String var2) {
      if (!this.deleted) {
         if (!var2.equals(this.lastTitleRaw)) {
            this.lastTitleRaw = var2;
            Player var3 = Bukkit.getPlayer(this.playerId);
            if (var3 != null && var3.isOnline()) {
               this.sendObjectiveUpdate(var3, var1);
            }
         }
      }
   }

   public synchronized void updateLines(List<Component> var1, List<String> var2) {
      this.updateLines(var1, var2, false);
   }

   public synchronized void updateLines(List<Component> var1, List<String> var2, boolean var3) {
      if (!this.deleted) {
         Player var4 = Bukkit.getPlayer(this.playerId);
         if (var4 != null && var4.isOnline()) {
            int var5 = Math.min(Math.min(var1.size(), var2.size()), 15);

            for (int var6 = 0; var6 < var5; var6++) {
               int var7 = var5 - 1 - var6;
               int var8 = var5 - var6;
               BoardLine var9 = this.lines.get(var7);
               Component var10 = (Component)var1.get(var6);
               String var11 = (String)var2.get(var6);
               boolean var12 = var9.update(var11);
               if (!var9.isScoreSet()) {
                  this.sendTeamCreate(var4, var9.getTeamName(), Component.empty(), var9.getEntry());
                  this.sendScore(var4, var9.getEntry(), var8, var10);
                  var9.markScoreSet();
               } else if (var12 || var3) {
                  this.sendScore(var4, var9.getEntry(), var8, var10);
               }
            }

            for (int var13 = var5; var13 < this.currentLineCount; var13++) {
               BoardLine var14 = this.lines.get(var13);
               this.sendResetScore(var4, var14.getEntry());
               this.sendTeamRemove(var4, var14.getTeamName());
               var14.reset();
            }

            this.currentLineCount = var5;
         }
      }
   }

   public synchronized void updateLines(List<Component> var1) {
      if (!this.deleted) {
         Player var2 = Bukkit.getPlayer(this.playerId);
         if (var2 != null && var2.isOnline()) {
            int var3 = Math.min(var1.size(), 15);

            for (int var4 = 0; var4 < var3; var4++) {
               int var5 = var3 - 1 - var4;
               int var6 = var3 - var4;
               BoardLine var7 = this.lines.get(var5);
               Component var8 = (Component)var1.get(var4);
               String var9 = var8.toString();
               boolean var10 = var7.update(var9);
               if (!var7.isScoreSet()) {
                  this.sendTeamCreate(var2, var7.getTeamName(), Component.empty(), var7.getEntry());
                  this.sendScore(var2, var7.getEntry(), var6, var8);
                  var7.markScoreSet();
               } else if (var10) {
                  this.sendScore(var2, var7.getEntry(), var6, var8);
               }
            }

            for (int var11 = var3; var11 < this.currentLineCount; var11++) {
               BoardLine var12 = this.lines.get(var11);
               this.sendResetScore(var2, var12.getEntry());
               this.sendTeamRemove(var2, var12.getTeamName());
               var12.reset();
            }

            this.currentLineCount = var3;
         }
      }
   }

   public boolean isDeleted() {
      return this.deleted;
   }

   public synchronized void delete() {
      if (!this.deleted) {
         this.deleted = true;
         Player var1 = Bukkit.getPlayer(this.playerId);
         if (var1 != null && var1.isOnline()) {
            for (int var2 = 0; var2 < this.currentLineCount; var2++) {
               BoardLine var3 = this.lines.get(var2);
               this.sendResetScore(var1, var3.getEntry());
               this.sendTeamRemove(var1, var3.getTeamName());
            }

            this.sendObjectiveRemove(var1);
         }

         this.lines.clear();
         this.currentLineCount = 0;
      }
   }

   public UUID getPlayerId() {
      return this.playerId;
   }

   private void sendObjectiveCreate(Player var1, Component var2) {
      WrapperPlayServerScoreboardObjective var3 = new WrapperPlayServerScoreboardObjective("swiftboard", ObjectiveMode.CREATE, var2, RenderType.INTEGER);
      PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var3);
   }

   private void sendObjectiveUpdate(Player var1, Component var2) {
      WrapperPlayServerScoreboardObjective var3 = new WrapperPlayServerScoreboardObjective("swiftboard", ObjectiveMode.UPDATE, var2, RenderType.INTEGER);
      PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var3);
   }

   private void sendObjectiveRemove(Player var1) {
      WrapperPlayServerScoreboardObjective var2 = new WrapperPlayServerScoreboardObjective(
         "swiftboard", ObjectiveMode.REMOVE, Component.empty(), RenderType.INTEGER
      );
      PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var2);
   }

   private void sendDisplaySlot(Player var1) {
      WrapperPlayServerDisplayScoreboard var2 = new WrapperPlayServerDisplayScoreboard(1, "swiftboard");
      PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var2);
   }

   private void sendScore(Player var1, String var2, int var3, Component var4) {
      WrapperPlayServerUpdateScore var5 = new WrapperPlayServerUpdateScore(
         var2, Action.CREATE_OR_UPDATE_ITEM, "swiftboard", var3, var4, this.hideNumbers ? BlankScoreFormat.INSTANCE : null
      );
      PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var5);
   }

   private void sendResetScore(Player var1, String var2) {
      WrapperPlayServerResetScore var3 = new WrapperPlayServerResetScore(var2, "swiftboard");
      PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var3);
   }

   private void sendTeamCreate(Player var1, String var2, Component var3, String var4) {
      ScoreBoardTeamInfo var5 = new ScoreBoardTeamInfo(
         Component.empty(), var3, Component.empty(), NameTagVisibility.NEVER, CollisionRule.NEVER, NamedTextColor.WHITE, OptionData.NONE
      );
      WrapperPlayServerTeams var6 = new WrapperPlayServerTeams(var2, TeamMode.CREATE, var5, Collections.singletonList(var4));
      PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var6);
   }

   private void sendTeamRemove(Player var1, String var2) {
      WrapperPlayServerTeams var3 = new WrapperPlayServerTeams(var2, TeamMode.REMOVE, (ScoreBoardTeamInfo)null, new String[0]);
      PacketEvents.getAPI().getPlayerManager().sendPacket(var1, var3);
   }
}
