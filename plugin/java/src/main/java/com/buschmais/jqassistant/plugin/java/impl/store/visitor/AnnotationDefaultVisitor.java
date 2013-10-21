package com.buschmais.jqassistant.plugin.java.impl.store.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;

/**
 * Visitor for default values of annotation methods.
 * <p>
 * Creates dependencies of the method to the type of the default value.
 * </p>
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
