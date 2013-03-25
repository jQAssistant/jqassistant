package com.buschmais.jqassistant.scanner;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.Descriptor;

public class SignatureVisitor extends AbstractVisitor implements
		org.objectweb.asm.signature.SignatureVisitor {

	private String signatureClassName;

	private final Descriptor descriptor;

	protected SignatureVisitor(Store store, Descriptor descriptor) {
		super(store);
		this.descriptor = descriptor;
	}

	@Override
	public void visitFormalTypeParameter(final String name) {
	}

	@Override
	public SignatureVisitor visitClassBound() {
		return new SignatureVisitor(getStore(), descriptor);
	}

	@Override
	public SignatureVisitor visitInterfaceBound() {
		return new SignatureVisitor(getStore(), descriptor);
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		return new SignatureVisitor(getStore(), descriptor);
	}

	@Override
	public SignatureVisitor visitInterface() {
		return new SignatureVisitor(getStore(), descriptor);
	}

	@Override
	public SignatureVisitor visitParameterType() {
		return new SignatureVisitor(getStore(), descriptor);
	}

	@Override
	public SignatureVisitor visitReturnType() {
		return new SignatureVisitor(getStore(), descriptor);
	}

	@Override
	public SignatureVisitor visitExceptionType() {
		return new SignatureVisitor(getStore(), descriptor);
	}

	@Override
	public void visitBaseType(final char descriptor) {
	}

	@Override
	public void visitTypeVariable(final String name) {
	}

	@Override
	public SignatureVisitor visitArrayType() {
		return this;
	}

	@Override
	public void visitClassType(final String name) {
		signatureClassName = name;
	}

	@Override
	public void visitInnerClassType(final String name) {
		signatureClassName = signatureClassName + "$" + name;
	}

	@Override
	public void visitTypeArgument() {
	}

	@Override
	public SignatureVisitor visitTypeArgument(final char wildcard) {
		return this;
	}

	public String getSignatureClassName() {
		return signatureClassName;
	}

	@Override
	public void visitEnd() {
	}
}