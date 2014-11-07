package com.buschmais.jqassistant.examples.rules.asciidoc.api;

/**
 * Defines a service.
 */
public interface Service {

    /**
     * Add integer values.
     * 
     * @param values
     *            The values to add.
     * @return The sum of all values.
     */
    int add(int... values);

}
