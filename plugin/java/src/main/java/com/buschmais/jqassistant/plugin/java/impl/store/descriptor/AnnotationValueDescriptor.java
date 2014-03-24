package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

/**
 * Represents an annotation value.
 */
public interface AnnotationValueDescriptor extends TypedDescriptor, ValueDescriptor<List<ValueDescriptor>>, AnnotationDescriptor {

	@Relation("HAS")
	@Override
	List<ValueDescriptor> getValue();

	@Override
	void setValue(List<ValueDescriptor> value);
}
