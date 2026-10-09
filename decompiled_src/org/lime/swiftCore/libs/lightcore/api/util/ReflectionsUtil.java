package org.lime.swiftCore.libs.lightcore.api.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lime.swiftCore.libs.caffeine.cache.Cache;
import org.lime.swiftCore.libs.caffeine.cache.Caffeine;

public final class ReflectionsUtil {
   private static final Cache<String, Class<?>> CLASS_CACHE = Caffeine.newBuilder().maximumSize(256L).expireAfterAccess(30L, TimeUnit.MINUTES).build();
   private static final Cache<String, Constructor<?>> CONSTRUCTOR_CACHE = Caffeine.newBuilder()
      .maximumSize(256L)
      .expireAfterAccess(30L, TimeUnit.MINUTES)
      .build();
   private static final Cache<String, Field> FIELD_CACHE = Caffeine.newBuilder().maximumSize(512L).expireAfterAccess(30L, TimeUnit.MINUTES).build();
   private static final Cache<String, Method> METHOD_CACHE = Caffeine.newBuilder().maximumSize(512L).expireAfterAccess(30L, TimeUnit.MINUTES).build();

   private ReflectionsUtil() {
   }

   public static void clearAllCaches() {
      CLASS_CACHE.invalidateAll();
      CONSTRUCTOR_CACHE.invalidateAll();
      FIELD_CACHE.invalidateAll();
      METHOD_CACHE.invalidateAll();
   }

   @Nullable
   public static Class<?> getClass(@NotNull String var0, @NotNull String var1) {
      return getClass(var0 + "." + var1);
   }

   @Nullable
   public static Class<?> getInnerClass(@NotNull String var0, @NotNull String var1) {
      return getClass(var0 + "$" + var1);
   }

   @Nullable
   public static Class<?> getClass(@NotNull String var0) {
      return getClass(var0, true);
   }

   @Nullable
   public static Class<?> getClass(@NotNull String var0, boolean var1) {
      try {
         Class var2 = CLASS_CACHE.getIfPresent(var0);
         if (var2 != null) {
            return var2;
         } else {
            Class var3 = Class.forName(var0);
            CLASS_CACHE.put(var0, var3);
            return var3;
         }
      } catch (ClassNotFoundException var4) {
         if (var1) {
            var4.printStackTrace();
         }

         return null;
      }
   }

   @Nullable
   public static Constructor<?> getConstructor(@NotNull Class<?> var0, Class<?>... var1) {
      String var2 = var0.getName() + Arrays.toString((Object[])var1);
      Constructor var3 = CONSTRUCTOR_CACHE.getIfPresent(var2);
      if (var3 != null) {
         return var3;
      } else {
         try {
            Constructor var4 = var0.getDeclaredConstructor(var1);
            var4.setAccessible(true);
            CONSTRUCTOR_CACHE.put(var2, var4);
            return var4;
         } catch (ReflectiveOperationException var5) {
            var5.printStackTrace();
            return null;
         }
      }
   }

   @Nullable
   public static Object invokeConstructor(@NotNull Constructor<?> var0, Object... var1) {
      try {
         return var0.newInstance(var1);
      } catch (ReflectiveOperationException var3) {
         var3.printStackTrace();
         return null;
      }
   }

   @NotNull
   public static List<Field> getFields(@NotNull Class<?> var0, boolean var1) {
      ArrayList var2 = new ArrayList();

      for (Class var3 = var0; var3 != null && var3 != Object.class; var3 = var3.getSuperclass()) {
         var2.addAll(Arrays.asList(var3.getDeclaredFields()));
         if (!var1) {
            break;
         }
      }

      return var2;
   }

   @Nullable
   public static Field getField(@NotNull Class<?> var0, @NotNull String var1) {
      String var2 = var0.getName() + "#" + var1;
      Field var3 = FIELD_CACHE.getIfPresent(var2);
      if (var3 != null) {
         return var3;
      } else {
         try {
            Field var4 = var0.getDeclaredField(var1);
            var4.setAccessible(true);
            FIELD_CACHE.put(var2, var4);
            return var4;
         } catch (NoSuchFieldException var6) {
            Class var5 = var0.getSuperclass();
            return var5 == null ? null : getField(var5, var1);
         }
      }
   }

   @Nullable
   public static Object getFieldValue(@NotNull Object var0, @NotNull String var1) {
      Class var2 = var0 instanceof Class var3 ? var3 : var0.getClass();
      Field var6 = getField(var2, var1);
      if (var6 == null) {
         return null;
      } else {
         try {
            return var6.get(var0 instanceof Class ? null : var0);
         } catch (IllegalAccessException var5) {
            var5.printStackTrace();
            return null;
         }
      }
   }

   public static boolean setFieldValue(@NotNull Object var0, @NotNull String var1, @Nullable Object var2) {
      boolean var3 = var0 instanceof Class;
      Class var4 = var3 ? (Class)var0 : var0.getClass();
      Field var5 = getField(var4, var1);
      if (var5 == null) {
         return false;
      } else {
         try {
            var5.set(var3 ? null : var0, var2);
            return true;
         } catch (IllegalAccessException var7) {
            var7.printStackTrace();
            return false;
         }
      }
   }

   @Nullable
   public static Method getMethod(@NotNull Class<?> var0, @NotNull String var1, @NotNull Class<?>... var2) {
      String var3 = var0.getName() + "#" + var1 + Arrays.toString((Object[])var2);
      Method var4 = METHOD_CACHE.getIfPresent(var3);
      if (var4 != null) {
         return var4;
      } else {
         try {
            Method var5 = var0.getDeclaredMethod(var1, var2);
            var5.setAccessible(true);
            METHOD_CACHE.put(var3, var5);
            return var5;
         } catch (NoSuchMethodException var7) {
            Class var6 = var0.getSuperclass();
            return var6 == null ? null : getMethod(var6, var1, var2);
         }
      }
   }

   @Nullable
   public static Object invokeMethod(@NotNull Method var0, @Nullable Object var1, @Nullable Object... var2) {
      try {
         return var0.invoke(var1, var2);
      } catch (InvocationTargetException | IllegalAccessException var4) {
         var4.printStackTrace();
         return null;
      }
   }

   @NotNull
   public static Set<Class<?>> getTypesAnnotatedWith(@NotNull ClassLoader var0, @NotNull String var1, @NotNull Class<? extends Annotation> var2) {
      HashSet var3 = new HashSet();

      for (String var6 : findClassNames(var0, var1)) {
         try {
            Class var7 = Class.forName(var6, false, var0);
            if (var7.isAnnotationPresent(var2)) {
               var3.add(var7);
            }
         } catch (Throwable var8) {
         }
      }

      return var3;
   }

   @NotNull
   public static Set<Class<?>> getSubTypesOf(@NotNull ClassLoader var0, @NotNull String var1, @NotNull Class<?> var2) {
      HashSet var3 = new HashSet();

      for (String var6 : findClassNames(var0, var1)) {
         try {
            Class var7 = Class.forName(var6, false, var0);
            if (var2.isAssignableFrom(var7) && !var2.equals(var7)) {
               var3.add(var7);
            }
         } catch (Throwable var8) {
         }
      }

      return var3;
   }

   @NotNull
   public static Set<Class<?>> getAllTypes(@NotNull ClassLoader var0, @NotNull String var1) {
      HashSet var2 = new HashSet();

      for (String var5 : findClassNames(var0, var1)) {
         try {
            var2.add(Class.forName(var5, false, var0));
         } catch (Throwable var7) {
         }
      }

      return var2;
   }

   @NotNull
   private static Set<String> findClassNames(@NotNull ClassLoader var0, @NotNull String var1) {
      HashSet var2 = new HashSet();
      String var3 = var1.replace('.', '/');

      try {
         Enumeration var4 = var0.getResources(var3);

         while (var4.hasMoreElements()) {
            URL var5 = (URL)var4.nextElement();
            String var6 = var5.getProtocol();

            try {
               if ("file".equals(var6)) {
                  URI var7 = var5.toURI();
                  File var8 = new File(var7);
                  if (var8.isDirectory()) {
                     scanDirectory(var8, var1, var2);
                  } else if (var8.getName().endsWith(".jar")) {
                     try (JarFile var9 = new JarFile(var8)) {
                        scanJar(var9, var3, var2);
                     }
                  } else {
                     JarFile var28 = tryGetJarFromUrl(var5);
                     if (var28 != null) {
                        JarFile var10 = var28;

                        try {
                           scanJar(var10, var3, var2);
                        } catch (Throwable var20) {
                           if (var28 != null) {
                              try {
                                 var10.close();
                              } catch (Throwable var15) {
                                 var20.addSuppressed(var15);
                              }
                           }

                           throw var20;
                        }

                        if (var28 != null) {
                           var28.close();
                        }
                     }
                  }
               } else if ("jar".equals(var6)) {
                  JarURLConnection var23 = (JarURLConnection)var5.openConnection();

                  try (JarFile var25 = var23.getJarFile()) {
                     scanJar(var25, var3, var2);
                  }
               } else {
                  JarFile var24 = tryGetJarFromUrl(var5);
                  if (var24 != null) {
                     JarFile var26 = var24;

                     try {
                        scanJar(var26, var3, var2);
                     } catch (Throwable var18) {
                        if (var24 != null) {
                           try {
                              var26.close();
                           } catch (Throwable var13) {
                              var18.addSuppressed(var13);
                           }
                        }

                        throw var18;
                     }

                     if (var24 != null) {
                        var24.close();
                     }
                  } else {
                     File var27 = new File(URLDecoder.decode(var5.getPath(), StandardCharsets.UTF_8));
                     if (var27.exists() && var27.isDirectory()) {
                        scanDirectory(var27, var1, var2);
                     }
                  }
               }
            } catch (URISyntaxException var21) {
            }
         }
      } catch (IOException var22) {
      }

      return var2;
   }

   private static void scanDirectory(@NotNull File var0, @NotNull String var1, @NotNull Set<String> var2) {
      int var3 = var0.getAbsolutePath().length() + 1;
      ArrayDeque var4 = new ArrayDeque();
      var4.push(var0);

      while (!var4.isEmpty()) {
         File var5 = (File)var4.pop();
         File[] var6 = var5.listFiles();
         if (var6 != null) {
            for (File var10 : var6) {
               if (var10.isDirectory()) {
                  var4.push(var10);
               } else {
                  String var11 = var10.getAbsolutePath();
                  if (var11.endsWith(".class")) {
                     String var12 = var11.substring(var3);
                     String var13 = var1 + "." + var12.replace(File.separatorChar, '.').replaceAll("\\.class$", "");
                     var2.add(var13);
                  }
               }
            }
         }
      }
   }

   private static void scanJar(@NotNull JarFile var0, @NotNull String var1, @NotNull Set<String> var2) {
      Enumeration var3 = var0.entries();

      while (var3.hasMoreElements()) {
         JarEntry var4 = (JarEntry)var3.nextElement();
         String var5 = var4.getName();
         if (!var4.isDirectory() && var5.startsWith(var1) && var5.endsWith(".class")) {
            String var6 = var5.replace('/', '.').substring(0, var5.length() - 6);
            var2.add(var6);
         }
      }
   }

   @Nullable
   private static JarFile tryGetJarFromUrl(@NotNull URL var0) {
      try {
         String var1 = var0.toExternalForm();
         int var2 = var1.indexOf("!/");
         String var3 = var2 != -1 ? var1.substring(0, var2) : var1;
         String var4 = var3.startsWith("jar:") ? var3.substring(4) : var3;
         int var5 = var4.indexOf("file:");
         String var6 = var5 != -1 ? var4.substring(var5 + 5) : var4;
         String var7 = URLDecoder.decode(var6, StandardCharsets.UTF_8);
         File var8 = new File(var7);
         if (var8.exists() && var8.isFile()) {
            return new JarFile(var8);
         }
      } catch (Throwable var9) {
      }

      return null;
   }
}
