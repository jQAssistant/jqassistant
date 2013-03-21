package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import com.buschmais.jqassistant.store.model.ClassDescriptor;

public class MethodVisitor extends AbstractVisitor implements org.objectweb.asm.MethodVisitor {
    // MethodVisitor

    private ClassDescriptor classDescriptor;

    protected MethodVisitor(DependencyModel model, ClassDescriptor classDescriptor) {
        super(model);
        this.classDescriptor = classDescriptor;
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
        getModel().addDependency(classDescriptor, getType(desc));
        return new AnnotationVisitor(getModel(), classDescriptor);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        getModel().addDependency(classDescriptor, getType(Type.getObjectType(type)));
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        getModel().addDependency(classDescriptor, getInternalName(owner));
        getModel().addDependency(classDescriptor, getType(desc));
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
        getModel().addDependency(classDescriptor, getInternalName(owner));
        addMethodDesc(desc);
    }

    @Override
    public void visitLdcInsn(final Object cst) {
        if (cst instanceof Type) {
            getModel().addDependency(classDescriptor, getType((Type) cst));
        }
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        getModel().addDependency(classDescriptor, getType(desc));
    }

    @Override
    public void
            visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
        getModel().addDependency(classDescriptor, getTypeSignature(signature));
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return new AnnotationVisitor(getModel(), classDescriptor);
    }

    @Override
    public void visitCode() {
    }

    @Override
    public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack, final Object[] stack) {
    }

    @Override
    public void visitInsn(final int opcode) {
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
    }

    @Override
    public void visitVarInsn(final int opcode, final int var) {
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
    }

    @Override
    public void visitLabel(final Label label) {
    }

    @Override
    public void visitIincInsn(final int var, final int increment) {
    }

    @Override
    public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label[] labels) {
    }

    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
    }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        getModel().addDependency(classDescriptor, getInternalName(type));
    }

    @Override
    public void visitLineNumber(final int line, final Label start) {
    }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        getModel().addDependency(classDescriptor, getType(desc));
        return new AnnotationVisitor(getModel(), classDescriptor);
    }

    private void addMethodDesc(final String desc) {
        getModel().addDependency(classDescriptor, getType(Type.getReturnType(desc)));
        Type[] types = Type.getArgumentTypes(desc);
        for (int i = 0; i < types.length; i++) {
            getModel().addDependency(classDescriptor, getType(types[i]));
        }
    }
}
