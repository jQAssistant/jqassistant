package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.Attribute;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;

public class FieldVisitor extends AbstractVisitor implements
		org.objectweb.asm.FieldVisitor {

	private final ClassDescriptor classDescriptor;

	protected FieldVisitor(Store store, ClassDescriptor classDescriptor) {
		super(store);
		this.classDescriptor = classDescriptor;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		return new AnnotationVisitor(getStore(), classDescriptor);
	}

	@Override
	public void visitAttribute(Attribute attribute) {
	}

	@Override
	public void visitEnd() {
	}

}
