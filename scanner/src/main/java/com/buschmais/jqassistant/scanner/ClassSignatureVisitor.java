package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.signature.SignatureVisitor;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;

public class ClassSignatureVisitor extends AbstractSignatureVisitor implements
		SignatureVisitor {

	private final ClassDescriptor classDescriptor;

	protected ClassSignatureVisitor(Store store, ClassDescriptor classDescriptor) {
		super(store);
		this.classDescriptor = classDescriptor;
	}

	@Override
	public SignatureVisitor visitClassBound() {
		return new AbstractSignatureVisitor(getStore()) {

			@Override
			public void visitClassType(String name) {
				classDescriptor.addDependency(getClassDescriptor(name));
			}

			@Override
			public void visitInnerClassType(String name) {
				classDescriptor.addDependency(getClassDescriptor(name));
			}

		};
	}

	@Override
	public SignatureVisitor visitInterfaceBound() {
		return new AbstractSignatureVisitor(getStore()) {

			@Override
			public void visitClassType(String name) {
				classDescriptor.addDependency(getClassDescriptor(name));
			}

			@Override
			public void visitInnerClassType(String name) {
				classDescriptor.addDependency(getClassDescriptor(name));
			}
		};
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		return new AbstractSignatureVisitor(getStore()) {

			@Override
			public void visitClassType(String name) {
				classDescriptor.addSuperClass(getClassDescriptor(name));
			}

			@Override
			public void visitInnerClassType(String name) {
				classDescriptor.addSuperClass(getClassDescriptor(name));
			}

		};
	}

	@Override
	public SignatureVisitor visitInterface() {
		return new AbstractSignatureVisitor(getStore()) {

			@Override
			public void visitClassType(String name) {
				classDescriptor.addImplements(getClassDescriptor(name));
			}

			@Override
			public void visitInnerClassType(String name) {
				classDescriptor.addImplements(getClassDescriptor(name));
			}
		};
	}

	@Override
	public void visitTypeVariable(String name) {
		classDescriptor.addDependency(getClassDescriptor(name));
	}

	@Override
	public SignatureVisitor visitArrayType() {
		return new AbstractSignatureVisitor(getStore()) {

			@Override
			public void visitClassType(String name) {
				classDescriptor.addDependency(getClassDescriptor(name));
			}

			@Override
			public void visitInnerClassType(String name) {
				classDescriptor.addDependency(getClassDescriptor(name));
			}
		};
	}

	@Override
	public SignatureVisitor visitTypeArgument(char wildcard) {
		return new AbstractSignatureVisitor(getStore()) {

			@Override
			public void visitClassType(String name) {
				classDescriptor.addDependency(getClassDescriptor(name));
			}

			@Override
			public void visitInnerClassType(String name) {
				classDescriptor.addDependency(getClassDescriptor(name));
			}
		};
	}

}
