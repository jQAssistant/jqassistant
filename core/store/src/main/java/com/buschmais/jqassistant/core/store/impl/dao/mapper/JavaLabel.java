package com.buschmais.jqassistant.core.store.impl.dao.mapper;

import com.buschmais.jqassistant.core.store.api.model.IndexedLabel;

/**
 * The node labels created by the scanner.
 */
public enum JavaLabel implements IndexedLabel {
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
    JavaLabel() {
        this(false);
    }

    /**
     * Parametrized constructor.
     *
     * @param indexed <code>true</code> if nodes with this label shall be indexed for faster lookup.
     */
    JavaLabel(boolean indexed) {
        this.indexed = indexed;
    }

    public boolean isIndexed() {
        return indexed;
    }
}
