package org.lime.swiftCore.scoreboard;

public sealed interface LinePart permits StaticPart, DynamicPart {
   String getText();

   boolean isStatic();
}
