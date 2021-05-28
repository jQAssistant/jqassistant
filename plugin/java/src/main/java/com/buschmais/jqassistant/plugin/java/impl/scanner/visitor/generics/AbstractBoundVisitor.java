package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.*;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Abstract signature visitor class to determine generic bounds.
 */
public abstract class AbstractBoundVisitor extends SignatureVisitor {

    protected final VisitorHelper visitorHelper;

    private final TypeCache.CachedType<? extends ClassFileDescriptor> containingType;

    private BoundDescriptor current;

    private int currentTypeParameterIndex = 0;

    public AbstractBoundVisitor(VisitorHelper visitorHelper, TypeCache.CachedType<? extends ClassFileDescriptor> containingType) {
        super(VisitorHelper.ASM_OPCODES);
        this.visitorHelper = visitorHelper;
        this.containingType = containingType;
    }

    // visitBaseType | visitTypeVariable | visitArrayType | ( visitClassType
    // visitTypeArgument* ( visitInnerClassType visitTypeArgument* )* visitEnd )

    @Override
    public final void visitBaseType(char descriptor) {
        // TODO check if this is the right way to determine the primitive type
        createBound(Type.getType(Character.toString(descriptor)).toString());
    }

    @Override
    public final void visitClassType(String name) {
        createBound(SignatureHelper.getObjectType(name));
    }

    @Override
    public final void visitInnerClassType(String name) {
        createBound(SignatureHelper.getObjectType(name));
    }

    private final void createBound(String rawType) {
        current = visitorHelper.getStore().create(BoundDescriptor.class);
        current.setRawType(visitorHelper.resolveType(rawType, containingType).getTypeDescriptor());
        apply(current);
    }

    @Override
    public final void visitTypeVariable(String name) {
        // TODO resolve type variables from declaring method, type or outer type
    }

    @Override
    public final SignatureVisitor visitArrayType() {
        GenericArrayTypeDescriptor genericArrayType = visitorHelper.getStore().create(GenericArrayTypeDescriptor.class);
        apply(genericArrayType);
        return new AbstractBoundVisitor(visitorHelper, containingType) {
            @Override
            protected void apply(BoundDescriptor bound) {
                genericArrayType.setComponentType(bound);
            }
        };
    }

    @Override
    public final void visitTypeArgument() {
    }

    @Override
    public final SignatureVisitor visitTypeArgument(char wildcard) {
        ParameterizedTypeDescriptor parameterizedType = visitorHelper.getStore().addDescriptorType(current, ParameterizedTypeDescriptor.class);
        if (wildcard == INSTANCEOF) {
            return new AbstractBoundVisitor(visitorHelper, containingType) {
                @Override
                protected void apply(BoundDescriptor bound) {
                    HasActualTypeArgumentDescriptor hasActualTypeArgument = visitorHelper.getStore().create(parameterizedType,
                            HasActualTypeArgumentDescriptor.class, bound);
                    hasActualTypeArgument.setIndex(currentTypeParameterIndex++);
                }
            };
        } else {
            WildcardTypeDescriptor wildcardType = visitorHelper.getStore().create(WildcardTypeDescriptor.class);
            HasActualTypeArgumentDescriptor hasActualTypeArgument = visitorHelper.getStore().create(parameterizedType, HasActualTypeArgumentDescriptor.class,
                    wildcardType);
            hasActualTypeArgument.setIndex(currentTypeParameterIndex++);
            return new AbstractBoundVisitor(visitorHelper, containingType) {
                @Override
                protected void apply(BoundDescriptor bound) {
                    switch (wildcard) {
                    case EXTENDS:
                        wildcardType.getUpperBounds().add(bound);
                        break;
                    case SUPER:
                        wildcardType.getLowerBounds().add(bound);
                        break;
                    }
                }
            };
        }
    }

    protected abstract void apply(BoundDescriptor bound);

}
