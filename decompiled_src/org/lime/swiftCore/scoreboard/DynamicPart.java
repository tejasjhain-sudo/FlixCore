package org.lime.swiftCore.scoreboard;

public record DynamicPart(String placeholder, DynamicPart.PlaceholderType type) implements LinePart {
   @Override
   public String getText() {
      return this.placeholder;
   }

   @Override
   public boolean isStatic() {
      return false;
   }

   public static enum PlaceholderType {
      INTERNAL,
      PAPI,
      ANIMATION;
   }
}
