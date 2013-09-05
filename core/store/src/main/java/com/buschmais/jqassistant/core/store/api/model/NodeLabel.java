package com.buschmais.jqassistant.core.store.api.model;

/**
 * The node labels created by the scanner.
 */
public enum NodeLabel implements PrimaryLabel {
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
    METHOD(true),
    /**
     * Parameter
     */
    PARAMETER(true),
    /**
     * Constructor
     */
    CONSTRUCTOR,
    /**
     * Field
     */
    FIELD(true),
    /**
     * value
     */
    VALUE;

    private boolean indexed;

    /**
     * Default constructor.
     */
    NodeLabel() {
        this(false);
    }

    /**
     * Parametrized constructor.
     *
     * @param indexed <code>true</code> if nodes with this label shall be indexed for faster lookup.
     */
    NodeLabel(boolean indexed) {
        this.indexed = indexed;
    }

    public boolean isIndexed() {
        return indexed;
    }
}
