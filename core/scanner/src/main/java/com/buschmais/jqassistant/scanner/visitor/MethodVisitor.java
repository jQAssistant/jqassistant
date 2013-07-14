package com.buschmais.jqassistant.scanner.visitor;

import com.buschmais.jqassistant.scanner.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.store.api.model.DependentDescriptor;
import com.buschmais.jqassistant.store.api.model.MethodDescriptor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;

public class MethodVisitor extends AbstractVisitor implements org.objectweb.asm.MethodVisitor {

    private final MethodDescriptor methodDescriptor;

    protected MethodVisitor(MethodDescriptor methodDescriptor, DescriptorResolverFactory resolverFactory) {
        super(resolverFactory);
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
        addAnnotation(methodDescriptor, getType(desc));
        return new AnnotationVisitor(methodDescriptor, getResolverFactory());
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        addDependency(methodDescriptor, getType(Type.getObjectType(type)));
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        addDependency(methodDescriptor, owner);
        addDependency(methodDescriptor, getType(desc));
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
        addDependency(methodDescriptor, owner);
        addMethodDesc(desc);
    }

    @Override
    public void visitLdcInsn(final Object cst) {
        if (cst instanceof Type) {
            addDependency(methodDescriptor, getType((Type) cst));
        }
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        addDependency(methodDescriptor, getType(desc));
    }

    @Override
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
        if (signature != null) {
            new SignatureReader(signature).accept(new DependentSignatureVisitor<DependentDescriptor>(methodDescriptor, getResolverFactory()));
        }
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        return new AnnotationVisitor(methodDescriptor, getResolverFactory());
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
        addDependency(methodDescriptor, type);
    }

    @Override
    public void visitLineNumber(final int line, final Label start) {
    }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        addAnnotation(methodDescriptor, getType(desc));
        return new AnnotationVisitor(methodDescriptor, getResolverFactory());
    }

    private void addMethodDesc(final String desc) {
        addDependency(methodDescriptor, getType(Type.getReturnType(desc)));
        Type[] types = Type.getArgumentTypes(desc);
        for (int i = 0; i < types.length; i++) {
            addDependency(methodDescriptor, getType(types[i]));
        }
    }

    @Override
    public void visitAttribute(Attribute arg0) {
    }

    @Override
    public void visitEnd() {
    }
}
