package com.buschmais.jqassistant.core.store.api.model;

import org.neo4j.graphdb.Label;

/**
 * Defines the interface for labels indicating if they are indexed or not.
 */
public interface IndexedLabel extends Label {

    String getIndexedProperty();
}
