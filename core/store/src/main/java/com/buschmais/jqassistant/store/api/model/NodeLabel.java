package com.buschmais.jqassistant.store.api.model;

/**
 * The node labels created by the scanner.
 */
public enum NodeLabel implements org.neo4j.graphdb.Label {
    /**
     * Artifact.
     */
    ARTIFACT(false),
    /**
     * Package
     */
    PACKAGE(true),
    /**
     * Type
     */
    TYPE(true),
    /**
     * Method
     */
    METHOD(false),
    /**
     * Field
     */
    FIELD(false);

    private boolean indexed;

    private NodeLabel(boolean indexed) {
        this.indexed = indexed;
    }

    public boolean isIndexed() {
        return indexed;
    }
}
