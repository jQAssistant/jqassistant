package com.buschmais.jqassistant.store.impl;

public class Name {

	private final String parentName;

	private final String localName;

	private final String fullQualifiedName;

	public Name(String parentName, String localName, String fullQualifiedName) {
		super();
		this.parentName = parentName;
		this.localName = localName;
		this.fullQualifiedName = fullQualifiedName;
	}

	public String getParentName() {
		return parentName;
	}

	public String getLocalName() {
		return localName;
	}

	public String getFullQualifiedName() {
		return fullQualifiedName;
	}

}
