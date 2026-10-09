package org.lime.swiftCore.scoreboard;

public class BoardLine {
   private final int index;
   private final String entry;
   private final String teamName;
   private int lastHash = 0;
   private boolean scoreSet = false;
   private static final String[] ENTRY_COLORS = new String[]{"§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9", "§a", "§b", "§c", "§d", "§e", "§f"};

   public BoardLine(int var1) {
      this.index = var1;
      this.entry = ENTRY_COLORS[var1 % ENTRY_COLORS.length];
      this.teamName = "sb_" + var1;
   }

   public boolean update(String var1) {
      int var2 = var1.hashCode();
      if (var2 == this.lastHash) {
         return false;
      } else {
         this.lastHash = var2;
         return true;
      }
   }

   public String getEntry() {
      return this.entry;
   }

   public String getTeamName() {
      return this.teamName;
   }

   public int getIndex() {
      return this.index;
   }

   public boolean isScoreSet() {
      return this.scoreSet;
   }

   public void markScoreSet() {
      this.scoreSet = true;
   }

   public void reset() {
      this.lastHash = 0;
      this.scoreSet = false;
   }
}
