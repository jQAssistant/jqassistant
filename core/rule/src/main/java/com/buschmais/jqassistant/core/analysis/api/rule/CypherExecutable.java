package com.buschmais.jqassistant.core.analysis.api.rule;

/**
 * Defines a cypher statement which may be executed.
 */
public class CypherExecutable implements Executable {

    private String statement;

    public CypherExecutable(String statement) {
        this.statement = statement;
    }

    public String getStatement() {
        return statement;
    }
}
