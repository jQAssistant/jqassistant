package com.buschmais.jqassistant.core.plugin.impl;

public class ClassNameFormatter {

	/**
	 * Trim class names read from xml jqassistant-plugin.xml.
	 * 
	 * @param value
	 * @return
	 */
	public static String parseClassName(String value) {
		return value.trim();
	}
}
