package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.delegate;

import java.util.List;

import com.buschmais.jqassistant.plugin.java.impl.scanner.visitor.VisitorHelper;

import org.objectweb.asm.AnnotationVisitor;

public class DelegatingAnnotationVisitor extends AnnotationVisitor {

    private Delegator<AnnotationVisitor> delegator;

    public DelegatingAnnotationVisitor(List<AnnotationVisitor> visitors) {
        super(VisitorHelper.ASM_OPCODES);
        this.delegator = new Delegator<>(visitors);
    }

    @Override
    public void visit(String name, Object value) {
        delegator.accept(visitor -> visitor.visit(name, value));
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        delegator.accept(visitor -> visitor.visitEnum(name, descriptor, value));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        return new DelegatingAnnotationVisitor(delegator.apply(visitor -> visitor.visitAnnotation(name, descriptor)));
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return new DelegatingAnnotationVisitor(delegator.apply(visitor -> visitor.visitArray(name)));
    }

    @Override
    public void visitEnd() {
        delegator.accept(visitor -> visitor.visitEnd());
    }
}
