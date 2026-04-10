package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;

import org.objectweb.asm.AnnotationVisitor;

public class FieldVisitor extends org.objectweb.asm.FieldVisitor {

    private final FieldDescriptor fieldDescriptor;
    private final ClassFileVisitorContext classFileVisitorContext;

    protected FieldVisitor(FieldDescriptor fieldDescriptor, ClassFileVisitorContext classFileVisitorContext) {
        super(ClassFileVisitorContext.ASM_OPCODES);
        this.fieldDescriptor = fieldDescriptor;
        this.classFileVisitorContext = classFileVisitorContext;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
        return classFileVisitorContext.addAnnotation(fieldDescriptor, SignatureHelper.getType(arg0));
    }
}
