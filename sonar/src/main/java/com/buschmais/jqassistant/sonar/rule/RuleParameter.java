package com.buschmais.jqassistant.sonar.rule;

/**
 * Created by dimahler on 2/25/14.
 */
public enum RuleParameter {

    Type("Type"),
    RequiresConcepts("Requires Concepts"),
    Cypher("Cypher");

    private String name;

    RuleParameter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
