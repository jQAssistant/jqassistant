package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.Type;

public abstract class AbstractVisitor {

	protected String getInternalName(final String name) {
		if (name != null) {
			return getType(Type.getObjectType(name));
		}
		return null;
	}

	// common

	public void visitEnd() {
	}

	protected String getType(final Type t) {
		switch (t.getSort()) {
		case Type.ARRAY:
			return getType(t.getElementType());
		case Type.OBJECT:
			return t.getInternalName();
		default:
			return null;
		}
	}
}
