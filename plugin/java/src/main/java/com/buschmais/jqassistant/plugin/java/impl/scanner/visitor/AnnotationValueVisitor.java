package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.plugin.common.api.model.ValueDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.AnnotationValueDescriptor;

/**
 * An annotation visitor that adds a {@link ValueDescriptor} to the current
 * {@link AnnotationValueDescriptor}.
 */
public class AnnotationValueVisitor extends AbstractAnnotationVisitor<AnnotationValueDescriptor> {

    /**
     * Constructor.
     *
     * @param classFileVisitorContext
     *     The
     *     {@link ClassFileVisitorContext}
     *     .
     */
    protected AnnotationValueVisitor(AnnotationValueDescriptor descriptor, ClassFileVisitorContext classFileVisitorContext) {
        super(descriptor, classFileVisitorContext);
    }

    @Override
    protected void setValue(AnnotationValueDescriptor descriptor, ValueDescriptor<?> value) {
        descriptor.getValue()
            .add(value);
    }
}
