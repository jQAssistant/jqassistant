package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
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

    private static final String DEFAULT_RAW_TYPE_BOUND = "java.lang.Object";

    protected final VisitorHelper visitorHelper;

    private final TypeCache.CachedType<? extends ClassFileDescriptor> containingType;

    private BoundDescriptor current;

    private List<BoundDescriptor> actualTypeArguments = new ArrayList<>();

    public AbstractBoundVisitor(VisitorHelper visitorHelper, TypeCache.CachedType<? extends ClassFileDescriptor> containingType) {
        super(VisitorHelper.ASM_OPCODES);
        this.visitorHelper = visitorHelper;
        this.containingType = containingType;
    }

    @Override
    public final void visitBaseType(char descriptor) {
        createBound(Type.getType(Character.toString(descriptor)).getClassName());
    }

    @Override
    public final void visitClassType(String name) {
        createBound(SignatureHelper.getObjectType(name));
    }

    @Override
    public final void visitInnerClassType(String name) {
        createBound(SignatureHelper.getObjectType(name));
    }

    private final void createBound(String rawTypeName) {
        current = visitorHelper.getStore().create(BoundDescriptor.class);
        TypeDescriptor rawType = visitorHelper.resolveType(rawTypeName, containingType).getTypeDescriptor();
        current.setRawType(rawType);
        apply(rawType, current);
    }

    @Override
    public final void visitTypeVariable(String name) {
        TypeVariableDescriptor typeVariable = visitorHelper.getTypeVariableResolver().resolve(name, containingType.getTypeDescriptor());
        apply(typeVariable);
    }

    @Override
    public final SignatureVisitor visitArrayType() {
        GenericArrayTypeDescriptor genericArrayType = visitorHelper.getStore().create(GenericArrayTypeDescriptor.class);
        apply(genericArrayType);
        return new AbstractBoundVisitor(visitorHelper, containingType) {
            @Override
            protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                genericArrayType.setComponentType(bound);
            }
        };
    }

    @Override
    public final void visitTypeArgument() {
        WildcardTypeDescriptor wildcardType = visitorHelper.getStore().create(WildcardTypeDescriptor.class);
        addActualArgumentType(wildcardType);
    }

    @Override
    public final SignatureVisitor visitTypeArgument(char wildcard) {
        if (wildcard == INSTANCEOF) {
            return new AbstractBoundVisitor(visitorHelper, containingType) {
                @Override
                protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                    addActualArgumentType(bound);
                }
            };
        } else {
            WildcardTypeDescriptor wildcardType = visitorHelper.getStore().create(WildcardTypeDescriptor.class);
            addActualArgumentType(wildcardType);
            return new AbstractBoundVisitor(visitorHelper, containingType) {
                @Override
                protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
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

    private void addActualArgumentType(BoundDescriptor argumentType) {
        actualTypeArguments.add(argumentType);
    }

    private void apply(BoundDescriptor bound) {
        TypeDescriptor rawType = getRawTypeBound(bound);
        apply(rawType != null ? rawType : visitorHelper.resolveType(DEFAULT_RAW_TYPE_BOUND, containingType).getTypeDescriptor(), bound);
    }

    /**
     * Determine the raw type bound for a {@link BoundDescriptor}.
     *
     * @param bound
     *     The {@link BoundDescriptor}.
     * @return The raw type bound (optional).
     */
    private TypeDescriptor getRawTypeBound(BoundDescriptor bound) {
        if (bound instanceof TypeVariableDescriptor) {
            TypeVariableDescriptor typeVariable = (TypeVariableDescriptor) bound;
            return getUniqueRawTypeBound(typeVariable.getUpperBounds());
        } else if (bound instanceof WildcardTypeDescriptor) {
            WildcardTypeDescriptor wildcardType = (WildcardTypeDescriptor) bound;
            List<BoundDescriptor> lowerBounds = wildcardType.getLowerBounds();
            List<BoundDescriptor> upperBounds = wildcardType.getUpperBounds();
            if (lowerBounds.size() == 1) {
                return getUniqueRawTypeBound(lowerBounds);
            } else if (upperBounds.size() == 1) {
                return getUniqueRawTypeBound(upperBounds);
            }
        }
        return bound.getRawType();
    }

    @Override
    public final void visitEnd() {
        if (!actualTypeArguments.isEmpty()) {
            Store store = visitorHelper.getStore();
            ParameterizedTypeDescriptor parameterizedType = store.addDescriptorType(current, ParameterizedTypeDescriptor.class);
            int index = 0;
            for (BoundDescriptor actualTypeArgument : actualTypeArguments) {
                HasActualTypeArgumentDescriptor hasActualTypeArgument = store.create(parameterizedType, HasActualTypeArgumentDescriptor.class,
                    actualTypeArgument);
                hasActualTypeArgument.setIndex(index++);
            }
        }
    }

    /**
     * Return a raw bound from a list of {@link BoundDescriptor}s if it is
     * non-ambiguous.
     *
     * @param bounds
     *     The {@link BoundDescriptor}.
     * @return The raw bound or <code>null</code> if it is ambiguous.
     */
    private TypeDescriptor getUniqueRawTypeBound(List<BoundDescriptor> bounds) {
        return bounds.size() == 1 ? bounds.stream().findFirst().get().getRawType() : null;
    }

    protected abstract void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound);

}
