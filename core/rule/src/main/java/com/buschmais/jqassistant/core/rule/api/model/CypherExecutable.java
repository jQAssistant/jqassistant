package com.buschmais.jqassistant.core.rule.api.model;

import com.buschmais.jqassistant.core.rule.impl.SourceExecutable;

/**
 * Defines a cypher statement which may be executed.
 */
public class CypherExecutable extends SourceExecutable<String> {

    private static final String LANGUAGE = "cypher";

    public CypherExecutable(String statement, boolean transactional) {
        super(LANGUAGE, statement, String.class, transactional);
    }

}
