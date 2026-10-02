package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import com.buschmais.jqassistant.plugin.java.api.model.AnnotatedDescriptor;

import lombok.extern.slf4j.Slf4j;
import org.objectweb.asm.AnnotationVisitor;

/**
 * {@link AnnotationVisitor} for processing jQASuppress.Repeatable annotations.
 */
@Slf4j
class RepeatableSuppressAnnotationVisitor extends AnnotationVisitor {

    private final AnnotatedDescriptor annotatedDescriptor;

    private final ClassFileVisitorContext classFileVisitorContext;

    private String currentAttribute;

    public RepeatableSuppressAnnotationVisitor(AnnotatedDescriptor annotatedDescriptor, ClassFileVisitorContext classFileVisitorContext) {
        super(ClassFileVisitorContext.ASM_OPCODES);
        this.annotatedDescriptor = annotatedDescriptor;
        this.classFileVisitorContext = classFileVisitorContext;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        this.currentAttribute = name;
        return this;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        if ("value".equals(currentAttribute)) {
            return new SuppressAnnotationVisitor(annotatedDescriptor, classFileVisitorContext);
        }
        return null;
    }
}
