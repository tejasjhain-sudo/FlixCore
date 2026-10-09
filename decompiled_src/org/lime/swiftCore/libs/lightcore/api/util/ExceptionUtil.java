package org.lime.swiftCore.libs.lightcore.api.util;

import java.io.IOException;

public final class ExceptionUtil {
   private ExceptionUtil() {
   }

   public static RuntimeException runtime(String var0) {
      return new RuntimeException(var0);
   }

   public static RuntimeException runtime(String var0, Throwable var1) {
      return new RuntimeException(var0, var1);
   }

   public static IllegalArgumentException illegalArgument(String var0) {
      return new IllegalArgumentException(var0);
   }

   public static IllegalArgumentException illegalArgument(String var0, Throwable var1) {
      return new IllegalArgumentException(var0, var1);
   }

   public static IllegalStateException illegalState(String var0) {
      return new IllegalStateException(var0);
   }

   public static IllegalStateException illegalState(String var0, Throwable var1) {
      return new IllegalStateException(var0, var1);
   }

   public static NullPointerException nullPointer(String var0) {
      return new NullPointerException(var0);
   }

   public static UnsupportedOperationException unsupported(String var0) {
      return new UnsupportedOperationException(var0);
   }

   public static IOException io(String var0) {
      return new IOException(var0);
   }

   public static IOException io(String var0, Throwable var1) {
      return new IOException(var0, var1);
   }

   public static ReflectiveOperationException reflection(String var0) {
      return new ReflectiveOperationException(var0);
   }

   public static ReflectiveOperationException reflection(String var0, Throwable var1) {
      return new ReflectiveOperationException(var0, var1);
   }
}
