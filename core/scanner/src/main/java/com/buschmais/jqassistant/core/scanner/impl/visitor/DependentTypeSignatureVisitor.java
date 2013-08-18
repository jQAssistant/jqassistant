package com.buschmais.jqassistant.core.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.DependentDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Type signature visitor which adds a dependency to a resolved types.
 */
public class DependentTypeSignatureVisitor extends AbstractTypeSignatureVisitor<DependentDescriptor> {

    /**
     * Constructor
     *
     * @param dependentDescriptor The descriptor which depends on the resolvedd types.
     * @param visitorHelper       The {@link VisitorHelper}
     */
    DependentTypeSignatureVisitor(DependentDescriptor dependentDescriptor, VisitorHelper visitorHelper) {
        super(dependentDescriptor, visitorHelper);
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return new DependentTypeSignatureVisitor(getUsingDescriptor(), getVisitorHelper());
    }

    @Override
    public SignatureVisitor visitInterface() {
        return new DependentTypeSignatureVisitor(getUsingDescriptor(), getVisitorHelper());
    }

    @Override
    public SignatureVisitor visitArrayType() {
        return new DependentTypeSignatureVisitor(getUsingDescriptor(), getVisitorHelper());
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        return new DependentTypeSignatureVisitor(getUsingDescriptor(), getVisitorHelper());
    }

    @Override
    public void visitEnd(TypeDescriptor resolvedTypeDescriptor) {
        getUsingDescriptor().getDependencies().add(resolvedTypeDescriptor);
    }
}
