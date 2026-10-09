package org.lime.swiftCore.libs.lightcore.api.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class FileUtil {
   private FileUtil() {
   }

   @NotNull
   public static File get(@NotNull JavaPlugin var0, @NotNull String var1) {
      File var2 = new File(var0.getDataFolder(), var1);
      if (!var2.exists()) {
         if (isFile(var1)) {
            var2.getParentFile().mkdirs();
         } else {
            var2.mkdirs();
         }
      }

      return var2;
   }

   public static void saveResource(@NotNull JavaPlugin var0, @NotNull String var1, boolean var2) {
      File var3 = get(var0, var1);
      if (!var3.exists() || var2) {
         var0.saveResource(var1, var2);
      }
   }

   public static void write(@NotNull File var0, @NotNull String var1) throws IOException {
      var0.getParentFile().mkdirs();
      Files.writeString(var0.toPath(), var1);
   }

   @NotNull
   public static String read(@NotNull File var0) throws IOException {
      return Files.readString(var0.toPath());
   }

   @NotNull
   public static List<String> readLines(@NotNull File var0) throws IOException {
      return Files.readAllLines(var0.toPath());
   }

   public static void copy(@NotNull File var0, @NotNull File var1) throws IOException {
      var1.getParentFile().mkdirs();
      Files.copy(var0.toPath(), var1.toPath(), StandardCopyOption.REPLACE_EXISTING);
   }

   public static void move(@NotNull File var0, @NotNull File var1) throws IOException {
      var1.getParentFile().mkdirs();
      Files.move(var0.toPath(), var1.toPath(), StandardCopyOption.REPLACE_EXISTING);
   }

   public static void delete(@NotNull File var0) throws IOException {
      if (var0.exists()) {
         if (var0.isDirectory()) {
            for (File var4 : Objects.requireNonNull(var0.listFiles())) {
               delete(var4);
            }
         }

         if (!var0.delete()) {
            throw new IOException("Failed to delete file: " + var0);
         }
      }
   }

   @NotNull
   public static List<File> listRecursive(@NotNull File var0) {
      return var0.exists() && var0.isDirectory()
         ? Arrays.stream(Objects.requireNonNull(var0.listFiles()))
            .flatMap(var0x -> var0x.isDirectory() ? listRecursive(var0x).stream() : Stream.of((File)var0x))
            .toList()
         : List.of();
   }

   public static void create(@NotNull File var0) throws IOException {
      if (!var0.exists()) {
         var0.getParentFile().mkdirs();
         Files.createFile(var0.toPath());
      }
   }

   public static boolean exists(@NotNull File var0) {
      return var0.exists();
   }

   public static boolean isFile(@NotNull String var0) {
      return var0.contains(".");
   }

   public static boolean isFile(@NotNull File var0) {
      return var0.isFile();
   }

   public static boolean isDirectory(@NotNull File var0) {
      return var0.isDirectory();
   }

   public static boolean isHidden(@NotNull File var0) {
      return var0.isHidden();
   }

   @NotNull
   public static String extension(@NotNull File var0) {
      String var1 = var0.getName();
      int var2 = var1.lastIndexOf(46);
      return var2 == -1 ? "" : var1.substring(var2 + 1);
   }

   @NotNull
   public static String fileName(@NotNull File var0) {
      String var1 = var0.getName();
      int var2 = var1.lastIndexOf(46);
      return var2 == -1 ? var1 : var1.substring(0, var2);
   }

   @NotNull
   public static String fileName(@NotNull String var0) {
      return fileName(new File(var0));
   }

   @NotNull
   public static File parent(@NotNull File var0) {
      return var0.getParentFile();
   }

   public static long size(@NotNull File var0) {
      return var0.exists() ? var0.length() : 0L;
   }

   public static long lastModified(@NotNull File var0) {
      return var0.exists() ? var0.lastModified() : 0L;
   }

   public static long linesCount(@NotNull File var0) throws IOException {
      long var2;
      try (Stream var1 = Files.lines(var0.toPath())) {
         var2 = var1.count();
      }

      return var2;
   }

   @NotNull
   public static String normalizePath(@NotNull File var0) {
      return var0.toPath().normalize().toAbsolutePath().toString();
   }

   @NotNull
   public static Path toPath(@NotNull File var0) {
      return var0.toPath();
   }
}
