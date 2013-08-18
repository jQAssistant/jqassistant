package com.buschmais.jqassistant.core.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.scanner.impl.resolver.DescriptorResolverFactory;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Abstract implementation of a types signature visitor.
 */
public abstract class AbstractTypeSignatureVisitor<T extends Descriptor> extends SignatureVisitor {

    /**
     * The resolved types descriptor.
     */
    private TypeDescriptor resolvedTypeDescriptor;

    /**
     * The descriptor using the resolved types descriptor.
     */
    private T usingDescriptor;

    private VisitorHelper visitorHelper;

    /**
     * Constructor.
     *
     * @param usingDescriptor The descriptor using the resolved types descriptor.
     * @param visitorHelper The {@link VisitorHelper}.
     */
    protected AbstractTypeSignatureVisitor(T usingDescriptor, VisitorHelper visitorHelper) {
        super(Opcodes.ASM4);
        this.usingDescriptor = usingDescriptor;
        this.visitorHelper = visitorHelper;
    }

    protected T getUsingDescriptor() {
        return usingDescriptor;
    }

    protected VisitorHelper getVisitorHelper() {
        return visitorHelper;
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
        resolvedTypeDescriptor = visitorHelper.getTypeDescriptor(name);
    }

    @Override
    public void visitInnerClassType(String name) {
        String innerClassName = resolvedTypeDescriptor.getFullQualifiedName() + "$" + name;
        resolvedTypeDescriptor = visitorHelper.getTypeDescriptor(innerClassName);
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
