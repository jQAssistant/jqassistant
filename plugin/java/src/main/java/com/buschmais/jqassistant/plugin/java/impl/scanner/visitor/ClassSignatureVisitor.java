package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;

public class ClassSignatureVisitor extends SignatureVisitor {

    private TypeCache.CachedType cachedType;

    private VisitorHelper visitorHelper;

    protected ClassSignatureVisitor(TypeCache.CachedType cachedType, VisitorHelper visitorHelper) {
        super(Opcodes.ASM5);
        this.cachedType = cachedType;
        this.visitorHelper = visitorHelper;
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return new DependentTypeSignatureVisitor(cachedType, visitorHelper);
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return new DependentTypeSignatureVisitor(cachedType, visitorHelper);
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return new AbstractTypeSignatureVisitor<TypeDescriptor>(cachedType, visitorHelper) {
            @Override
            public SignatureVisitor visitArrayType() {
                return new DependentTypeSignatureVisitor(cachedType, visitorHelper);
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return new DependentTypeSignatureVisitor(cachedType, visitorHelper);
            }

            @Override
            public void visitEnd(TypeDescriptor resolvedTypeDescriptor) {
                cachedType.getTypeDescriptor().setSuperClass(resolvedTypeDescriptor);
            }

        };
    }

    @Override
    public SignatureVisitor visitInterface() {
        return new AbstractTypeSignatureVisitor<TypeDescriptor>(cachedType, visitorHelper) {

            @Override
            public SignatureVisitor visitArrayType() {
                return new DependentTypeSignatureVisitor(cachedType, visitorHelper);
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return new DependentTypeSignatureVisitor(cachedType, visitorHelper);
            }

            @Override
            public void visitEnd(TypeDescriptor resolvedTypeDescriptor) {
                cachedType.getTypeDescriptor().getInterfaces().add(resolvedTypeDescriptor);
            }
        };
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        throw new UnsupportedOperationException("Method is not implemented.");
    }
}
