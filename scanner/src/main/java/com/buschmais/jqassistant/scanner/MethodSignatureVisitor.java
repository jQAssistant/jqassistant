package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.signature.SignatureVisitor;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;

public class MethodSignatureVisitor extends AbstractSignatureVisitor {

	private final ClassDescriptor classDescriptor;

	protected MethodSignatureVisitor(Store store,
			ClassDescriptor classDescriptor) {
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
	public SignatureVisitor visitParameterType() {
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
	public SignatureVisitor visitReturnType() {
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
	public SignatureVisitor visitExceptionType() {
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
