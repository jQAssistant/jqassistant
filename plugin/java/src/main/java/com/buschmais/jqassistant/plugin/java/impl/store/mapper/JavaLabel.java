package com.buschmais.jqassistant.plugin.java.impl.store.mapper;

import com.buschmais.jqassistant.core.store.api.model.IndexedLabel;

/**
 * The node labels created by the scanner.
 */
public enum JavaLabel implements IndexedLabel {
	/**
	 * Artifact.
	 */
	ARTIFACT("FQN"),
	/**
	 * Package
	 */
	PACKAGE("FQN"),
	/**
	 * Type
	 */
	TYPE("FQN"),
	/**
	 * Method
	 */
	METHOD("FQN"),
	/**
	 * Parameter
	 */
	PARAMETER("FQN"),

	/**
	 * Constructor
	 */
	CONSTRUCTOR,
	/**
	 * Field
	 */
	FIELD("FQN"),
	/**
	 * value
	 */
	VALUE,
	/**
	 * Properties
	 */
	PROPERTYFILE;

	private String indexedProperty;

	JavaLabel() {
		this(null);
	}

	/**
	 * Parametrized constructor.
	 * 
	 * @param indexedProperty
	 *            The name of the property to be used for indexing.
	 */
	JavaLabel(String indexedProperty) {
		this.indexedProperty = indexedProperty;
	}

	public String getIndexedProperty() {
		return indexedProperty;
	}
}
