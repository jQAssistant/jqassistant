package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.SignatureHelper;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

public class FieldVisitor extends org.objectweb.asm.FieldVisitor {

    private final TypeCache.CachedType containingType;
    private final FieldDescriptor fieldDescriptor;
    private final VisitorHelper visitorHelper;

    protected FieldVisitor(TypeCache.CachedType containingType, FieldDescriptor fieldDescriptor, VisitorHelper visitorHelper) {
        super(Opcodes.ASM7);
        this.containingType = containingType;
        this.fieldDescriptor = fieldDescriptor;
        this.visitorHelper = visitorHelper;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
        return visitorHelper.addAnnotation(containingType, fieldDescriptor, SignatureHelper.getType(arg0));
    }
}
