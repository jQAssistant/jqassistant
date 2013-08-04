package com.buschmais.jqassistant.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.ClassDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.DependentDescriptor;
import com.buschmais.jqassistant.scanner.impl.resolver.DescriptorResolverFactory;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Type signature visitor which adds a dependency to a resolved type.
 */
public class DependentTypeSignatureVisitor extends AbstractTypeSignatureVisitor<DependentDescriptor> {

    /**
     * Constructor
     *
     * @param dependentDescriptor The descriptor which depends on the resolvedd type.
     * @param resolverFactory     The {@link DescriptorResolverFactory}
     */
    DependentTypeSignatureVisitor(DependentDescriptor dependentDescriptor, DescriptorResolverFactory resolverFactory) {
        super(dependentDescriptor, resolverFactory);
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
    public void visitEnd(ClassDescriptor resolvedClassDescriptor) {
        getUsingDescriptor().getDependencies().add(resolvedClassDescriptor);
    }
}
