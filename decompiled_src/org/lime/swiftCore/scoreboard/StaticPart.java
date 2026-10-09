package org.lime.swiftCore.scoreboard;

public record StaticPart(String text) implements LinePart {
   @Override
   public String getText() {
      return this.text;
   }

   @Override
   public boolean isStatic() {
      return true;
   }
}
