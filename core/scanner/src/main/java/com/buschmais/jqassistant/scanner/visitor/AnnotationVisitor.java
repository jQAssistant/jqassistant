package com.buschmais.jqassistant.scanner.visitor;

import org.objectweb.asm.Type;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.DependentDescriptor;

public class AnnotationVisitor extends AbstractVisitor implements
		org.objectweb.asm.AnnotationVisitor {

	private final DependentDescriptor parentDescriptor;

	protected AnnotationVisitor(Store store,
			DependentDescriptor parentDescriptor) {
		super(store);
		this.parentDescriptor = parentDescriptor;
	}

	@Override
	public void visit(final String name, final Object value) {
		if (value instanceof Type) {
			addDependency(parentDescriptor, getType((Type) value));
		}
	}

	@Override
	public void visitEnum(final String name, final String desc,
			final String value) {
		addDependency(parentDescriptor, getType((desc)));
	}

	@Override
	public AnnotationVisitor visitAnnotation(final String name,
			final String desc) {
		addDependency(parentDescriptor, getType((desc)));
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
