package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.signature.SignatureVisitor;

import com.buschmais.jqassistant.store.api.Store;

public abstract class AbstractSignatureVisitor extends AbstractVisitor
		implements SignatureVisitor {

	protected AbstractSignatureVisitor(Store store) {
		super(store);
	}

	@Override
	public void visitFormalTypeParameter(String name) {
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
	public void visitBaseType(char descriptor) {
	}

	@Override
	public void visitTypeVariable(String name) {
	}

	@Override
	public SignatureVisitor visitArrayType() {
		return this;
	}

	@Override
	public void visitClassType(String name) {
	}

	@Override
	public void visitInnerClassType(String name) {
	}

	@Override
	public void visitTypeArgument() {
	}

	@Override
	public SignatureVisitor visitTypeArgument(char wildcard) {
		return this;
	}

	@Override
	public void visitEnd() {
	}

}
