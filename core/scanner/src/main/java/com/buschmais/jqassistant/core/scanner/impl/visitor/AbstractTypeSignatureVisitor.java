package com.buschmais.jqassistant.core.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.scanner.impl.resolver.DescriptorResolverFactory;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Abstract implementation of a type signature visitor.
 */
public abstract class AbstractTypeSignatureVisitor<T extends Descriptor> extends AbstractVisitor implements SignatureVisitor {

    /**
     * The resolved type descriptor.
     */
    private TypeDescriptor resolvedTypeDescriptor;

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
        resolvedTypeDescriptor = getTypeDescriptor(name);
    }

    @Override
    public void visitInnerClassType(String name) {
        String innerClassName = resolvedTypeDescriptor.getFullQualifiedName() + "$" + name;
        resolvedTypeDescriptor = getTypeDescriptor(innerClassName);
    }

    @Override
    public void visitTypeArgument() {
    }

    @Override
    public void visitEnd() {
        visitEnd(resolvedTypeDescriptor);
    }

    public abstract void visitEnd(TypeDescriptor typeDescriptor);
}
