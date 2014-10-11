package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.xo.api.annotation.ResultOf.Parameter;

import java.util.Set;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Interface describing a {@link Descriptor} which depends on other
 * {@link TypeDescriptor}s.
 */
public interface DependentDescriptor extends Descriptor {

    /**
     * Return the classes this descriptor depends on.
     * 
     * @return The classes this descriptor depends on.
     */
    @Relation("DEPENDS_ON")
    Set<TypeDescriptor> getDependencies();

    @ResultOf
    @Cypher("match (d),(t) where id(d)={this} and id(t)={dependency} create unique (d)-[:DEPENDS_ON]->(t)")
    void addDependency(@Parameter("dependency") TypeDescriptor dependency);

}
