package com.buschmais.jqassistant.store.api.model;

/**
 * The node labels created by the scanner.
 */
public enum NodeLabel implements org.neo4j.graphdb.Label {
    /**
     * Artifact.
     */
    ARTIFACT(true),
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


    /**
     * Constructor   .
     *
     * @param indexed <code>true</code> if a schema index shall be used for this label.
     */
    private NodeLabel(boolean indexed) {
        this.indexed = indexed;
    }

    /**
     * Return <code>true</code> if a schema index shall be used for this label.
     *
     * @return <code>true</code> if a schema index shall be used for this label.
     */
    public boolean isIndexed() {
        return indexed;
    }
}
