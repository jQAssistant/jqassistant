package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.HasActualTypeArgumentDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.ParameterizedTypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

class ParameterizedTypeVisitor extends AbstractBoundVisitor<ParameterizedTypeDescriptor> {

    private final int typeParameterIndex;

    ParameterizedTypeVisitor(ParameterizedTypeDescriptor targetType, int typeParameterIndex, VisitorHelper visitorHelper,
            TypeCache.CachedType<? extends ClassFileDescriptor> containingType) {
        super(targetType, visitorHelper, containingType);
        this.typeParameterIndex = typeParameterIndex;
    }

    @Override
    protected void apply(BoundDescriptor bound) {
        HasActualTypeArgumentDescriptor hasActualTypeArgument = visitorHelper.getStore().create(boundTarget, HasActualTypeArgumentDescriptor.class, bound);
        hasActualTypeArgument.setIndex(typeParameterIndex);
    }
}
