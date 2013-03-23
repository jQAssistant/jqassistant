package com.buschmais.jqassistant.scanner;

import com.buschmais.jqassistant.store.api.Store;

public class SignatureVisitor extends AbstractVisitor implements
		org.objectweb.asm.signature.SignatureVisitor {

	private String signatureClassName;

	protected SignatureVisitor(Store store) {
		super(store);
	}

	@Override
	public void visitFormalTypeParameter(final String name) {
	}

	@Override
	public SignatureVisitor visitClassBound() {
		return this;
	}

	@Override
	public SignatureVisitor visitInterfaceBound() {
		return this;
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		return this;
	}

	@Override
	public SignatureVisitor visitInterface() {
		return this;
	}

	@Override
	public SignatureVisitor visitParameterType() {
		return this;
	}

	@Override
	public SignatureVisitor visitReturnType() {
		return this;
	}

	@Override
	public SignatureVisitor visitExceptionType() {
		return this;
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