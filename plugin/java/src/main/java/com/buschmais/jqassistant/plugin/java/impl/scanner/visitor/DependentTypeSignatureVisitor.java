package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import org.objectweb.asm.signature.SignatureVisitor;

import com.buschmais.jqassistant.plugin.java.api.model.DependentDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

/**
 * Type signature visitor which adds a dependency to a resolved types.
 */
public class DependentTypeSignatureVisitor extends AbstractTypeSignatureVisitor<DependentDescriptor> {

    /**
     * Constructor
     * 
     * @param containingType
     *            The type containing the dependent element.
     * @param visitorHelper
     *            The {@link VisitorHelper}
     */
    DependentTypeSignatureVisitor(TypeCache.CachedType containingType, VisitorHelper visitorHelper) {
        super(containingType, visitorHelper);
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return new DependentTypeSignatureVisitor(getContainingType(), getVisitorHelper());
    }

    @Override
    public SignatureVisitor visitInterface() {
        return new DependentTypeSignatureVisitor(getContainingType(), getVisitorHelper());
    }

    @Override
    public SignatureVisitor visitArrayType() {
        return new DependentTypeSignatureVisitor(getContainingType(), getVisitorHelper());
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        return new DependentTypeSignatureVisitor(getContainingType(), getVisitorHelper());
    }

    @Override
    public void visitEnd(TypeDescriptor resolvedTypeDescriptor) {
    }
}
