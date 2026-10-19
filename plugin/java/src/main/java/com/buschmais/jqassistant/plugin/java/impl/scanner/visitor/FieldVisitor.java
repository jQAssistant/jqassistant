package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;

import static org.objectweb.asm.TypeReference.FIELD;

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

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        TypeReference typeReference = new TypeReference(typeRef);
        if (typeReference.getSort() == FIELD && typePath == null) {
            return classFileVisitorContext.addAnnotation(fieldDescriptor, SignatureHelper.getType(descriptor));
        }
        return null;
    }
}
