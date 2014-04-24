package com.buschmais.jqassistant.plugin.java.impl.store.visitor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ParameterDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;

/**
 * Visitor for method signatures.
 */
public class MethodSignatureVisitor extends SignatureVisitor {

    private MethodDescriptor methodDescriptor;
    private VisitorHelper visitorHelper;
    private int parameterIndex = 0;

    MethodSignatureVisitor(MethodDescriptor methodDescriptor, VisitorHelper visitorHelper) {
        super(Opcodes.ASM5);
        this.methodDescriptor = methodDescriptor;
        this.visitorHelper = visitorHelper;
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
    public SignatureVisitor visitParameterType() {
        final ParameterDescriptor parameterDescriptor = visitorHelper.addParameterDescriptor(methodDescriptor, parameterIndex);
        parameterIndex++;
        return new AbstractTypeSignatureVisitor<ParameterDescriptor>(parameterDescriptor, visitorHelper) {

            @Override
            public SignatureVisitor visitArrayType() {
                return new DependentTypeSignatureVisitor(parameterDescriptor, visitorHelper);
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return new DependentTypeSignatureVisitor(parameterDescriptor, visitorHelper);
            }

            @Override
            public void visitEnd(TypeDescriptor resolvedTypeDescriptor) {
                parameterDescriptor.setType(resolvedTypeDescriptor);
            }
        };
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return new AbstractTypeSignatureVisitor<MethodDescriptor>(methodDescriptor, visitorHelper) {

            @Override
            public SignatureVisitor visitArrayType() {
                return new DependentTypeSignatureVisitor(methodDescriptor, visitorHelper);
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return new DependentTypeSignatureVisitor(methodDescriptor, visitorHelper);
            }

            @Override
            public void visitEnd(TypeDescriptor resolvedTypeDescriptor) {
                methodDescriptor.setReturns(resolvedTypeDescriptor);
            }
        };
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        return new DependentTypeSignatureVisitor(methodDescriptor, visitorHelper);
    }
}
