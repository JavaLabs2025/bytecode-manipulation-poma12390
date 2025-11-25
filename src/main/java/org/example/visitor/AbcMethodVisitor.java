package org.example.visitor;

import org.example.dto.MethodMetrics;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class AbcMethodVisitor extends MethodVisitor implements Opcodes {

    private final MethodMetrics methodMetrics;

    public AbcMethodVisitor(int api, MethodVisitor methodVisitor, MethodMetrics methodMetrics) {
        super(api, methodVisitor);
        this.methodMetrics = methodMetrics;
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        if (opcode == ISTORE
                || opcode == LSTORE
                || opcode == FSTORE
                || opcode == DSTORE
                || opcode == ASTORE) {
            methodMetrics.incrementAbcAssignments();
        }
        super.visitVarInsn(opcode, varIndex);
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        methodMetrics.incrementAbcAssignments();
        super.visitIincInsn(varIndex, increment);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        switch (opcode) {
            case IFEQ:
            case IFNE:
            case IFLT:
            case IFGE:
            case IFGT:
            case IFLE:
            case IF_ICMPEQ:
            case IF_ICMPNE:
            case IF_ICMPLT:
            case IF_ICMPGE:
            case IF_ICMPGT:
            case IF_ICMPLE:
            case IF_ACMPEQ:
            case IF_ACMPNE:
            case IFNULL:
            case IFNONNULL:
                methodMetrics.incrementAbcConditions();
                break;
            case GOTO:
            case JSR:
                methodMetrics.incrementAbcBranches();
                break;
            default:
                break;
        }
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        methodMetrics.incrementAbcBranches();
        methodMetrics.addAbcConditions(labels.length + 1);
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        methodMetrics.incrementAbcBranches();
        methodMetrics.addAbcConditions(labels.length + 1);
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitInsn(int opcode) {
        switch (opcode) {
            case RETURN:
            case IRETURN:
            case FRETURN:
            case ARETURN:
            case LRETURN:
            case DRETURN:
                methodMetrics.incrementAbcBranches();
                break;
            default:
                break;
        }
        super.visitInsn(opcode);
    }

    @Override
    public void visitMethodInsn(
            int opcode,
            String owner,
            String name,
            String descriptor,
            boolean isInterface
    ) {
        methodMetrics.incrementAbcBranches();
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }
}
