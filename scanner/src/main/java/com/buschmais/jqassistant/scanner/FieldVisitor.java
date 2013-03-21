package com.buschmais.jqassistant.scanner;

import com.buschmais.jqassistant.store.model.ClassDescriptor;

public class FieldVisitor extends AbstractVisitor implements org.objectweb.asm.FieldVisitor {

    private ClassDescriptor classDescriptor;

    protected FieldVisitor(DependencyModel model, ClassDescriptor classDescriptor) {
        super(model);
        this.classDescriptor = classDescriptor;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
        return new AnnotationVisitor(getModel(), classDescriptor);
    }

}
