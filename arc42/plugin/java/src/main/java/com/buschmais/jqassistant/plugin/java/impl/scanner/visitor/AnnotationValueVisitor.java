package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.plugin.common.api.model.ValueDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.AnnotationValueDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;

/**
 * An annotation visitor that adds a {@link ValueDescriptor} to the current
 * {@link AnnotationValueDescriptor}.
 */
public class AnnotationValueVisitor extends AbstractAnnotationVisitor<AnnotationValueDescriptor> {

    /**
     * Constructor.
     * 
     * @param visitorHelper
     *            The
     *            {@link com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper}
     *            .
     */
    protected AnnotationValueVisitor(TypeCache.CachedType containingType, AnnotationValueDescriptor descriptor, VisitorHelper visitorHelper) {
        super(containingType, descriptor, visitorHelper);
    }

    @Override
    protected void setValue(AnnotationValueDescriptor descriptor, ValueDescriptor<?> value) {
        descriptor.getValue().add(value);
    }
}
