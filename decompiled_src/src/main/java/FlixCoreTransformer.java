import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

public class FlixCoreTransformer {

    private static ClassWriter createWriter() {
        return new ClassWriter(ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                return "java/lang/Object";
            }
        };
    }

    public static byte[] patchVerificationResult(byte[] classBytes) throws Exception {
        ClassReader cr = new ClassReader(classBytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        for (MethodNode mn : cn.methods) {
            if (mn.name.equals("isValid") && mn.desc.equals("()Z")) {
                mn.instructions.clear();
                mn.tryCatchBlocks.clear();
                mn.instructions.add(new InsnNode(Opcodes.ICONST_1));
                mn.instructions.add(new InsnNode(Opcodes.IRETURN));
                mn.maxStack = 1;
                mn.maxLocals = 1;
                System.out.println("Patched VerificationResult.isValid()");
            }
        }

        ClassWriter cw = createWriter();
        cn.accept(cw);
        return cw.toByteArray();
    }

    public static byte[] patchAuthGuard(byte[] classBytes) throws Exception {
        ClassReader cr = new ClassReader(classBytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        for (MethodNode mn : cn.methods) {
            if (mn.name.equals("verify") && mn.desc.contains("VerificationResult")) {
                mn.instructions.clear();
                mn.tryCatchBlocks.clear();
                mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); // JavaPlugin plugin
                mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1)); // String licenseKey
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "club/aspvp/license/LicenseClient",
                        "verifyForAuthGuard",
                        "(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;)Lorg/lime/swiftCore/libs/authguard/sdk/VerificationResult;",
                        false));
                mn.instructions.add(new InsnNode(Opcodes.ARETURN));
                mn.maxStack = 2;
                mn.maxLocals = 5;
                System.out.println("Hooked AuthGuard.verify() -> club.aspvp.license.LicenseClient.verifyForAuthGuard()!");
            }
        }

        ClassWriter cw = createWriter();
        cn.accept(cw);
        return cw.toByteArray();
    }

    public static byte[] patchPlaceholderExpansion(byte[] classBytes) throws Exception {
        ClassReader cr = new ClassReader(classBytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        for (MethodNode mn : cn.methods) {
            if (mn.name.equals("getIdentifier") && mn.desc.equals("()Ljava/lang/String;")) {
                mn.instructions.clear();
                mn.instructions.add(new LdcInsnNode("flixcore"));
                mn.instructions.add(new InsnNode(Opcodes.ARETURN));
                mn.maxStack = 1;
                mn.maxLocals = 1;
                System.out.println("Patched org.lime.swiftCore.o.c.getIdentifier() -> flixcore");
            }
        }

        ClassWriter cw = createWriter();
        cn.accept(cw);
        return cw.toByteArray();
    }

    public static byte[] patchArenaBounds(byte[] classBytes) throws Exception {
        ClassReader cr = new ClassReader(classBytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        for (MethodNode mn : cn.methods) {
            if (mn.name.startsWith("ô0000") && mn.desc.equals("(Lorg/bukkit/Location;)Z")) {
                InsnList prefix = new InsnList();
                LabelNode continueLabel = new LabelNode();

                // if (ArenaSafetyHelper.isInsideOrNearArena(this, loc)) return true;
                prefix.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
                prefix.add(new VarInsnNode(Opcodes.ALOAD, 1)); // loc
                prefix.add(new MethodInsnNode(Opcodes.INVOKESTATIC,
                        "org/lime/swiftCore/arena/ArenaSafetyHelper",
                        "isInsideOrNearArena",
                        "(Lorg/lime/swiftCore/arena/d;Lorg/bukkit/Location;)Z",
                        false));
                prefix.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
                prefix.add(new InsnNode(Opcodes.ICONST_1));
                prefix.add(new InsnNode(Opcodes.IRETURN));
                prefix.add(continueLabel);

                mn.instructions.insert(prefix);
                System.out.println("Patched org.lime.swiftCore.arena.d boundary check with ArenaSafetyHelper!");
            }
        }

        ClassWriter cw = createWriter();
        cn.accept(cw);
        return cw.toByteArray();
    }

    public static byte[] patchBuyerInfo(byte[] classBytes) throws Exception {
        ClassReader cr = new ClassReader(classBytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        for (MethodNode mn : cn.methods) {
            if (mn.desc.equals("()Ljava/lang/String;") && (mn.access & Opcodes.ACC_STATIC) != 0) {
                mn.instructions.clear();
                mn.instructions.add(new LdcInsnNode("FlixCore v4.7.0 | Activated"));
                mn.instructions.add(new InsnNode(Opcodes.ARETURN));
                mn.maxStack = 1;
                mn.maxLocals = 0;
                System.out.println("Patched org.lime.swiftCore.m.b buyer info method");
                break;
            }
        }

        ClassWriter cw = createWriter();
        cn.accept(cw);
        return cw.toByteArray();
    }

    public static byte[] patchAllSwiftcoreCommands(byte[] classBytes, String className) {
        try {
            ClassReader cr = new ClassReader(classBytes);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);

            boolean modified = false;
            for (MethodNode mn : cn.methods) {
                for (AbstractInsnNode insn : mn.instructions) {
                    if (insn instanceof LdcInsnNode ldc && ldc.cst instanceof String s) {
                        if (s.contains("/swiftcore")) {
                            ldc.cst = s.replace("/swiftcore", "/flixcore");
                            modified = true;
                            System.out.println("Patched LDC /swiftcore -> /flixcore in " + className + "." + mn.name);
                        } else if ("swiftcore-usage".equals(s)) {
                            ldc.cst = "flixcore-usage";
                            modified = true;
                            System.out.println("Patched LDC swiftcore-usage -> flixcore-usage in " + className + "." + mn.name);
                        }
                    } else if (insn instanceof InvokeDynamicInsnNode indy) {
                        if (indy.bsmArgs != null) {
                            for (int i = 0; i < indy.bsmArgs.length; i++) {
                                if (indy.bsmArgs[i] instanceof String s && s.contains("/swiftcore")) {
                                    indy.bsmArgs[i] = s.replace("/swiftcore", "/flixcore");
                                    modified = true;
                                    System.out.println("Patched INDY bsmArg /swiftcore -> /flixcore in " + className + "." + mn.name);
                                }
                            }
                        }
                    }
                }
            }

            if (modified) {
                ClassWriter cw = createWriter();
                cn.accept(cw);
                return cw.toByteArray();
            }
        } catch (Exception e) {
            System.err.println("Warning: could not patch " + className + ": " + e.getMessage());
        }
        return classBytes;
    }

    public static byte[] patchSwiftCore(byte[] classBytes) throws Exception {
        ClassReader cr = new ClassReader(classBytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        for (MethodNode mn : cn.methods) {
            for (AbstractInsnNode insn : mn.instructions) {
                if (insn instanceof LdcInsnNode ldc) {
                    if ("SwiftCore".equals(ldc.cst)) {
                        ldc.cst = "FlixCore";
                    } else if ("SwiftCore has been enabled!".equals(ldc.cst)) {
                        ldc.cst = "FlixCore has been enabled!";
                    } else if ("SwiftCore has been disabled!".equals(ldc.cst)) {
                        ldc.cst = "FlixCore has been disabled!";
                    }
                }
            }

            if (mn.name.equals("onEnable")) {
                InsnList injection = new InsnList();
                injection.add(new VarInsnNode(Opcodes.ALOAD, 0));
                injection.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "org/lime/swiftCore/updater/FlixCoreUpdater", "init", "(Lorg/bukkit/plugin/java/JavaPlugin;)V", false));
                mn.instructions.insert(injection);
                System.out.println("Injected FlixCoreUpdater.init(this) in onEnable()");
            }

            if (mn.name.equals("registerCommands")) {
                for (AbstractInsnNode insn : mn.instructions) {
                    if (insn instanceof LdcInsnNode ldc && "swiftcore".equals(ldc.cst)) {
                        ldc.cst = "flixcore";
                        System.out.println("Replaced 'swiftcore' command registration with 'flixcore'");
                    }
                }
            }

            if (mn.name.equals("registerPlaceholders")) {
                for (AbstractInsnNode insn : mn.instructions) {
                    if (insn instanceof MethodInsnNode min && min.name.equals("register") && min.owner.equals("org/lime/swiftCore/o/c")) {
                        InsnList injection = new InsnList();
                        injection.add(new TypeInsnNode(Opcodes.NEW, "org/lime/swiftCore/o/d"));
                        injection.add(new InsnNode(Opcodes.DUP));
                        injection.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        injection.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        injection.add(new FieldInsnNode(Opcodes.GETFIELD, "org/lime/swiftCore/SwiftCore", "queueManager", "Lorg/lime/swiftCore/kit/QueueManager;"));
                        injection.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "org/lime/swiftCore/o/d", "<init>", "(Lorg/lime/swiftCore/SwiftCore;Lorg/lime/swiftCore/kit/QueueManager;)V", false));
                        injection.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "org/lime/swiftCore/o/d", "register", "()Z", false));
                        injection.add(new InsnNode(Opcodes.POP));
                        mn.instructions.insert(insn, injection);
                        System.out.println("Injected legacy 'swiftcore' dual placeholder expansion in registerPlaceholders()");
                        break;
                    }
                }
            }
        }

        ClassWriter cw = createWriter();
        cn.accept(cw);
        return cw.toByteArray();
    }

    public static void build(File sourceJar, File decompiledDir, File outputJar) throws Exception {
        System.out.println("Building " + outputJar + " from " + sourceJar + " and resources from " + decompiledDir);
        Map<String, byte[]> jarEntries = new LinkedHashMap<>();

        try (JarInputStream jis = new JarInputStream(new FileInputStream(sourceJar))) {
            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                jis.transferTo(baos);
                jarEntries.put(entry.getName(), baos.toByteArray());
            }
        }

        // Hook AuthGuard to LicenseClient (VerificationResult.isValid() evaluates actual Supabase validity)
        if (jarEntries.containsKey("org/lime/swiftCore/libs/authguard/sdk/AuthGuard.class")) {
            byte[] b = patchAuthGuard(jarEntries.get("org/lime/swiftCore/libs/authguard/sdk/AuthGuard.class"));
            jarEntries.put("org/lime/swiftCore/libs/authguard/sdk/AuthGuard.class", b);
        }
        if (jarEntries.containsKey("org/lime/swiftCore/o/c.class")) {
            byte[] b = patchPlaceholderExpansion(jarEntries.get("org/lime/swiftCore/o/c.class"));
            jarEntries.put("org/lime/swiftCore/o/c.class", b);
        }
        if (jarEntries.containsKey("org/lime/swiftCore/m/b.class")) {
            byte[] b = patchBuyerInfo(jarEntries.get("org/lime/swiftCore/m/b.class"));
            jarEntries.put("org/lime/swiftCore/m/b.class", b);
        }
        if (jarEntries.containsKey("org/lime/swiftCore/SwiftCore.class")) {
            byte[] b = patchSwiftCore(jarEntries.get("org/lime/swiftCore/SwiftCore.class"));
            jarEntries.put("org/lime/swiftCore/SwiftCore.class", b);
        }
        if (jarEntries.containsKey("org/lime/swiftCore/arena/d.class")) {
            byte[] b = patchArenaBounds(jarEntries.get("org/lime/swiftCore/arena/d.class"));
            jarEntries.put("org/lime/swiftCore/arena/d.class", b);
        }

        // Patch all classes for /swiftcore -> /flixcore references
        for (Map.Entry<String, byte[]> entry : new ArrayList<>(jarEntries.entrySet())) {
            if (entry.getKey().endsWith(".class") && !entry.getKey().startsWith("org/lime/swiftCore/libs/")) {
                byte[] transformed = patchAllSwiftcoreCommands(entry.getValue(), entry.getKey());
                if (transformed != entry.getValue()) {
                    jarEntries.put(entry.getKey(), transformed);
                }
            }
        }

        // Add d.class (the dual placeholder expansion)
        // Add d.class (the dual placeholder expansion)
        File dClass = new File(decompiledDir, "target/classes/org/lime/swiftCore/o/d.class");
        if (!dClass.exists()) {
            dClass = new File(decompiledDir.getParentFile(), "target/classes/org/lime/swiftCore/o/d.class");
        }
        if (!dClass.exists()) {
            dClass = new File("/tmp/org/lime/swiftCore/o/d.class");
        }
        if (dClass.exists()) {
            jarEntries.put("org/lime/swiftCore/o/d.class", Files.readAllBytes(dClass.toPath()));
            System.out.println("Included org/lime/swiftCore/o/d.class into jar!");
        }

        // Add FlixCoreUpdater classes
        File updaterDir = new File(decompiledDir, "target/classes/org/lime/swiftCore/updater");
        if (!updaterDir.exists()) {
            updaterDir = new File(decompiledDir.getParentFile(), "target/classes/org/lime/swiftCore/updater");
        }
        if (updaterDir.exists() && updaterDir.isDirectory()) {
            File[] files = updaterDir.listFiles((dir, name) -> name.endsWith(".class"));
            if (files != null) {
                for (File f : files) {
                    jarEntries.put("org/lime/swiftCore/updater/" + f.getName(), Files.readAllBytes(f.toPath()));
                    System.out.println("Included: org/lime/swiftCore/updater/" + f.getName());
                }
            }
        }

        // Add ArenaSafetyHelper class
        File safetyClass = new File(decompiledDir, "target/classes/org/lime/swiftCore/arena/ArenaSafetyHelper.class");
        if (!safetyClass.exists()) {
            safetyClass = new File(decompiledDir.getParentFile(), "target/classes/org/lime/swiftCore/arena/ArenaSafetyHelper.class");
        }
        if (safetyClass.exists()) {
            jarEntries.put("org/lime/swiftCore/arena/ArenaSafetyHelper.class", Files.readAllBytes(safetyClass.toPath()));
            System.out.println("Included org/lime/swiftCore/arena/ArenaSafetyHelper.class into jar!");
        }

        // Add LicenseClient classes
        File licenseDir = new File(decompiledDir, "target/classes/club/aspvp/license");
        if (!licenseDir.exists()) {
            licenseDir = new File(decompiledDir.getParentFile(), "target/classes/club/aspvp/license");
        }
        if (licenseDir.exists() && licenseDir.isDirectory()) {
            File[] files = licenseDir.listFiles((dir, name) -> name.endsWith(".class"));
            if (files != null) {
                for (File f : files) {
                    jarEntries.put("club/aspvp/license/" + f.getName(), Files.readAllBytes(f.toPath()));
                    System.out.println("Included: club/aspvp/license/" + f.getName());
                }
            }
        }

        // Add PracticeBot stub classes
        File botApiDir = new File(decompiledDir, "target/classes/com/lime/practicebot/api");
        if (!botApiDir.exists()) {
            botApiDir = new File(decompiledDir.getParentFile(), "target/classes/com/lime/practicebot/api");
        }
        if (botApiDir.exists() && botApiDir.isDirectory()) {
            File[] files = botApiDir.listFiles((dir, name) -> name.endsWith(".class"));
            if (files != null) {
                for (File f : files) {
                    jarEntries.put("com/lime/practicebot/api/" + f.getName(), Files.readAllBytes(f.toPath()));
                    System.out.println("Included stub: com/lime/practicebot/api/" + f.getName());
                }
            }
        }

        // Replace all resources with files from decompiledDir
        Path decompRoot = decompiledDir.toPath();
        Files.walk(decompRoot)
            .filter(Files::isRegularFile)
            .forEach(p -> {
                String relPath = decompRoot.relativize(p).toString().replace('\\', '/');
                if (relPath.endsWith(".yml") || relPath.endsWith(".properties") || relPath.startsWith("menus/") || relPath.startsWith("miscellaneous/") || relPath.startsWith("database/")) {
                    try {
                        byte[] fileBytes = Files.readAllBytes(p);
                        jarEntries.put(relPath, fileBytes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        // Write output jar
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(outputJar))) {
            for (Map.Entry<String, byte[]> e : jarEntries.entrySet()) {
                JarEntry entry = new JarEntry(e.getKey());
                jos.putNextEntry(entry);
                jos.write(e.getValue());
                jos.closeEntry();
            }
        }

        System.out.println("SUCCESS: " + outputJar.getName() + " created successfully (" + outputJar.length() + " bytes)!");
    }

    public static void main(String[] args) throws Exception {
        File srcJar = new File(args[0]);
        File decDir = new File(args[1]);
        File outJar = new File(args[2]);
        build(srcJar, decDir, outJar);
    }
}
