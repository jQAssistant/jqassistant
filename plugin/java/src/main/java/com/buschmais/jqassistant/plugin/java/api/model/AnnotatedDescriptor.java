package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Interface describing an {@link Descriptor} which is annotated by
 * {@link AnnotationValueDescriptor}s.
 */
public interface AnnotatedDescriptor extends Descriptor {

    /**
     * Return the annotations this descriptor is annotated by.
     * 
     * @return The annotations this descriptor is annotated by.
     */
    @Relation("ANNOTATED_BY")
    List<AnnotationValueDescriptor> getAnnotatedBy();
}
