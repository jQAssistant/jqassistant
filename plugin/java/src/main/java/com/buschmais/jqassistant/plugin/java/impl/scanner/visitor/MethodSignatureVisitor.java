package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ParameterDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics.AbstractBoundVisitor;

import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Visitor for method signatures.
 */
public class MethodSignatureVisitor extends SignatureVisitor {

    private final TypeCache.CachedType containingType;
    private final MethodDescriptor methodDescriptor;
    private final VisitorHelper visitorHelper;
    private int parameterIndex = 0;

    MethodSignatureVisitor(TypeCache.CachedType containingType, MethodDescriptor methodDescriptor, VisitorHelper visitorHelper) {
        super(VisitorHelper.ASM_OPCODES);
        this.containingType = containingType;
        this.methodDescriptor = methodDescriptor;
        this.visitorHelper = visitorHelper;
    }

    @Override
    public SignatureVisitor visitParameterType() {
        final ParameterDescriptor parameterDescriptor = visitorHelper.addParameterDescriptor(methodDescriptor, parameterIndex);
        parameterIndex++;
        return new AbstractBoundVisitor<BoundDescriptor>(null, visitorHelper, containingType) {
            @Override
            protected void apply(BoundDescriptor bound) {
                parameterDescriptor.setType(bound.getRawType());
                parameterDescriptor.setGenericType(bound);
            }
        };
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return new AbstractBoundVisitor<BoundDescriptor>(null, visitorHelper, containingType) {
            @Override
            protected void apply(BoundDescriptor bound) {
                methodDescriptor.setReturns(bound.getRawType());
                methodDescriptor.setReturnsGeneric(bound);
            }
        };
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        return new AbstractBoundVisitor<BoundDescriptor>(null, visitorHelper, containingType) {
            @Override
            protected void apply(BoundDescriptor bound) {
                methodDescriptor.getThrows().add(bound.getRawType());
                methodDescriptor.getThrowsGeneric().add(bound);
            }
        };
    }
}
