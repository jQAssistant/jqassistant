package com.buschmais.jqassistant.scanner.visitor;

import org.objectweb.asm.signature.SignatureVisitor;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;

public class ClassSignatureVisitor extends
		DependentSignatureVisitor<ClassDescriptor> implements SignatureVisitor {

	protected ClassSignatureVisitor(Store store, ClassDescriptor classDescriptor) {
		super(store, classDescriptor);
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		return new DependentSignatureVisitor<ClassDescriptor>(getStore(),
				getDependentDescriptor()) {

			@Override
			public void visitClassType(String name) {
				getDependentDescriptor()
						.addSuperClass(getClassDescriptor(name));
			}

			@Override
			public void visitInnerClassType(String name) {
				getDependentDescriptor()
						.addSuperClass(getClassDescriptor(name));
			}

		};
	}

	@Override
	public SignatureVisitor visitInterface() {
		return new DependentSignatureVisitor<ClassDescriptor>(getStore(),
				getDependentDescriptor()) {

			@Override
			public void visitClassType(String name) {
				getDependentDescriptor()
						.addImplements(getClassDescriptor(name));
			}

			@Override
			public void visitInnerClassType(String name) {
				getDependentDescriptor()
						.addImplements(getClassDescriptor(name));
			}
		};
	}

}
