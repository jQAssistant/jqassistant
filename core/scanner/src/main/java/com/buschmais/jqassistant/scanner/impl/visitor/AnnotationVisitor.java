package com.buschmais.jqassistant.scanner.impl.visitor;

import com.buschmais.jqassistant.scanner.impl.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.store.api.model.descriptor.DependentDescriptor;
import org.objectweb.asm.Type;

public class AnnotationVisitor extends AbstractVisitor implements org.objectweb.asm.AnnotationVisitor {

    private final DependentDescriptor parentDescriptor;

    protected AnnotationVisitor(DependentDescriptor parentDescriptor, DescriptorResolverFactory resolverFactory) {
        super(resolverFactory);
        this.parentDescriptor = parentDescriptor;
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
