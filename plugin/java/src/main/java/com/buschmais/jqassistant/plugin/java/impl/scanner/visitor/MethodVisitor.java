package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;

import com.buschmais.jqassistant.plugin.java.api.model.AnnotationValueDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ParameterDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;

public class MethodVisitor extends org.objectweb.asm.MethodVisitor {

    private TypeCache.CachedType containingType;
    private MethodDescriptor methodDescriptor;
    private VisitorHelper visitorHelper;
    private int syntheticParameters = 0;
    private int line;

    protected MethodVisitor(TypeCache.CachedType containingType, MethodDescriptor methodDescriptor, VisitorHelper visitorHelper) {
        super(Opcodes.ASM5);
        this.containingType = containingType;
        this.methodDescriptor = methodDescriptor;
        this.visitorHelper = visitorHelper;
    }

    @Override
    public org.objectweb.asm.AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
        String annotationType = SignatureHelper.getType(desc);
        if ("java.lang.Synthetic".equals(annotationType)) {
            // Ignore synthetic parameters add the start of the signature.
            syntheticParameters++;
            return null;
        }
        ParameterDescriptor parameterDescriptor = visitorHelper.getParameterDescriptor(methodDescriptor, parameter - syntheticParameters);
        AnnotationValueDescriptor annotationDescriptor = visitorHelper.addAnnotation(containingType, parameterDescriptor, SignatureHelper.getType(desc));
        return new AnnotationVisitor(containingType, annotationDescriptor, visitorHelper);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        visitorHelper.getType(SignatureHelper.getObjectType(type), containingType);
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        String fieldSignature = SignatureHelper.getFieldSignature(name, desc);
        TypeCache.CachedType targetType = visitorHelper.getType(SignatureHelper.getObjectType(owner), containingType);
        FieldDescriptor fieldDescriptor = visitorHelper.getFieldDescriptor(targetType, fieldSignature);
        switch (opcode) {
        case Opcodes.GETFIELD:
        case Opcodes.GETSTATIC:
            visitorHelper.addReads(methodDescriptor, line, fieldDescriptor);
            break;
        case Opcodes.PUTFIELD:
        case Opcodes.PUTSTATIC:
            visitorHelper.addWrites(methodDescriptor, line, fieldDescriptor);
            break;
        }
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc, boolean itf) {
        String methodSignature = SignatureHelper.getMethodSignature(name, desc);
        TypeCache.CachedType targetType = visitorHelper.getType(SignatureHelper.getObjectType(owner), containingType);
        MethodDescriptor invokedMethodDescriptor = visitorHelper.getMethodDescriptor(targetType, methodSignature);
        visitorHelper.addInvokes(methodDescriptor, line, invokedMethodDescriptor);
    }

    @Override
    public void visitLdcInsn(final Object cst) {
        if (cst instanceof Type) {
            visitorHelper.getType(SignatureHelper.getType((Type) cst), containingType);
        }
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        visitorHelper.getType(SignatureHelper.getType(desc), containingType);
    }

    @Override
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
        if (signature != null) {
            new SignatureReader(signature).accept(new DependentTypeSignatureVisitor(containingType, visitorHelper));
        }
    }

    @Override
    public org.objectweb.asm.AnnotationVisitor visitAnnotationDefault() {
        return new AnnotationDefaultVisitor(containingType, this.methodDescriptor, visitorHelper);
    }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        if (type != null) {
            String fullQualifiedName = SignatureHelper.getObjectType(type);
            visitorHelper.getType(fullQualifiedName, containingType);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        AnnotationValueDescriptor annotationDescriptor = visitorHelper.addAnnotation(containingType, methodDescriptor, SignatureHelper.getType(desc));
        return new AnnotationVisitor(containingType, annotationDescriptor, visitorHelper);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        this.line = line;
    }
}
