package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

public class ClassSignatureVisitor extends SignatureVisitor {

    private TypeDescriptor typeDescriptor;

    private VisitorHelper visitorHelper;

    protected ClassSignatureVisitor(TypeDescriptor typeDescriptor, VisitorHelper visitorHelper) {
        super(Opcodes.ASM5);
        this.typeDescriptor = typeDescriptor;
        this.visitorHelper = visitorHelper;
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
        return new AbstractTypeSignatureVisitor<TypeDescriptor>(typeDescriptor, visitorHelper) {
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
        return new AbstractTypeSignatureVisitor<TypeDescriptor>(typeDescriptor, visitorHelper) {

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
    public SignatureVisitor visitTypeArgument(char wildcard) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }
}
