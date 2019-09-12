package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ParameterDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;

import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Visitor for method signatures.
 */
public class MethodSignatureVisitor extends SignatureVisitor {

    private TypeCache.CachedType containingType;
    private MethodDescriptor methodDescriptor;
    private VisitorHelper visitorHelper;
    private DependentTypeSignatureVisitor dependentTypeSignatureVisitor;
    private int parameterIndex = 0;

    MethodSignatureVisitor(TypeCache.CachedType containingType, MethodDescriptor methodDescriptor, VisitorHelper visitorHelper,
                           DependentTypeSignatureVisitor dependentTypeSignatureVisitor) {
        super(VisitorHelper.OPCODE);
        this.containingType = containingType;
        this.methodDescriptor = methodDescriptor;
        this.visitorHelper = visitorHelper;
        this.dependentTypeSignatureVisitor = dependentTypeSignatureVisitor;
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return dependentTypeSignatureVisitor;
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return dependentTypeSignatureVisitor;
    }

    @Override
    public SignatureVisitor visitParameterType() {
        final ParameterDescriptor parameterDescriptor = visitorHelper.addParameterDescriptor(methodDescriptor, parameterIndex);
        parameterIndex++;
        return new AbstractTypeSignatureVisitor(containingType, visitorHelper) {

            @Override
            public SignatureVisitor visitArrayType() {
                return dependentTypeSignatureVisitor;
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return dependentTypeSignatureVisitor;
            }

            @Override
            public void visitEnd(TypeDescriptor resolvedTypeDescriptor) {
                parameterDescriptor.setType(resolvedTypeDescriptor);
            }
        };
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return new AbstractTypeSignatureVisitor(containingType, visitorHelper) {

            @Override
            public SignatureVisitor visitArrayType() {
                return dependentTypeSignatureVisitor;
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return dependentTypeSignatureVisitor;
            }

            @Override
            public void visitEnd(TypeDescriptor resolvedTypeDescriptor) {
                methodDescriptor.setReturns(resolvedTypeDescriptor);
            }
        };
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        return dependentTypeSignatureVisitor;
    }
}
