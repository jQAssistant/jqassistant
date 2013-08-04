package com.buschmais.jqassistant.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.ClassDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.scanner.impl.resolver.DescriptorResolverFactory;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Abstract implementation of a type signature visitor.
 */
public abstract class AbstractTypeSignatureVisitor<T extends Descriptor> extends AbstractVisitor implements SignatureVisitor {

    /**
     * The resolved type descriptor.
     */
    private ClassDescriptor resolvedClassDescriptor;

    /**
     * The descriptor using the resolved type descriptor.
     */
    private T usingDescriptor;

    /**
     * Constructor.
     *
     * @param usingDescriptor The descriptor using the resolved type descriptor.
     * @param resolverFactory The {@link DescriptorResolverFactory}.
     */
    protected AbstractTypeSignatureVisitor(T usingDescriptor, DescriptorResolverFactory resolverFactory) {
        super(resolverFactory);
        this.usingDescriptor = usingDescriptor;
    }

    /**
     * Returns the using descriptor.
     * @return The using descriptor.
     */
    protected T getUsingDescriptor() {
        return usingDescriptor;
    }

    @Override
    public void visitFormalTypeParameter(String name) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitClassBound() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitInterface() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitParameterType() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitReturnType() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public void visitBaseType(char descriptor) {
    }

    @Override
    public void visitTypeVariable(String name) {
    }

    @Override
    public void visitClassType(String name) {
        resolvedClassDescriptor = getClassDescriptor(name);
    }

    @Override
    public void visitInnerClassType(String name) {
        String innerClassName = resolvedClassDescriptor.getFullQualifiedName() + "$" + name;
        resolvedClassDescriptor = getClassDescriptor(innerClassName);
    }

    @Override
    public void visitTypeArgument() {
    }

    @Override
    public void visitEnd() {
        visitEnd(resolvedClassDescriptor);
    }

    public abstract void visitEnd(ClassDescriptor classDescriptor);
}
