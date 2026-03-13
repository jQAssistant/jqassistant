package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.plugin.common.api.model.ValueDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;

/**
 * Visitor for default values of annotation methods.
 *
 * Creates dependencies of the method to the type of the default value.
 */
public class AnnotationDefaultVisitor extends AbstractAnnotationVisitor<MethodDescriptor> {

    /**
     * Constructor.
     * 
     * @param visitorHelper
     *            The
     *            {@link com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper}
     *            .
     */
    protected AnnotationDefaultVisitor(TypeCache.CachedType containingType, MethodDescriptor descriptor, VisitorHelper visitorHelper) {
        super(containingType, descriptor, visitorHelper);
    }

    @Override
    protected void setValue(MethodDescriptor descriptor, ValueDescriptor<?> value) {
        descriptor.setHasDefault(value);
    }
}
