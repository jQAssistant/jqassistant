package com.buschmais.jqassistant.plugin.java.impl.store.visitor;

import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ParameterDescriptor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Visitor for method signatures.
 */
public class MethodSignatureVisitor extends SignatureVisitor {

    private MethodDescriptor methodDescriptor;
    private VisitorHelper visitorHelper;
    private int parameterIndex = 0;

    MethodSignatureVisitor(MethodDescriptor methodDescriptor, VisitorHelper visitorHelper) {
        super(Opcodes.ASM4);
        this.methodDescriptor = methodDescriptor;
        this.visitorHelper = visitorHelper;
    }

    @Override
    public void visitFormalTypeParameter(String name) {
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return new DependentTypeSignatureVisitor(methodDescriptor, visitorHelper);
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return new DependentTypeSignatureVisitor(methodDescriptor, visitorHelper);
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitInterface() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitParameterType() {
        ParameterDescriptor parameterDescriptor = visitorHelper.addParameterDescriptor(methodDescriptor, parameterIndex);
        parameterIndex++;
        return new DependentTypeSignatureVisitor(parameterDescriptor, visitorHelper);
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return new DependentTypeSignatureVisitor(methodDescriptor, visitorHelper);
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        return new DependentTypeSignatureVisitor(methodDescriptor, visitorHelper);
    }

    @Override
    public void visitBaseType(char descriptor) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public void visitTypeVariable(String name) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitArrayType() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public void visitClassType(String name) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public void visitInnerClassType(String name) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public void visitTypeArgument() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public void visitEnd() {
    }
}
