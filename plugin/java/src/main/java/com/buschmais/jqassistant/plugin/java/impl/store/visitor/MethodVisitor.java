package com.buschmais.jqassistant.plugin.java.impl.store.visitor;

import com.buschmais.jqassistant.plugin.java.api.SignatureHelper;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.*;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;

public class MethodVisitor extends org.objectweb.asm.MethodVisitor {

    private MethodDescriptor methodDescriptor;
    private VisitorHelper visitorHelper;

    protected MethodVisitor(MethodDescriptor methodDescriptor, VisitorHelper visitorHelper) {
        super(Opcodes.ASM4);
        this.methodDescriptor = methodDescriptor;
        this.visitorHelper = visitorHelper;
    }

    @Override
    public org.objectweb.asm.AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
        ParameterDescriptor parameterDescriptor = methodDescriptor.findParameter(parameter);
        AnnotationValueDescriptor annotationDescriptor = visitorHelper.addAnnotation(parameterDescriptor, SignatureHelper.getType(desc));
        return new AnnotationVisitor(annotationDescriptor, visitorHelper);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        visitorHelper.addDependency(methodDescriptor, SignatureHelper.getObjectType(type));
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        String fieldSignature = SignatureHelper.getFieldSignature(name, desc);
        TypeDescriptor typeDescriptor = visitorHelper.getTypeDescriptor(SignatureHelper.getObjectType(owner));
        FieldDescriptor fieldDescriptor = visitorHelper.getFieldDescriptor(typeDescriptor, fieldSignature);
        switch (opcode) {
            case Opcodes.GETFIELD:
            case Opcodes.GETSTATIC:
                this.methodDescriptor.addReads(fieldDescriptor);
                break;
            case Opcodes.PUTFIELD:
            case Opcodes.PUTSTATIC:
                this.methodDescriptor.addWrites(fieldDescriptor);
                break;
        }
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
        String methodSignature = SignatureHelper.getMethodSignature(name, desc);
        TypeDescriptor typeDescriptor = visitorHelper.getTypeDescriptor(SignatureHelper.getObjectType(owner));
        MethodDescriptor invokedMethodDescriptor = visitorHelper.getMethodDescriptor(typeDescriptor, methodSignature);
        this.methodDescriptor.addInvokes(invokedMethodDescriptor);
        visitorHelper.addDependency(methodDescriptor, SignatureHelper.getType(Type.getReturnType(desc)));
    }

    @Override
    public void visitLdcInsn(final Object cst) {
        if (cst instanceof Type) {
            visitorHelper.addDependency(methodDescriptor, SignatureHelper.getType((Type) cst));
        }
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        visitorHelper.addDependency(methodDescriptor, SignatureHelper.getType(desc));
    }

    @Override
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end,
                                   final int index) {
        if (signature != null) {
            new SignatureReader(signature).accept(new DependentTypeSignatureVisitor(methodDescriptor, visitorHelper));
        }
    }

    @Override
    public org.objectweb.asm.AnnotationVisitor visitAnnotationDefault() {
        return new AnnotationDefaultVisitor(this.methodDescriptor, visitorHelper);
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
        visitorHelper.addDependency(methodDescriptor, type);
    }

    @Override
    public void visitLineNumber(final int line, final Label start) {
    }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        AnnotationValueDescriptor annotationDescriptor = visitorHelper.addAnnotation(methodDescriptor, SignatureHelper.getType(desc));
        return new AnnotationVisitor(annotationDescriptor, visitorHelper);
    }

    @Override
    public void visitAttribute(Attribute arg0) {
    }

    @Override
    public void visitEnd() {
    }
}
