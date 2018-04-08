package com.buschmais.jqassistant.core.analysis.api.rule;

/**
 * Defines an executable, e.g. a cypher query, script or template.
 *
 * @param <S>
 *            The rule source type, e.g. an Asciidoc block or XML type
 */
public interface Executable<S> {

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
    S getSource();

}
