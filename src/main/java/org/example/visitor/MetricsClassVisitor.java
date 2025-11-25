package org.example.visitor;

import org.example.dto.ClassMetrics;
import org.example.dto.MethodMetrics;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.Map;

public final class MetricsClassVisitor extends ClassVisitor {

    private final Map<String, ClassMetrics> classesByName;
    private ClassMetrics currentClassMetrics;

    public MetricsClassVisitor(Map<String, ClassMetrics> classesByName) {
        super(Opcodes.ASM9);
        this.classesByName = classesByName;
    }

    @Override
    public void visit(
            int version,
            int access,
            String name,
            String signature,
            String superName,
            String[] interfaces
    ) {
        currentClassMetrics = new ClassMetrics();
        currentClassMetrics.setName(name);
        currentClassMetrics.setSuperName(superName);
        if (interfaces != null && interfaces.length > 0) {
            currentClassMetrics.getInterfaces().addAll(Arrays.asList(interfaces));
        }
        classesByName.put(name, currentClassMetrics);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(
            int access,
            String name,
            String descriptor,
            String signature,
            Object value
    ) {
        if (currentClassMetrics != null) {
            currentClassMetrics.incrementFieldCount();
        }
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(
            int access,
            String name,
            String descriptor,
            String signature,
            String[] exceptions
    ) {
        if (currentClassMetrics == null) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        MethodMetrics methodMetrics = new MethodMetrics(name, descriptor);
        currentClassMetrics.addMethod(methodMetrics);

        MethodVisitor baseVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
        return new AbcMethodVisitor(Opcodes.ASM9, baseVisitor, methodMetrics);
    }
}
