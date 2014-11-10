package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
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
    List<TypeDescriptor> getDependencies();

}
