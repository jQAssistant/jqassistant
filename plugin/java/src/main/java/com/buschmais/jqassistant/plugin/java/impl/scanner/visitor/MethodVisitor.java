package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;

import com.buschmais.jqassistant.plugin.java.api.model.AnnotationValueDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ParameterDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;

public class MethodVisitor extends org.objectweb.asm.MethodVisitor {

    private TypeDescriptor typeDescriptor;
    private MethodDescriptor methodDescriptor;
    private VisitorHelper visitorHelper;
    private int syntheticParameters = 0;
    private int line;

    protected MethodVisitor(TypeDescriptor typeDescriptor, MethodDescriptor methodDescriptor, VisitorHelper visitorHelper) {
        super(Opcodes.ASM5);
        this.typeDescriptor = typeDescriptor;
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
        ParameterDescriptor parameterDescriptor = methodDescriptor.findParameter(parameter - syntheticParameters);
        AnnotationValueDescriptor annotationDescriptor = visitorHelper.addAnnotation(parameterDescriptor, SignatureHelper.getType(desc));
        return new AnnotationVisitor(annotationDescriptor, visitorHelper);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        visitorHelper.addDependency(typeDescriptor, methodDescriptor, SignatureHelper.getObjectType(type));
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        String fieldSignature = SignatureHelper.getFieldSignature(name, desc);
        VisitorHelper.CachedType cachedType = visitorHelper.getType(SignatureHelper.getObjectType(owner));
        FieldDescriptor fieldDescriptor = visitorHelper.getFieldDescriptor(cachedType, fieldSignature);
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
        VisitorHelper.CachedType cachedType = visitorHelper.getType(SignatureHelper.getObjectType(owner));
        MethodDescriptor invokedMethodDescriptor = visitorHelper.getMethodDescriptor(cachedType, methodSignature);
        visitorHelper.addInvokes(methodDescriptor, line, invokedMethodDescriptor);
        visitorHelper.addDependency(typeDescriptor, methodDescriptor, SignatureHelper.getType(Type.getReturnType(desc)));
    }

    @Override
    public void visitLdcInsn(final Object cst) {
        if (cst instanceof Type) {
            visitorHelper.addDependency(typeDescriptor, methodDescriptor, SignatureHelper.getType((Type) cst));
        }
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        visitorHelper.addDependency(typeDescriptor, methodDescriptor, SignatureHelper.getType(desc));
    }

    @Override
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start, final Label end, final int index) {
        if (signature != null) {
            new SignatureReader(signature).accept(new DependentTypeSignatureVisitor(methodDescriptor, visitorHelper));
        }
    }

    @Override
    public org.objectweb.asm.AnnotationVisitor visitAnnotationDefault() {
        return new AnnotationDefaultVisitor(this.methodDescriptor, visitorHelper);
    }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        if (type != null) {
            visitorHelper.addDependency(typeDescriptor, methodDescriptor, SignatureHelper.getType(type));
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        AnnotationValueDescriptor annotationDescriptor = visitorHelper.addAnnotation(methodDescriptor, SignatureHelper.getType(desc));
        return new AnnotationVisitor(annotationDescriptor, visitorHelper);
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        this.line = line;
    }
}
