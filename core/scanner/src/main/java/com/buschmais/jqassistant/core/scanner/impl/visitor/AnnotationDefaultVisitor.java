package com.buschmais.jqassistant.core.scanner.impl.visitor;

import com.buschmais.jqassistant.core.model.api.descriptor.MethodDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.value.ClassValueDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.value.PrimitiveValueDescriptor;
import org.objectweb.asm.*;
import org.objectweb.asm.AnnotationVisitor;

/**
 * Visitor for default values of annotation methods.
 * <p>Creates dependencies of the method to the type of the default value.</p>
 */
public class AnnotationDefaultVisitor extends org.objectweb.asm.AnnotationVisitor {

    private MethodDescriptor methodDescriptor;
    private VisitorHelper visitorHelper;

    public AnnotationDefaultVisitor(MethodDescriptor methodDescriptor, VisitorHelper visitorHelper) {
        super(Opcodes.ASM4);
        this.methodDescriptor = methodDescriptor;
        this.visitorHelper = visitorHelper;
    }

    @Override
    public void visit(String name, Object value) {
        TypeDescriptor typeDescriptor;
        if (value instanceof Type) {
            String type = visitorHelper.getType((Type) value);
            typeDescriptor = visitorHelper.getTypeDescriptor(type);
        } else {
            typeDescriptor = visitorHelper.getTypeDescriptor(value.getClass().getName());
        }
        methodDescriptor.getDependencies().add(typeDescriptor);
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
        TypeDescriptor typeDescriptor = visitorHelper.getTypeDescriptor(visitorHelper.getType(desc));
        methodDescriptor.getDependencies().add(typeDescriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
        TypeDescriptor typeDescriptor = visitorHelper.getTypeDescriptor(visitorHelper.getType(desc));
        methodDescriptor.getDependencies().add(typeDescriptor);
        return this;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return this;
    }

    @Override
    public void visitEnd() {
    }
}
