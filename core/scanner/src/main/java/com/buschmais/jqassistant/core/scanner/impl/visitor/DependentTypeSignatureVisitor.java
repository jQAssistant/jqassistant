package com.buschmais.jqassistant.core.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.DependentDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.scanner.impl.resolver.DescriptorResolverFactory;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Type signature visitor which adds a dependency to a resolved type.
 */
public class DependentTypeSignatureVisitor extends AbstractTypeSignatureVisitor<DependentDescriptor> {

    /**
     * Constructor
     *
     * @param dependentDescriptor The descriptor which depends on the resolvedd type.
     * @param visitorHelper     The {@link VisitorHelper}
     */
    DependentTypeSignatureVisitor(DependentDescriptor dependentDescriptor, VisitorHelper visitorHelper) {
        super(dependentDescriptor, visitorHelper);
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
        getUsingDescriptor().getDependencies().add(resolvedTypeDescriptor);
    }
}
