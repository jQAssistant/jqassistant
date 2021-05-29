package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ParameterDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Visitor for method signatures.
 */
public class MethodSignatureVisitor extends AbstractGenericDeclarationVisitor<MethodDescriptor> {

    private int parameterIndex = 0;

    public MethodSignatureVisitor(TypeCache.CachedType containingType, MethodDescriptor methodDescriptor, VisitorHelper visitorHelper) {
        super(visitorHelper, methodDescriptor, containingType);
    }

    @Override
    public SignatureVisitor visitParameterType() {
        final ParameterDescriptor parameterDescriptor = visitorHelper.addParameterDescriptor(descriptor, parameterIndex);
        parameterIndex++;
        return new AbstractBoundVisitor(visitorHelper, containingType) {
            @Override
            protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                parameterDescriptor.setType(rawTypeBound);
                parameterDescriptor.setGenericType(bound);
            }
        };
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return new AbstractBoundVisitor(visitorHelper, containingType) {
            @Override
            protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                descriptor.setReturns(rawTypeBound);
                descriptor.setReturnsGeneric(bound);
            }
        };
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        return new AbstractBoundVisitor(visitorHelper, containingType) {
            @Override
            protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                descriptor.getThrows().add(rawTypeBound);
                descriptor.getThrowsGeneric().add(bound);
            }
        };
    }
}
