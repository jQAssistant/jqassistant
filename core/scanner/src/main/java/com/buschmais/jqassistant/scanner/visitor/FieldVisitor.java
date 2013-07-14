package com.buschmais.jqassistant.scanner.visitor;

import com.buschmais.jqassistant.scanner.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.store.api.model.FieldDescriptor;
import org.objectweb.asm.Attribute;

public class FieldVisitor extends AbstractVisitor implements org.objectweb.asm.FieldVisitor {

    private final FieldDescriptor fieldDescriptor;

    protected FieldVisitor(FieldDescriptor fieldDescriptor, DescriptorResolverFactory resolverFactory) {
        super(resolverFactory);
        this.fieldDescriptor = fieldDescriptor;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
        addAnnotation(fieldDescriptor, getType(arg0));
        return new AnnotationVisitor(fieldDescriptor, getResolverFactory());
    }

    @Override
    public void visitAttribute(Attribute attribute) {
    }

    @Override
    public void visitEnd() {
    }

}
