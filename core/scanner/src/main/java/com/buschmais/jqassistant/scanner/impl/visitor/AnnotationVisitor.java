package com.buschmais.jqassistant.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.DependentDescriptor;
import com.buschmais.jqassistant.scanner.impl.resolver.DescriptorResolverFactory;
import org.objectweb.asm.Type;

/**
 * An annotation visitor.
 * <p>Adds a dependency from the annotated type to the types of the annotation values.</p>
 */
public class AnnotationVisitor extends AbstractVisitor implements org.objectweb.asm.AnnotationVisitor {

    private final DependentDescriptor parentDescriptor;

    /**
     * Constructor.
     *
     * @param dependentDescriptor The descriptor which annotated (e.g. class, field or method.)
     * @param resolverFactory     The .{@link DescriptorResolverFactory}.
     */
    protected AnnotationVisitor(DependentDescriptor dependentDescriptor, DescriptorResolverFactory resolverFactory) {
        super(resolverFactory);
        this.parentDescriptor = dependentDescriptor;
    }

    @Override
    public void visit(final String name, final Object value) {
        if (value instanceof Type) {
            addDependency(parentDescriptor, getType((Type) value));
        }
    }

    @Override
    public void visitEnum(final String name, final String desc, final String value) {
        addDependency(parentDescriptor, getType((desc)));
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        addDependency(parentDescriptor, getType((desc)));
        return this;
    }

    @Override
    public AnnotationVisitor visitArray(final String name) {
        return this;
    }

    @Override
    public void visitEnd() {
    }
}
