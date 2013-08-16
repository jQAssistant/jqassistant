package com.buschmais.jqassistant.core.model.api.rules;

import java.util.HashMap;
import java.util.Map;

/**
 * A CYPHER query including all parameters.
 */
public class Query {

    private String cypher;

    private Map<String, Object> parameters = new HashMap<String, Object>();

    public String getCypher() {
        return cypher;
    }

    public void setCypher(String cypher) {
        this.cypher = cypher;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "cypher='" + cypher + "', parameters=" + parameters + "]";
    }
}
