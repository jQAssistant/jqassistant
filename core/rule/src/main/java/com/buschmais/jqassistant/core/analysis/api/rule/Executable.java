package com.buschmais.jqassistant.core.analysis.api.rule;

/**
 * Defines an executable, e.g. a cypher query, script or template.
 */
public interface Executable {

    /**
     * Return the language.
     *
     * @return The language.
     */
    String getLanguage();

    /**
     * Return the executable source.
     *
     * @return The source.
     */
    String getSource();
}
