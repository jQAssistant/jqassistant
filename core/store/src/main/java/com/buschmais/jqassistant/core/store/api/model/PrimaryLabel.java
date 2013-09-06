package com.buschmais.jqassistant.core.store.api.model;

import org.neo4j.graphdb.Label;

/**
 * Defines the interface for primary labels which are used to determine a node type.
 */
public interface PrimaryLabel extends Label {

    boolean isIndexed();
}
