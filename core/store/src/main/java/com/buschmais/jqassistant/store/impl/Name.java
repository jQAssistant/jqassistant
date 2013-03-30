package com.buschmais.jqassistant.store.impl;

import com.buschmais.jqassistant.store.api.model.Descriptor;

public class Name {

	private final String name;

	private final String fullQualifiedName;

	public Name(Descriptor parent, char separator, String name) {
		this.name = name;
		if (parent != null) {
			this.fullQualifiedName = parent.getFullQualifiedName() + separator
					+ name;
		} else {
			this.fullQualifiedName = name;
		}
	}

	public String getName() {
		return name;
	}

	public String getFullQualifiedName() {
		return fullQualifiedName;
	}

}
