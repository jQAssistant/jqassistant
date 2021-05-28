package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.GenericDeclarationDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.TypeVariableDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

import org.objectweb.asm.signature.SignatureVisitor;

public class AbstractGenericDeclarationVisitor<T extends Descriptor> extends SignatureVisitor {

    protected final VisitorHelper visitorHelper;

    protected final T descriptor;

    protected final TypeCache.CachedType containingType;

    private int currentTypeParameterIndex = 0;

    private TypeVariableDescriptor currentTypeParameter;

    protected AbstractGenericDeclarationVisitor(VisitorHelper visitorHelper, T descriptor, TypeCache.CachedType containingType) {
        super(VisitorHelper.ASM_OPCODES);
        this.visitorHelper = visitorHelper;
        this.descriptor = descriptor;
        this.containingType = containingType;
    }

    @Override
    public final void visitFormalTypeParameter(String name) {
        GenericDeclarationDescriptor genericDeclaration = visitorHelper.getStore().addDescriptorType(descriptor, GenericDeclarationDescriptor.class);
        this.currentTypeParameter = genericDeclaration.resolveTypeParameter(currentTypeParameterIndex);
        this.currentTypeParameter.setName(name);
        this.currentTypeParameterIndex++;
    }

    @Override
    public final SignatureVisitor visitClassBound() {
        return new AbstractBoundVisitor(visitorHelper, containingType) {
            @Override
            protected void apply(BoundDescriptor bound) {
                currentTypeParameter.getBounds().add(bound);
            }
        };
    }

    @Override
    public final SignatureVisitor visitInterfaceBound() {
        return new AbstractBoundVisitor(visitorHelper, containingType) {
            @Override
            protected void apply(BoundDescriptor bound) {
                currentTypeParameter.getBounds().add(bound);
            }
        };
    }

}
