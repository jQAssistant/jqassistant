package com.buschmais.jqassistant.scanner.visitor;

import org.objectweb.asm.Attribute;

import com.buschmais.jqassistant.scanner.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.store.api.model.FieldDescriptor;

public class FieldVisitor extends AbstractVisitor implements
		org.objectweb.asm.FieldVisitor {

	private final FieldDescriptor fieldDescriptor;

	protected FieldVisitor(FieldDescriptor fieldDescriptor,
			DescriptorResolverFactory resolverFactory) {
		super(resolverFactory);
		this.fieldDescriptor = fieldDescriptor;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		return new AnnotationVisitor(fieldDescriptor,
				getClassDescriptorResolver());
	}

	@Override
	public void visitAttribute(Attribute attribute) {
	}

	@Override
	public void visitEnd() {
	}

}
