package com.buschmais.jqassistant.plugin.jpa2.impl.store.mapper;

import com.buschmais.jqassistant.core.store.api.model.IndexedLabel;

/**
 * Defines the JPA labels.
 */
public enum Jpa2Label implements IndexedLabel {

	JPA, PERSISTENCE, PERSISTENCEUNIT;

	public String getIndexedProperty() {
		return null;
	}
}
