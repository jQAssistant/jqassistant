package com.buschmais.jqassistant.store.api.model;

import com.buschmais.jqassistant.store.impl.model.NodeType;

public interface Descriptor {

	String FULLQUALIFIEDNAME = "fullQualifiedName";

	String TYPE = "type";

	String getFullQualifiedName();

	NodeType getType();

}
