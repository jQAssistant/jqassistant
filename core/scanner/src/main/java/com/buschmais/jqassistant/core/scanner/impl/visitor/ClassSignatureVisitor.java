package com.buschmais.jqassistant.core.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.scanner.impl.resolver.DescriptorResolverFactory;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

public class ClassSignatureVisitor extends SignatureVisitor {

    private TypeDescriptor typeDescriptor;

    private VisitorHelper visitorHelper;

    protected ClassSignatureVisitor(TypeDescriptor typeDescriptor, VisitorHelper visitorHelper) {
        super(Opcodes.ASM4);
        this.typeDescriptor = typeDescriptor;
        this.visitorHelper = visitorHelper;
    }

    @Override
    public void visitFormalTypeParameter(String name) {
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return new DependentTypeSignatureVisitor(typeDescriptor, visitorHelper);
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return new DependentTypeSignatureVisitor(typeDescriptor, visitorHelper);
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return new AbstractTypeSignatureVisitor(typeDescriptor, visitorHelper) {
            @Override
            public SignatureVisitor visitArrayType() {
                return new DependentTypeSignatureVisitor(typeDescriptor, visitorHelper);
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return new DependentTypeSignatureVisitor(typeDescriptor, visitorHelper);
            }

            @Override
            public void visitEnd(TypeDescriptor resolvedTypeDescriptor) {
                typeDescriptor.setSuperClass(resolvedTypeDescriptor);
            }

        };
    }

    @Override
    public SignatureVisitor visitInterface() {
        return new AbstractTypeSignatureVisitor(typeDescriptor, visitorHelper) {

            @Override
            public SignatureVisitor visitArrayType() {
                return new DependentTypeSignatureVisitor(typeDescriptor, visitorHelper);
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return new DependentTypeSignatureVisitor(typeDescriptor, visitorHelper);
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
