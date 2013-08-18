package com.buschmais.jqassistant.core.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.DependentDescriptor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * An annotation visitor.
 * <p>Adds a dependency from the annotated types to the types of the annotation values.</p>
 */
public class AnnotationVisitor extends org.objectweb.asm.AnnotationVisitor {

    private final DependentDescriptor parentDescriptor;

    private VisitorHelper visitorHelper;

    /**
     * Constructor.
     *
     * @param dependentDescriptor The descriptor which annotated (e.g. class, field or method.)
     * @param visitorHelper       The {@link VisitorHelper}.
     */
    protected AnnotationVisitor(DependentDescriptor dependentDescriptor, VisitorHelper visitorHelper) {
        super(Opcodes.ASM4);
        this.parentDescriptor = dependentDescriptor;
        this.visitorHelper = visitorHelper;
    }

    @Override
    public void visit(final String name, final Object value) {
        if (value instanceof Type) {
            visitorHelper.addDependency(parentDescriptor, visitorHelper.getType((Type) value));
        }
    }

    @Override
    public void visitEnum(final String name, final String desc, final String value) {
        visitorHelper.addDependency(parentDescriptor, visitorHelper.getType((desc)));
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        visitorHelper.addDependency(parentDescriptor, visitorHelper.getType((desc)));
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
