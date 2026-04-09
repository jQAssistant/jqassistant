package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.api.model.TypeClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.*;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.ClassFileVisitorContext;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Abstract signature visitor class to determine generic bounds.
 */
public abstract class AbstractBoundVisitor extends SignatureVisitor {

    private static final String DEFAULT_RAW_TYPE_BOUND = "java.lang.Object";

    protected final ClassFileVisitorContext classFileVisitorContext;

    private final TypeClassFileDescriptor containingType;

    private final List<BoundDescriptor> actualTypeArguments = new ArrayList<>();

    private BoundDescriptor current;

    public AbstractBoundVisitor(ClassFileVisitorContext classFileVisitorContext, TypeClassFileDescriptor containingType) {
        super(ClassFileVisitorContext.ASM_OPCODES);
        this.classFileVisitorContext = classFileVisitorContext;
        this.containingType = containingType;
    }

    @Override
    public final void visitBaseType(char descriptor) {
        createBound(Type.getType(Character.toString(descriptor))
            .getClassName());
    }

    @Override
    public final void visitClassType(String name) {
        createBound(SignatureHelper.getObjectType(name));
    }

    @Override
    public final void visitInnerClassType(String name) {
        createBound(SignatureHelper.getObjectType(name));
    }

    private void createBound(String rawTypeName) {
        current = classFileVisitorContext.getStore()
            .create(BoundDescriptor.class);
        TypeDescriptor rawType = classFileVisitorContext.resolveType(rawTypeName);
        current.setRawType(rawType);
        apply(rawType, current);
    }

    @Override
    public final void visitTypeVariable(String name) {
        TypeVariableDescriptor typeVariable = classFileVisitorContext.getTypeVariableResolver()
            .resolve(name, containingType);
        apply(typeVariable);
    }

    @Override
    public final SignatureVisitor visitArrayType() {
        GenericArrayTypeDescriptor genericArrayType = classFileVisitorContext.getStore()
            .create(GenericArrayTypeDescriptor.class);
        apply(genericArrayType);
        return new AbstractBoundVisitor(classFileVisitorContext, containingType) {
            @Override
            protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                genericArrayType.setComponentType(bound);
            }
        };
    }

    @Override
    public final void visitTypeArgument() {
        WildcardTypeDescriptor wildcardType = classFileVisitorContext.getStore()
            .create(WildcardTypeDescriptor.class);
        addActualArgumentType(wildcardType);
    }

    @Override
    public final SignatureVisitor visitTypeArgument(char wildcard) {
        if (wildcard == INSTANCEOF) {
            return new AbstractBoundVisitor(classFileVisitorContext, containingType) {
                @Override
                protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                    addActualArgumentType(bound);
                }
            };
        } else {
            WildcardTypeDescriptor wildcardType = classFileVisitorContext.getStore()
                .create(WildcardTypeDescriptor.class);
            addActualArgumentType(wildcardType);
            return new AbstractBoundVisitor(classFileVisitorContext, containingType) {
                @Override
                protected void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound) {
                    switch (wildcard) {
                    case EXTENDS:
                        wildcardType.getUpperBounds()
                            .add(bound);
                        break;
                    case SUPER:
                        wildcardType.getLowerBounds()
                            .add(bound);
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
        apply(rawType != null ? rawType : classFileVisitorContext.resolveType(DEFAULT_RAW_TYPE_BOUND), bound);
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
            Store store = classFileVisitorContext.getStore();
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
        return bounds.size() == 1 ?
            bounds.stream()
            .findFirst()
            .get()
            .getRawType() :
            null;
    }

    protected abstract void apply(TypeDescriptor rawTypeBound, BoundDescriptor bound);

}
