package com.buschmais.jqassistant.core.model.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: dimahler
 * Date: 6/21/13
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
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

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
