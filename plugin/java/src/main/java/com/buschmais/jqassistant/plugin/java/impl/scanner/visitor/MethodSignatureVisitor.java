package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ParameterDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

/**
 * Visitor for method signatures.
 */
public class MethodSignatureVisitor extends SignatureVisitor {

    private TypeCache.CachedType containingType;
    private MethodDescriptor methodDescriptor;
    private VisitorHelper visitorHelper;
    private int parameterIndex = 0;

    MethodSignatureVisitor(TypeCache.CachedType containingType, MethodDescriptor methodDescriptor, VisitorHelper visitorHelper) {
        super(Opcodes.ASM5);
        this.containingType = containingType;
        this.methodDescriptor = methodDescriptor;
        this.visitorHelper = visitorHelper;
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return new DependentTypeSignatureVisitor(containingType, visitorHelper);
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return new DependentTypeSignatureVisitor(containingType, visitorHelper);
    }

    @Override
    public SignatureVisitor visitParameterType() {
        final ParameterDescriptor parameterDescriptor = visitorHelper.getParameterDescriptor(methodDescriptor, parameterIndex);
        parameterIndex++;
        return new AbstractTypeSignatureVisitor<ParameterDescriptor>(containingType, visitorHelper) {

            @Override
            public SignatureVisitor visitArrayType() {
                return new DependentTypeSignatureVisitor(containingType, visitorHelper);
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return new DependentTypeSignatureVisitor(containingType, visitorHelper);
            }

            @Override
            public void visitEnd(TypeDescriptor resolvedTypeDescriptor) {
                parameterDescriptor.setType(resolvedTypeDescriptor);
            }
        };
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return new AbstractTypeSignatureVisitor<MethodDescriptor>(containingType, visitorHelper) {

            @Override
            public SignatureVisitor visitArrayType() {
                return new DependentTypeSignatureVisitor(containingType, visitorHelper);
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return new DependentTypeSignatureVisitor(containingType, visitorHelper);
            }

            @Override
            public void visitEnd(TypeDescriptor resolvedTypeDescriptor) {
                methodDescriptor.setReturns(resolvedTypeDescriptor);
            }
        };
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        return new DependentTypeSignatureVisitor(containingType, visitorHelper);
    }
}
