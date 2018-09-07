package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.rule.impl.SourceExecutable;

/**
 * Defines a cypher statement which may be executed.
 */
public class CypherExecutable extends SourceExecutable<String> {

    private static final String LANGUAGE = "cypher";

    public CypherExecutable(String statement) {
        super(LANGUAGE, statement);
    }

}
