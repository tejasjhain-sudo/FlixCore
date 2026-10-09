package org.checkerframework.framework.qual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum LiteralKind {
   NULL,
   INT,
   LONG,
   FLOAT,
   DOUBLE,
   BOOLEAN,
   CHAR,
   STRING,
   ALL,
   PRIMITIVE;

   public static List<LiteralKind> allLiteralKinds() {
      ArrayList var0 = new ArrayList<>(Arrays.asList(values()));
      var0.remove(ALL);
      var0.remove(PRIMITIVE);
      return var0;
   }

   public static List<LiteralKind> primitiveLiteralKinds() {
      return Arrays.asList(INT, LONG, FLOAT, DOUBLE, BOOLEAN, CHAR);
   }
}
