package com.buschmais.jqassistant.scanner.visitor;

import com.buschmais.jqassistant.scanner.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.store.api.model.DependentDescriptor;
import org.objectweb.asm.signature.SignatureVisitor;

public class DependentSignatureVisitor<T extends DependentDescriptor> extends AbstractVisitor implements SignatureVisitor {

    private final T dependentDescriptor;

    public DependentSignatureVisitor(T dependentDescriptor, DescriptorResolverFactory resolverFactory) {
        super(resolverFactory);
        this.dependentDescriptor = dependentDescriptor;
    }

    public T getDependentDescriptor() {
        return dependentDescriptor;
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return getTypeVisitor();
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return getTypeVisitor();
    }

    @Override
    public SignatureVisitor visitParameterType() {
        return getTypeVisitor();
    }

    @Override
    public SignatureVisitor visitReturnType() {
        return getTypeVisitor();
    }

    @Override
    public SignatureVisitor visitExceptionType() {
        return getTypeVisitor();
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        return getTypeVisitor();
    }

    @Override
    public SignatureVisitor visitArrayType() {
        return getTypeVisitor();
    }

    @Override
    public void visitFormalTypeParameter(String name) {
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return getTypeVisitor();
    }

    @Override
    public SignatureVisitor visitInterface() {
        return getTypeVisitor();
    }

    @Override
    public void visitBaseType(char descriptor) {
    }

    @Override
    public void visitTypeVariable(String name) {
    }

    @Override
    public void visitClassType(String name) {
    }

    @Override
    public void visitInnerClassType(String name) {
    }

    @Override
    public void visitTypeArgument() {
    }

    @Override
    public void visitEnd() {
    }

    private SignatureVisitor getTypeVisitor() {
        return new DependentSignatureVisitor<T>(dependentDescriptor, getResolverFactory()) {

            @Override
            public void visitClassType(String name) {
                dependentDescriptor.getDependencies().add(getClassDescriptor(name));
            }

            @Override
            public void visitInnerClassType(String name) {
                dependentDescriptor.getDependencies().add(getClassDescriptor(name));
            }

        };
    }

}
