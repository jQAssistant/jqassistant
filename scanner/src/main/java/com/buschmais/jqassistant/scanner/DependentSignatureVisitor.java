package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.signature.SignatureVisitor;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.DependentDescriptor;

public final class DependentSignatureVisitor<T extends DependentDescriptor>
		extends AbstractSignatureVisitor {

	private final DependentDescriptor dependentDescriptor;

	public DependentSignatureVisitor(Store store, T dependentDescriptor) {
		super(store);
		this.dependentDescriptor = dependentDescriptor;
	}

	@Override
	public SignatureVisitor visitClassBound() {
		return getTypeVisitor();
	}

	@Override
	public SignatureVisitor visitInterfaceBound() {
		return getTypeVisitor();
	}

	@Override
	public SignatureVisitor visitParameterType() {
		return getTypeVisitor();
	}

	@Override
	public SignatureVisitor visitReturnType() {
		return getTypeVisitor();
	}

	@Override
	public SignatureVisitor visitExceptionType() {
		return getTypeVisitor();
	}

	@Override
	public SignatureVisitor visitTypeArgument(char wildcard) {
		return getTypeVisitor();
	}

	@Override
	public SignatureVisitor visitArrayType() {
		return getTypeVisitor();
	}

	private AbstractSignatureVisitor getTypeVisitor() {
		return new AbstractSignatureVisitor(getStore()) {

			@Override
			public void visitClassType(String name) {
				dependentDescriptor.addDependency(getClassDescriptor(name));
			}

			@Override
			public void visitInnerClassType(String name) {
				dependentDescriptor.addDependency(getClassDescriptor(name));
			}

		};
	}
}
