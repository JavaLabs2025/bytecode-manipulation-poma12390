package org.example.calculator;

import org.example.dto.ProjectMetrics;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class JarMetricsCalculator {

    private final Path jarPath;

    public JarMetricsCalculator(Path jarPath) {
        this.jarPath = Objects.requireNonNull(jarPath, "jarPath");
    }

    public ProjectMetrics analyze() throws IOException {
        Map<String, ClassInfo> classes = loadClasses();

        if (classes.isEmpty()) {
            return new ProjectMetrics(
                    0,
                    0,
                    0.0,
                    0,
                    0.0,
                    0.0,
                    0.0
            );
        }

        Map<String, Integer> depthCache = new HashMap<>();

        int classCount = 0;
        double inheritanceDepthSum = 0.0;
        int maxInheritanceDepth = 0;

        int totalFields = 0;
        int totalOverriddenMethods = 0;

        int totalMethodsForAbc = 0;
        int totalAssignmentsForAbc = 0;

        for (ClassInfo classInfo : classes.values()) {
            if (classInfo.isInterface) {
                continue;
            }

            classCount++;

            int depth = computeInheritanceDepth(classInfo.name, classes, depthCache);
            inheritanceDepthSum += depth;
            if (depth > maxInheritanceDepth) {
                maxInheritanceDepth = depth;
            }

            totalFields += classInfo.fieldCount;

            int overriddenHere = countOverriddenMethods(classInfo, classes);
            totalOverriddenMethods += overriddenHere;

            for (MethodNode methodNode : classInfo.methods) {
                if (isMethodForAbc(methodNode)) {
                    int assignments = countAssignmentsInMethod(methodNode);
                    totalAssignmentsForAbc += assignments;
                    totalMethodsForAbc++;
                }
            }
        }

        double averageInheritanceDepth = classCount == 0 ? 0.0 : inheritanceDepthSum / classCount;
        double averageFieldCountPerClass = classCount == 0 ? 0.0 : (double) totalFields / classCount;
        double averageOverriddenMethodsPerClass = classCount == 0 ? 0.0 : (double) totalOverriddenMethods / classCount;
        double abcAverageAssignmentsPerMethod = totalMethodsForAbc == 0 ? 0.0 : (double) totalAssignmentsForAbc / totalMethodsForAbc;

        return new ProjectMetrics(
                classCount,
                maxInheritanceDepth,
                averageInheritanceDepth,
                totalAssignmentsForAbc,
                abcAverageAssignmentsPerMethod,
                averageOverriddenMethodsPerClass,
                averageFieldCountPerClass
        );
    }

    // Все классы в ClassInfo
    private Map<String, ClassInfo> loadClasses() throws IOException {
        Map<String, ClassInfo> result = new HashMap<>();

        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }

                try (InputStream is = jarFile.getInputStream(entry)) {
                    ClassReader classReader = new ClassReader(is);
                    ClassNode classNode = new ClassNode();
                    classReader.accept(classNode, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

                    boolean isInterface = (classNode.access & Opcodes.ACC_INTERFACE) != 0;

                    List<MethodNode> methods = new ArrayList<>();
                    if (classNode.methods != null) {
                        for (Object m : classNode.methods) {
                            methods.add((MethodNode) m);
                        }
                    }

                    int fieldCount = classNode.fields == null ? 0 : classNode.fields.size();

                    ClassInfo info = new ClassInfo(
                            classNode.name,
                            classNode.superName,
                            isInterface,
                            fieldCount,
                            methods
                    );
                    result.put(classNode.name, info);
                }
            }
        }

        return result;
    }

    private int computeInheritanceDepth(
            String className,
            Map<String, ClassInfo> classes,
            Map<String, Integer> depthCache
    ) {
        if (className == null) {
            return 0;
        }
        Integer cached = depthCache.get(className);
        if (cached != null) {
            return cached;
        }

        ClassInfo info = classes.get(className);
        if (info == null) {
            depthCache.put(className, 0);
            return 0;
        }

        if (info.superName == null || "java/lang/Object".equals(info.superName)) {
            depthCache.put(className, 0);
            return 0;
        }

        int depth = 1 + computeInheritanceDepth(info.superName, classes, depthCache);
        depthCache.put(className, depth);
        return depth;
    }

    private int countOverriddenMethods(ClassInfo classInfo, Map<String, ClassInfo> classes) {
        Set<String> superMethodSignatures = new HashSet<>();

        String superName = classInfo.superName;
        while (superName != null) {
            ClassInfo superInfo = classes.get(superName);
            if (superInfo == null) {
                break;
            }

            for (MethodNode m : superInfo.methods) {
                if (isConstructorOrClassInitializer(m)) {
                    continue;
                }
                String signature = m.name + m.desc;
                superMethodSignatures.add(signature);
            }

            superName = superInfo.superName;
        }

        int overridden = 0;
        for (MethodNode m : classInfo.methods) {
            if (isConstructorOrClassInitializer(m)) {
                continue;
            }
            String signature = m.name + m.desc;
            if (superMethodSignatures.contains(signature)) {
                overridden++;
            }
        }

        return overridden;
    }

    private boolean isMethodForAbc(MethodNode methodNode) {
        if ((methodNode.access & Opcodes.ACC_ABSTRACT) != 0) {
            return false;
        }
        if ((methodNode.access & Opcodes.ACC_SYNTHETIC) != 0) {
            return false;
        }
        return !isConstructorOrClassInitializer(methodNode);
    }

    private boolean isConstructorOrClassInitializer(MethodNode methodNode) {
        String name = methodNode.name;
        return "<init>".equals(name) || "<clinit>".equals(name);
    }

    private int countAssignmentsInMethod(MethodNode methodNode) {
        int count = 0;
        for (AbstractInsnNode insn = methodNode.instructions.getFirst();
             insn != null;
             insn = insn.getNext()) {

            int opcode = insn.getOpcode();
            if (opcode < 0) {
                continue;
            }

            if (isStoreOpcode(opcode) || opcode == Opcodes.IINC) {
                count++;
            }
        }
        return count;
    }

    private boolean isStoreOpcode(int opcode) {
        return opcode == Opcodes.ISTORE
                || opcode == Opcodes.LSTORE
                || opcode == Opcodes.FSTORE
                || opcode == Opcodes.DSTORE
                || opcode == Opcodes.ASTORE;
    }

        private record ClassInfo(
                String name,
                String superName,
                boolean isInterface,
                int fieldCount,
                List<MethodNode> methods
    ) {
    }
}
