package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.*;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * Abstract signature visitor class to determine bounds of a target
 * {@link BoundDescriptor}.
 *
 * @param <T>
 *            The type of the target {@link BoundDescriptor}.
 */
public abstract class AbstractBoundVisitor<T extends BoundDescriptor> extends SignatureVisitor {

    protected final VisitorHelper visitorHelper;

    /**
     * The {@link BoundDescriptor} that is the target to add bounds added.
     */
    protected final T boundTarget;

    private final TypeCache.CachedType<? extends ClassFileDescriptor> containingType;

    private BoundDescriptor current;

    private int currentTypeParameterIndex = 0;

    public AbstractBoundVisitor(T boundTarget, VisitorHelper visitorHelper, TypeCache.CachedType<? extends ClassFileDescriptor> containingType) {
        super(VisitorHelper.ASM_OPCODES);
        this.boundTarget = boundTarget;
        this.visitorHelper = visitorHelper;
        this.containingType = containingType;
    }

    // visitBaseType | visitTypeVariable | visitArrayType | ( visitClassType
    // visitTypeArgument* ( visitInnerClassType visitTypeArgument* )* visitEnd )

    @Override
    public void visitBaseType(char descriptor) {
        // TODO check if this is the right way to determine the primitive type
        createBound(Type.getType(Character.toString(descriptor)).toString());
    }

    @Override
    public void visitClassType(String name) {
        createBound(SignatureHelper.getObjectType(name));
    }

    @Override
    public void visitInnerClassType(String name) {
        createBound(SignatureHelper.getObjectType(name));
    }

    private void createBound(String rawType) {
        current = visitorHelper.getStore().create(BoundDescriptor.class);
        current.setRawType(visitorHelper.resolveType(rawType, containingType).getTypeDescriptor());
        apply(current);
    }

    @Override
    public void visitTypeVariable(String name) {
        // TODO resolve type variables from declaring method, type or outer type
    }

    @Override
    public SignatureVisitor visitArrayType() {
        GenericArrayTypeDescriptor genericArrayType = visitorHelper.getStore().create(GenericArrayTypeDescriptor.class);
        apply(genericArrayType);
        return new GenericArrayTypeVisitor(genericArrayType, visitorHelper, containingType);
    }

    @Override
    public void visitTypeArgument() {
    }

    @Override
    public SignatureVisitor visitTypeArgument(char wildcard) {
        ParameterizedTypeDescriptor parameterizedType = visitorHelper.getStore().addDescriptorType(current, ParameterizedTypeDescriptor.class);
        if (wildcard == INSTANCEOF) {
            return new ParameterizedTypeVisitor(parameterizedType, currentTypeParameterIndex++, visitorHelper, containingType);
        } else {
            WildcardTypeDescriptor wildcardType = visitorHelper.getStore().create(WildcardTypeDescriptor.class);
            HasActualTypeArgumentDescriptor hasActualTypeArgument = visitorHelper.getStore().create(parameterizedType, HasActualTypeArgumentDescriptor.class,
                    wildcardType);
            hasActualTypeArgument.setIndex(currentTypeParameterIndex++);
            return new WildcardTypeVisitor(wildcardType, wildcard, visitorHelper, containingType);
        }
    }

    protected abstract void apply(BoundDescriptor bound);

}
