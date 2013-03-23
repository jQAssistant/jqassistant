package com.buschmais.jqassistant.store.api.model;

public interface Descriptor {

	String FULLQUALIFIEDNAME = "fullQualifiedName";

	String getFullQualifiedName();

	void setFullQualifiedName(String fullQualifiedName);

	String getLocalName();

	void setLocalName(String localName);
}
