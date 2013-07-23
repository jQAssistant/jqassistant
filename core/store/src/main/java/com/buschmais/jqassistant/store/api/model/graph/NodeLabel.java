package com.buschmais.jqassistant.store.api.model.graph;

import org.neo4j.graphdb.Label;

/**
 * The node labels created by the scanner.
 */
public enum NodeLabel implements org.neo4j.graphdb.Label {
    /**
     * Package
     */
    PACKAGE,
    /**
     * Class
     */
    CLASS,
    /**
     * Method
     */
    METHOD,
    /**
     * Field
     */
    FIELD;
}
