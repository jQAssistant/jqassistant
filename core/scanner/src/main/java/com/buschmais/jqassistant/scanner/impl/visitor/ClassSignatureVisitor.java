package com.buschmais.jqassistant.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.ClassDescriptor;
import com.buschmais.jqassistant.scanner.impl.resolver.DescriptorResolverFactory;
import org.objectweb.asm.signature.SignatureVisitor;

public class ClassSignatureVisitor extends AbstractVisitor implements SignatureVisitor {

    private ClassDescriptor classDescriptor;

    protected ClassSignatureVisitor(ClassDescriptor classDescriptor, DescriptorResolverFactory resolverFactory) {
        super(resolverFactory);
        this.classDescriptor = classDescriptor;
    }

    @Override
    public void visitFormalTypeParameter(String name) {
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return new DependentTypeSignatureVisitor(classDescriptor, getResolverFactory());
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return new DependentTypeSignatureVisitor(classDescriptor, getResolverFactory());
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return new AbstractTypeSignatureVisitor(classDescriptor, getResolverFactory()) {
            @Override
            public SignatureVisitor visitArrayType() {
                return new DependentTypeSignatureVisitor(classDescriptor, getResolverFactory());
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return new DependentTypeSignatureVisitor(classDescriptor, getResolverFactory());
            }

            @Override
            public void visitEnd(ClassDescriptor resolvedClassDescriptor) {
                classDescriptor.setSuperClass(resolvedClassDescriptor);
            }

        };
    }

    @Override
    public SignatureVisitor visitInterface() {
        return new AbstractTypeSignatureVisitor(classDescriptor, getResolverFactory()) {

            @Override
            public SignatureVisitor visitArrayType() {
                return new DependentTypeSignatureVisitor(classDescriptor, getResolverFactory());
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return new DependentTypeSignatureVisitor(classDescriptor, getResolverFactory());
            }

            @Override
            public void visitEnd(ClassDescriptor resolvedClassDescriptor) {
                classDescriptor.getInterfaces().add(resolvedClassDescriptor);
            }
        };
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
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public void visitTypeVariable(String name) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitArrayType() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public void visitClassType(String name) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public void visitInnerClassType(String name) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public void visitTypeArgument() {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }

    @Override
    public void visitEnd() {
    }
}
