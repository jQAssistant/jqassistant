package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.plugin.common.api.model.ValueDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.AnnotationValueDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;

/**
 * An annotation visitor.
 *
 * Adds a dependency from the annotated types to the types of the annotation
 * values.
 *
 */
public class AnnotationVisitor extends AbstractAnnotationVisitor<AnnotationValueDescriptor> {

    /**
     * Constructor.
     * 
     * @param visitorHelper
     *            The
     *            {@link com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper}
     *            .
     */
    protected AnnotationVisitor(TypeCache.CachedType containingType, AnnotationValueDescriptor descriptor, VisitorHelper visitorHelper) {
        super(containingType, descriptor, visitorHelper);
    }

    @Override
    protected void setValue(AnnotationValueDescriptor descriptor, ValueDescriptor<?> value) {
        descriptor.getValue().add(value);
    }
}
