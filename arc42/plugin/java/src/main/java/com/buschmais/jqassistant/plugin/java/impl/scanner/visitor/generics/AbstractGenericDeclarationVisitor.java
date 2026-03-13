package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.GenericDeclarationDeclaresTypeParameter;
import com.buschmais.jqassistant.plugin.java.api.model.generics.GenericDeclarationDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.TypeVariableDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

import org.objectweb.asm.signature.SignatureVisitor;

public class AbstractGenericDeclarationVisitor<T extends Descriptor> extends SignatureVisitor {

    protected final VisitorHelper visitorHelper;

    protected final T descriptor;

    protected final TypeCache.CachedType containingType;

    protected GenericDeclarationDescriptor genericDeclaration;

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
        if (this.genericDeclaration == null) {
            this.genericDeclaration = visitorHelper.getStore()
                .addDescriptorType(descriptor, GenericDeclarationDescriptor.class);
        }
        this.currentTypeParameter = resolveDeclaredTypeParameter();
        this.currentTypeParameter.setName(name);
        this.currentTypeParameterIndex++;
        visitorHelper.getTypeVariableResolver()
            .declare(this.currentTypeParameter);
    }

    /**
     * Resolves the declared type parameter (i.e. {@link TypeVariableDescriptor}) for the current index.
     *
     * @return The {@link TypeVariableDescriptor}.
     */
    private TypeVariableDescriptor resolveDeclaredTypeParameter() {
        // Find an existing type parameter declaration (i.e. which is already referenced.
        for (GenericDeclarationDeclaresTypeParameter typeParameter : this.genericDeclaration.getDeclaredTypeParameters()) {
            if (typeParameter.getIndex() == this.currentTypeParameterIndex) {
                return typeParameter.getTypeParameter();
            }
        }
        // Create a new type parameter declaration.
        TypeVariableDescriptor typeParameter = visitorHelper.getStore()
            .create(TypeVariableDescriptor.class);
        GenericDeclarationDeclaresTypeParameter declaration = visitorHelper.getStore()
            .create(this.genericDeclaration, GenericDeclarationDeclaresTypeParameter.class, typeParameter);
        declaration.setIndex(this.currentTypeParameterIndex);
        return typeParameter;
    }

    @Override
    public final SignatureVisitor visitClassBound() {
        return new AbstractBoundVisitor(visitorHelper, containingType) {
            @Override
            protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                currentTypeParameter.setRawType(rawTypeBound);
                currentTypeParameter.getUpperBounds()
                    .add(bound);
            }
        };
    }

    @Override
    public final SignatureVisitor visitInterfaceBound() {
        return new AbstractBoundVisitor(visitorHelper, containingType) {
            @Override
            protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                currentTypeParameter.setRawType(rawTypeBound);
                currentTypeParameter.getUpperBounds()
                    .add(bound);
            }
        };
    }
}
