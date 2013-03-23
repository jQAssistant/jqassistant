package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.Type;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;

public class AnnotationVisitor extends AbstractVisitor implements
		org.objectweb.asm.AnnotationVisitor {

	private final ClassDescriptor classDescriptor;

	protected AnnotationVisitor(Store store, ClassDescriptor classDescriptor) {
		super(store);
		this.classDescriptor = classDescriptor;
	}

	@Override
	public void visit(final String name, final Object value) {
		if (value instanceof Type) {
			addDependency(classDescriptor, getType((Type) value));
		}
	}

	@Override
	public void visitEnum(final String name, final String desc,
			final String value) {
		addDependency(classDescriptor, getType((desc)));
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String name,
			final String desc) {
		addDependency(classDescriptor, getType((desc)));
		return this;
	}

	@Override
	public AnnotationVisitor visitArray(final String name) {
		return this;
	}

	@Override
	public void visitEnd() {
	}
}
