package com.buschmais.jqassistant.plugin.java.impl.store.visitor;

import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.AnnotationValueDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ValueDescriptor;

/**
 * An annotation visitor.
 * <p>
 * Adds a dependency from the annotated types to the types of the annotation
 * values.
 * </p>
 */
public class AnnotationVisitor extends AbstractAnnotationVisitor<AnnotationValueDescriptor> {

    /**
     * Constructor.
     * 
     * @param visitorHelper
     *            The
     *            {@link com.buschmais.jqassistant.plugin.java.impl.store.visitor.VisitorHelper}
     *            .
     */
    protected AnnotationVisitor(AnnotationValueDescriptor descriptor, VisitorHelper visitorHelper) {
        super(descriptor, visitorHelper);
    }

    @Override
    protected void setValue(AnnotationValueDescriptor descriptor, ValueDescriptor value) {
        descriptor.getValue().add(value);
    }
}
