package com.buschmais.jqassistant.plugin.java.test.set.dependency.annotations;

/**
 * A type containing annotations on class, field, method and parameter level
 */
@Annotation(value = "type", classValues = { Number.class })
public class AnnotatedType {

	@Annotation(value = "type", classValues = { Number.class })
	int value;

	@Annotation(value = "type", classValues = { Number.class })
	void doSomething(@Annotation(value = "type", classValues = { Number.class }) int value) {
	}
}
