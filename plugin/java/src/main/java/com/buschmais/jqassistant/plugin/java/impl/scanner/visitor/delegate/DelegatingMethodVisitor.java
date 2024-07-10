package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.delegate;

import java.util.List;

import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

import org.objectweb.asm.*;

public class DelegatingMethodVisitor extends MethodVisitor {

    private Delegator<MethodVisitor> delegator;

    public DelegatingMethodVisitor(List<MethodVisitor> visitors) {
        super(VisitorHelper.ASM_OPCODES);
        this.delegator = new Delegator<>(visitors);
    }

    public void visitParameter(String name, int access) {
        delegator.accept(visitor -> visitor.visitParameter(name, access));
    }

    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        delegator.accept(visitor -> visitor.visitAnnotableParameterCount(parameterCount, visible));
    }

    public void visitAttribute(Attribute attribute) {
        delegator.accept(visitor -> visitor.visitAttribute(attribute));
    }

    public void visitCode() {
        delegator.accept(visitor -> visitor.visitCode());
    }

    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        delegator.accept(visitor -> visitor.visitFrame(type, numLocal, local, numStack, stack));
    }

    public void visitInsn(int opcode) {
        delegator.accept(visitor -> visitor.visitInsn(opcode));
    }

    public void visitIntInsn(int opcode, int operand) {
        delegator.accept(visitor -> visitor.visitIntInsn(opcode, operand));
    }

    public void visitVarInsn(int opcode, int var) {
        delegator.accept(visitor -> visitor.visitVarInsn(opcode, var));
    }

    public void visitTypeInsn(int opcode, String type) {
        delegator.accept(visitor -> visitor.visitTypeInsn(opcode, type));
    }

    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        delegator.accept(visitor -> visitor.visitFieldInsn(opcode, owner, name, descriptor));
    }

    public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        delegator.accept(visitor -> visitor.visitMethodInsn(opcode, owner, name, descriptor));
    }

    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        delegator.accept(visitor -> visitor.visitMethodInsn(opcode, owner, name, descriptor, isInterface));
    }

    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        delegator.accept(visitor -> visitor.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments));
    }

    public void visitJumpInsn(int opcode, Label label) {
        delegator.accept(visitor -> visitor.visitJumpInsn(opcode, label));
    }

    public void visitLabel(Label label) {
        delegator.accept(visitor -> visitor.visitLabel(label));
    }

    public void visitLdcInsn(Object value) {
        delegator.accept(visitor -> visitor.visitLdcInsn(value));
    }

    public void visitIincInsn(int var, int increment) {
        delegator.accept(visitor -> visitor.visitIincInsn(var, increment));
    }

    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        delegator.accept(visitor -> visitor.visitTableSwitchInsn(min, max, dflt, labels));
    }

    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        delegator.accept(visitor -> visitor.visitLookupSwitchInsn(dflt, keys, labels));
    }

    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        delegator.accept(visitor -> visitor.visitMultiANewArrayInsn(descriptor, numDimensions));
    }

    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        delegator.accept(visitor -> visitor.visitTryCatchBlock(start, end, handler, type));
    }

    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        delegator.accept(visitor -> visitor.visitLocalVariable(name, descriptor, signature, start, end, index));
    }

    public void visitLineNumber(int line, Label start) {
        delegator.accept(visitor -> visitor.visitLineNumber(line, start));
    }

    public void visitMaxs(int maxStack, int maxLocals) {
        delegator.accept(visitor -> visitor.visitMaxs(maxStack, maxLocals));
    }

    public void visitEnd() {
        delegator.accept(visitor -> visitor.visitEnd());
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return new DelegatingAnnotationVisitor(delegator.apply(visitor -> visitor.visitAnnotationDefault()));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new DelegatingAnnotationVisitor(delegator.apply(visitor -> visitor.visitAnnotation(descriptor, visible)));
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return new DelegatingAnnotationVisitor(delegator.apply(visitor -> visitor.visitTypeAnnotation(typeRef, typePath, descriptor, visible)));
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        return new DelegatingAnnotationVisitor(delegator.apply(visitor -> visitor.visitParameterAnnotation(parameter, descriptor, visible)));
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return new DelegatingAnnotationVisitor(delegator.apply(visitor -> visitor.visitInsnAnnotation(typeRef, typePath, descriptor, visible)));
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return new DelegatingAnnotationVisitor(delegator.apply(x -> visitTryCatchAnnotation(typeRef, typePath, descriptor, visible)));
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor,
        boolean visible) {
        return new DelegatingAnnotationVisitor(
            delegator.apply(visitor -> visitor.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible)));
    }
}
