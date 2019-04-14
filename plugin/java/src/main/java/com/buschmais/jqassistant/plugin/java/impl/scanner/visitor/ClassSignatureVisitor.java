package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

public class ClassSignatureVisitor extends SignatureVisitor {

    private TypeCache.CachedType<? extends ClassFileDescriptor> cachedType;

    private VisitorHelper visitorHelper;

    private DependentTypeSignatureVisitor dependentTypeSignatureVisitor;

    protected ClassSignatureVisitor(TypeCache.CachedType<? extends ClassFileDescriptor> cachedType, VisitorHelper visitorHelper, DependentTypeSignatureVisitor dependentTypeSignatureVisitor) {
        super(Opcodes.ASM7);
        this.cachedType = cachedType;
        this.visitorHelper = visitorHelper;
        this.dependentTypeSignatureVisitor = dependentTypeSignatureVisitor;
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return dependentTypeSignatureVisitor;
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return dependentTypeSignatureVisitor;
    }

    @Override
    public SignatureVisitor visitSuperclass() {
        return new AbstractTypeSignatureVisitor(cachedType, visitorHelper) {
            @Override
            public SignatureVisitor visitArrayType() {
                return dependentTypeSignatureVisitor;
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return dependentTypeSignatureVisitor;
            }

            @Override
            public void visitEnd(TypeDescriptor resolvedTypeDescriptor) {
                cachedType.getTypeDescriptor().setSuperClass(resolvedTypeDescriptor);
            }

        };
    }

    @Override
    public SignatureVisitor visitInterface() {
        return new AbstractTypeSignatureVisitor(cachedType, visitorHelper) {

            @Override
            public SignatureVisitor visitArrayType() {
                return dependentTypeSignatureVisitor;
            }

            @Override
            public SignatureVisitor visitTypeArgument(char wildcard) {
                return dependentTypeSignatureVisitor;
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
