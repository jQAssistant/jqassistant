package com.buschmais.jqassistant.core.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.scanner.impl.resolver.DescriptorResolverFactory;
import org.objectweb.asm.signature.SignatureVisitor;

public class ClassSignatureVisitor extends AbstractVisitor implements SignatureVisitor {

    private TypeDescriptor typeDescriptor;

    protected ClassSignatureVisitor(TypeDescriptor typeDescriptor, DescriptorResolverFactory resolverFactory) {
        super(resolverFactory);
        this.typeDescriptor = typeDescriptor;
    }

    @Override
    public void visitFormalTypeParameter(String name) {
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return new DependentTypeSignatureVisitor(typeDescriptor, getResolverFactory());
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return new DependentTypeSignatureVisitor(typeDescriptor, getResolverFactory());
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return new AbstractTypeSignatureVisitor(typeDescriptor, getResolverFactory()) {
            @Override
            public SignatureVisitor visitArrayType() {
                return new DependentTypeSignatureVisitor(typeDescriptor, getResolverFactory());
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return new DependentTypeSignatureVisitor(typeDescriptor, getResolverFactory());
            }

            @Override
            public void visitEnd(TypeDescriptor resolvedTypeDescriptor) {
                typeDescriptor.setSuperClass(resolvedTypeDescriptor);
            }

        };
    }

    @Override
    public SignatureVisitor visitInterface() {
        return new AbstractTypeSignatureVisitor(typeDescriptor, getResolverFactory()) {

            @Override
            public SignatureVisitor visitArrayType() {
                return new DependentTypeSignatureVisitor(typeDescriptor, getResolverFactory());
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return new DependentTypeSignatureVisitor(typeDescriptor, getResolverFactory());
            }

            @Override
            public void visitEnd(TypeDescriptor resolvedTypeDescriptor) {
                typeDescriptor.getInterfaces().add(resolvedTypeDescriptor);
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
