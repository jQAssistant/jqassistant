package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ValueDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Represents an annotation value.
 */
public interface AnnotationValueDescriptor extends TypedDescriptor, ValueDescriptor<List<ValueDescriptor<?>>>, AnnotationDescriptor, FullQualifiedNameDescriptor {

    @Relation("HAS")
    @Override
    List<ValueDescriptor<?>> getValue();

    @Override
    void setValue(List<ValueDescriptor<?>> value);
}
