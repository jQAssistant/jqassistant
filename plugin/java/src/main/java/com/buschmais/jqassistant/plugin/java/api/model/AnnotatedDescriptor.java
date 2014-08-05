package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.xo.api.annotation.ResultOf.Parameter;

import java.util.Set;

import com.buschmais.jqassistant.core.store.api.type.Descriptor;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
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
    Set<AnnotationValueDescriptor> getAnnotatedBy();

    @ResultOf
    @Cypher("match (a),(v) where id(a)={this} and id(v)={value} create unique (a)-[:ANNOTATED_BY]->(v)")
    void addAnnotatedBy(@Parameter("value") AnnotationValueDescriptor value);
}
