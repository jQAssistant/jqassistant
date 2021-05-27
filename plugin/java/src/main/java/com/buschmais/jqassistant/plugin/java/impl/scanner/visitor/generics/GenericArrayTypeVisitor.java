package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.GenericArrayTypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

class GenericArrayTypeVisitor extends AbstractBoundVisitor<GenericArrayTypeDescriptor> {

    GenericArrayTypeVisitor(GenericArrayTypeDescriptor targetType, VisitorHelper visitorHelper,
            TypeCache.CachedType<? extends ClassFileDescriptor> containingType) {
        super(targetType, visitorHelper, containingType);
    }

    @Override
    protected void apply(BoundDescriptor bound) {
        boundTarget.setComponentType(bound);
    }
}
