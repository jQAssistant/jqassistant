package com.buschmais.jqassistant.plugin.java.impl.store.visitor;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;

import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.AnnotationValueDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.FieldDescriptor;

public class FieldVisitor extends org.objectweb.asm.FieldVisitor {

	private final FieldDescriptor fieldDescriptor;
	private VisitorHelper visitorHelper;

	protected FieldVisitor(FieldDescriptor fieldDescriptor, VisitorHelper visitorHelper) {
		super(Opcodes.ASM4);
		this.fieldDescriptor = fieldDescriptor;
		this.visitorHelper = visitorHelper;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		AnnotationValueDescriptor annotationDescriptor = visitorHelper.addAnnotation(fieldDescriptor, visitorHelper.getType(arg0));
		return new AnnotationVisitor(annotationDescriptor, visitorHelper);
	}

	@Override
	public void visitAttribute(Attribute attribute) {
	}

	@Override
	public void visitEnd() {
	}

}
