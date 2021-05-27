package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.GenericDeclarationDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.TypeVariableDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

import org.objectweb.asm.signature.SignatureVisitor;

public class ClassSignatureVisitor extends SignatureVisitor {

    private TypeCache.CachedType<? extends ClassFileDescriptor> cachedType;

    private VisitorHelper visitorHelper;

    private int currentTypeParameterIndex = 0;

    private TypeVariableDescriptor currentTypeParameter;

    public ClassSignatureVisitor(TypeCache.CachedType<? extends ClassFileDescriptor> cachedType, VisitorHelper visitorHelper) {
        super(VisitorHelper.ASM_OPCODES);
        this.cachedType = cachedType;
        this.visitorHelper = visitorHelper;
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
        return new AbstractBoundVisitor<BoundDescriptor>(null, visitorHelper, cachedType) {
            @Override
            protected void apply(BoundDescriptor bound) {
                ClassFileDescriptor typeDescriptor = cachedType.getTypeDescriptor();
                typeDescriptor.setSuperClass(bound.getRawType());
                typeDescriptor.setGenericSuperClass(bound);
            }
        };
    }

    @Override
    public SignatureVisitor visitInterface() {
        return new AbstractBoundVisitor<BoundDescriptor>(null, visitorHelper, cachedType) {
            @Override
            protected void apply(BoundDescriptor bound) {
                ClassFileDescriptor typeDescriptor = cachedType.getTypeDescriptor();
                typeDescriptor.getInterfaces().add(bound.getRawType());
                typeDescriptor.getGenericInterfaces().add(bound);
            }
        };
    }
}
