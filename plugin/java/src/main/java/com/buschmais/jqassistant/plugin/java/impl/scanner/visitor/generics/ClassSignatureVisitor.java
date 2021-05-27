package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.GenericDeclarationDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.TypeVariableDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.AbstractTypeSignatureVisitor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.DependentTypeSignatureVisitor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

import org.objectweb.asm.signature.SignatureVisitor;

public class ClassSignatureVisitor extends SignatureVisitor {

    private TypeCache.CachedType<? extends ClassFileDescriptor> cachedType;

    private VisitorHelper visitorHelper;

    private DependentTypeSignatureVisitor dependentTypeSignatureVisitor;

    private int currentTypeParameterIndex = 0;

    private TypeVariableDescriptor currentTypeParameter;

    public ClassSignatureVisitor(TypeCache.CachedType<? extends ClassFileDescriptor> cachedType, VisitorHelper visitorHelper,
            DependentTypeSignatureVisitor dependentTypeSignatureVisitor) {
        super(VisitorHelper.ASM_OPCODES);
        this.cachedType = cachedType;
        this.visitorHelper = visitorHelper;
        this.dependentTypeSignatureVisitor = dependentTypeSignatureVisitor;
    }

    @Override
    public void visitFormalTypeParameter(String name) {
        GenericDeclarationDescriptor genericDeclaration = visitorHelper.getStore().addDescriptorType(cachedType.getTypeDescriptor(),
                GenericDeclarationDescriptor.class);
        this.currentTypeParameter = genericDeclaration.resolveTypeParameter(currentTypeParameterIndex);
        this.currentTypeParameter.setName(name);
        this.currentTypeParameterIndex++;
    }

    @Override
    public SignatureVisitor visitClassBound() {
        return new TypeVariableVisitor(currentTypeParameter, visitorHelper, cachedType);
    }

    @Override
    public SignatureVisitor visitInterfaceBound() {
        return new TypeVariableVisitor(currentTypeParameter, visitorHelper, cachedType);
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
