package com.buschmais.jqassistant.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.ClassDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.DependentDescriptor;
import com.buschmais.jqassistant.scanner.impl.resolver.DescriptorResolverFactory;
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

            private ClassDescriptor classDescriptor;

            @Override
            public void visitClassType(String name) {
                classDescriptor = getClassDescriptor(name);
                dependentDescriptor.getDependencies().add(classDescriptor);
            }

            @Override
            public void visitInnerClassType(String name) {
                String innerClassName = classDescriptor.getFullQualifiedName() + "$" + name;
                dependentDescriptor.getDependencies().add(getClassDescriptor(innerClassName));
            }

        };
    }

}
