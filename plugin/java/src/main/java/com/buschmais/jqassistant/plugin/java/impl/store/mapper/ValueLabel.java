package com.buschmais.jqassistant.plugin.java.impl.store.mapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Denotes labels for {@link JavaLabel#VALUE} nodes.
 */
public enum ValueLabel {

	/**
	 * Primitive value.
	 */
	PRIMITIVE,
	/**
	 * Enumeration value.
	 */
	ENUM,
	/**
	 * Annotation value.
	 */
	ANNOTATION,
	/**
	 * Type value.
	 */
	CLASS,
	/**
	 * Array value.
	 */
	ARRAY,
	/**
	 * Property value.
	 */
	PROPERTY;

	private static Map<String, ValueLabel> labels;

	static {
		labels = new HashMap<>();
		for (ValueLabel valueLabel : ValueLabel.values()) {
			labels.put(valueLabel.name(), valueLabel);
		}
	}

	public static ValueLabel getValueLabel(String name) {
		return labels.get(name);
	}
}
