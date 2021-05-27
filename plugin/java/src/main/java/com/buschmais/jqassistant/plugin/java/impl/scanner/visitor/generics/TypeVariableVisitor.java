package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.TypeVariableDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

class TypeVariableVisitor extends AbstractBoundVisitor<TypeVariableDescriptor> {

    TypeVariableVisitor(TypeVariableDescriptor genericType, VisitorHelper visitorHelper,
            TypeCache.CachedType<? extends ClassFileDescriptor> containingType) {
        super(genericType, visitorHelper, containingType);
    }

    @Override
    protected void apply(BoundDescriptor bound) {
        boundTarget.getBounds().add(bound);
    }

}
