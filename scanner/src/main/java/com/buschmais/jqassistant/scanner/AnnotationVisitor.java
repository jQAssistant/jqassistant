package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.Type;

import com.buschmais.jqassistant.store.model.ClassDescriptor;

public class AnnotationVisitor extends AbstractVisitor implements org.objectweb.asm.AnnotationVisitor {

    private ClassDescriptor classDescriptor;

    protected AnnotationVisitor(DependencyModel model, ClassDescriptor classDescriptor) {
        super(model);
        this.classDescriptor = classDescriptor;
    }

    @Override
    public void visit(final String name, final Object value) {
        if (value instanceof Type) {
            getModel().addDependency(classDescriptor, getType((Type) value));
        }
    }

    @Override
    public void visitEnum(final String name, final String desc, final String value) {
        getModel().addDependency(classDescriptor, getType((desc)));
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        getModel().addDependency(classDescriptor, getType((desc)));
        return this;
    }

    @Override
    public AnnotationVisitor visitArray(final String name) {
        return this;
    }
}
