package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import org.objectweb.asm.signature.SignatureVisitor;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;

/**
 * Type signature visitor which adds a dependency to a resolved types.
 */
public class DependentTypeSignatureVisitor extends AbstractTypeSignatureVisitor<TypeDescriptor> {

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
        return this;
    }

    @Override
    public SignatureVisitor visitInterface() {
        return this;
    }

    @Override
    public SignatureVisitor visitArrayType() {
        return this;
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        return this;
    }

    @Override
    public void visitEnd(TypeDescriptor resolvedTypeDescriptor) {
    }
}
