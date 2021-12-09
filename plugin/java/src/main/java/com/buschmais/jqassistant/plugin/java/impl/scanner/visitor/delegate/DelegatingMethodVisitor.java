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
        delegator.delegateToConsumer(visitor -> visitor.visitParameter(name, access));
    }

    public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
        delegator.delegateToConsumer(visitor -> visitor.visitAnnotableParameterCount(parameterCount, visible));
    }

    public void visitAttribute(Attribute attribute) {
        delegator.delegateToConsumer(visitor -> visitor.visitAttribute(attribute));
    }

    public void visitCode() {
        delegator.delegateToConsumer(visitor -> visitor.visitCode());
    }

    public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        delegator.delegateToConsumer(visitor -> visitor.visitFrame(type, numLocal, local, numStack, stack));
    }

    public void visitInsn(int opcode) {
        delegator.delegateToConsumer(visitor -> visitor.visitInsn(opcode));
    }

    public void visitIntInsn(int opcode, int operand) {
        delegator.delegateToConsumer(visitor -> visitor.visitIntInsn(opcode, operand));
    }

    public void visitVarInsn(int opcode, int var) {
        delegator.delegateToConsumer(visitor -> visitor.visitVarInsn(opcode, var));
    }

    public void visitTypeInsn(int opcode, String type) {
        delegator.delegateToConsumer(visitor -> visitor.visitTypeInsn(opcode, type));
    }

    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        delegator.delegateToConsumer(visitor -> visitor.visitFieldInsn(opcode, owner, name, descriptor));
    }

    public void visitMethodInsn(int opcode, String owner, String name, String descriptor) {
        delegator.delegateToConsumer(visitor -> visitor.visitMethodInsn(opcode, owner, name, descriptor));
    }

    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        delegator.delegateToConsumer(visitor -> visitor.visitMethodInsn(opcode, owner, name, descriptor, isInterface));
    }

    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        delegator.delegateToConsumer(visitor -> visitor.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments));
    }

    public void visitJumpInsn(int opcode, Label label) {
        delegator.delegateToConsumer(visitor -> visitor.visitJumpInsn(opcode, label));
    }

    public void visitLabel(Label label) {
        delegator.delegateToConsumer(visitor -> visitor.visitLabel(label));
    }

    public void visitLdcInsn(Object value) {
        delegator.delegateToConsumer(visitor -> visitor.visitLdcInsn(value));
    }

    public void visitIincInsn(int var, int increment) {
        delegator.delegateToConsumer(visitor -> visitor.visitIincInsn(var, increment));
    }

    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        delegator.delegateToConsumer(visitor -> visitor.visitTableSwitchInsn(min, max, dflt, labels));
    }

    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        delegator.delegateToConsumer(visitor -> visitor.visitLookupSwitchInsn(dflt, keys, labels));
    }

    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        delegator.delegateToConsumer(visitor -> visitor.visitMultiANewArrayInsn(descriptor, numDimensions));
    }

    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        delegator.delegateToConsumer(visitor -> visitor.visitTryCatchBlock(start, end, handler, type));
    }

    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        delegator.delegateToConsumer(visitor -> visitor.visitLocalVariable(name, descriptor, signature, start, end, index));
    }

    public void visitLineNumber(int line, Label start) {
        delegator.delegateToConsumer(visitor -> visitor.visitLineNumber(line, start));
    }

    public void visitMaxs(int maxStack, int maxLocals) {
        delegator.delegateToConsumer(visitor -> visitor.visitMaxs(maxStack, maxLocals));
    }

    public void visitEnd() {
        delegator.delegateToConsumer(visitor -> visitor.visitEnd());
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return new DelegatingAnnotationVisitor(delegator.delegateToFunction(visitor -> visitor.visitAnnotationDefault()));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new DelegatingAnnotationVisitor(delegator.delegateToFunction(visitor -> visitor.visitAnnotation(descriptor, visible)));
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return new DelegatingAnnotationVisitor(delegator.delegateToFunction(visitor -> visitor.visitTypeAnnotation(typeRef, typePath, descriptor, visible)));
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
        return new DelegatingAnnotationVisitor(delegator.delegateToFunction(visitor -> visitor.visitParameterAnnotation(parameter, descriptor, visible)));
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return new DelegatingAnnotationVisitor(delegator.delegateToFunction(visitor -> visitor.visitInsnAnnotation(typeRef, typePath, descriptor, visible)));
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return new DelegatingAnnotationVisitor(delegator.delegateToFunction(x -> visitTryCatchAnnotation(typeRef, typePath, descriptor, visible)));
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String descriptor,
        boolean visible) {
        return new DelegatingAnnotationVisitor(
            delegator.delegateToFunction(visitor -> visitor.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible)));
    }
}
