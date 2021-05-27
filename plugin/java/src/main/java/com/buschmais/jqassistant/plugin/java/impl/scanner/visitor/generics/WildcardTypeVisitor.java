package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.generics;

import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.WildcardTypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;
import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

class WildcardTypeVisitor extends AbstractBoundVisitor<WildcardTypeDescriptor> {

    private final char wildcard;

    WildcardTypeVisitor(WildcardTypeDescriptor targetType, char wildcard, VisitorHelper visitorHelper,
            TypeCache.CachedType<? extends ClassFileDescriptor> containingType) {
        super(targetType, visitorHelper, containingType);
        this.wildcard = wildcard;
    }

    @Override
    protected void apply(BoundDescriptor bound) {
        switch (wildcard) {
        case EXTENDS:
            boundTarget.getUpperBounds().add(bound);
            break;
        case SUPER:
            boundTarget.getLowerBounds().add(bound);
            break;
        }
    }
}
