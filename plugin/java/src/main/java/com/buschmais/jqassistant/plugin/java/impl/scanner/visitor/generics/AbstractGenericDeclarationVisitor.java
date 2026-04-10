package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.GenericDeclarationDeclaresTypeParameter;
import com.buschmais.jqassistant.plugin.java.api.model.generics.GenericDeclarationDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.TypeVariableDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.ClassFileVisitorContext;

import org.objectweb.asm.signature.SignatureVisitor;

public class AbstractGenericDeclarationVisitor<T extends Descriptor> extends SignatureVisitor {

    protected final ClassFileVisitorContext classFileVisitorContext;

    protected final T descriptor;

    protected final TypeClassFileDescriptor containingType;

    protected GenericDeclarationDescriptor genericDeclaration;

    private int currentTypeParameterIndex = 0;

    private TypeVariableDescriptor currentTypeParameter;

    protected AbstractGenericDeclarationVisitor(ClassFileVisitorContext classFileVisitorContext, T descriptor, TypeClassFileDescriptor containingType) {
        super(ClassFileVisitorContext.ASM_OPCODES);
        this.classFileVisitorContext = classFileVisitorContext;
        this.descriptor = descriptor;
        this.containingType = containingType;
    }

    @Override
    public final void visitFormalTypeParameter(String name) {
        if (this.genericDeclaration == null) {
            this.genericDeclaration = classFileVisitorContext.getStore()
                .addDescriptorType(descriptor, GenericDeclarationDescriptor.class);
        }
        this.currentTypeParameter = resolveDeclaredTypeParameter();
        this.currentTypeParameter.setName(name);
        this.currentTypeParameterIndex++;
        classFileVisitorContext.getTypeVariableResolver()
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
        TypeVariableDescriptor typeParameter = classFileVisitorContext.getStore()
            .create(TypeVariableDescriptor.class);
        GenericDeclarationDeclaresTypeParameter declaration = classFileVisitorContext.getStore()
            .create(this.genericDeclaration, GenericDeclarationDeclaresTypeParameter.class, typeParameter);
        declaration.setIndex(this.currentTypeParameterIndex);
        return typeParameter;
    }

    @Override
    public final SignatureVisitor visitClassBound() {
        return new AbstractBoundVisitor(classFileVisitorContext, containingType) {
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
        return new AbstractBoundVisitor(classFileVisitorContext, containingType) {
            @Override
            protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                currentTypeParameter.setRawType(rawTypeBound);
                currentTypeParameter.getUpperBounds()
                    .add(bound);
            }
        };
    }
}
