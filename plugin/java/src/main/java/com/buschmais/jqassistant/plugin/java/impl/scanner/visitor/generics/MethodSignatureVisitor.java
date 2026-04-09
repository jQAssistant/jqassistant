package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ParameterDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.ClassFileVisitorContext;

import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Visitor for method signatures.
 */
public class MethodSignatureVisitor extends AbstractGenericDeclarationVisitor<MethodDescriptor> {

    private int parameterIndex = 0;

    public MethodSignatureVisitor(TypeClassFileDescriptor containingType, MethodDescriptor methodDescriptor, ClassFileVisitorContext classFileVisitorContext) {
        super(classFileVisitorContext, methodDescriptor, containingType);
    }

    @Override
    public SignatureVisitor visitParameterType() {
        final ParameterDescriptor parameterDescriptor = classFileVisitorContext.addParameterDescriptor(descriptor, parameterIndex);
        parameterIndex++;
        return new AbstractBoundVisitor(classFileVisitorContext, containingType) {
            @Override
            protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                parameterDescriptor.setType(rawTypeBound);
                parameterDescriptor.setGenericType(bound);
            }
        };
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return new AbstractBoundVisitor(classFileVisitorContext, containingType) {
            @Override
            protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                descriptor.setReturns(rawTypeBound);
                descriptor.setReturnsGeneric(bound);
            }
        };
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        return new AbstractBoundVisitor(classFileVisitorContext, containingType) {
            @Override
            protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                // raw type not added here, already done by class visitor
                descriptor.getThrowsGeneric().add(bound);
            }
        };
    }
}
