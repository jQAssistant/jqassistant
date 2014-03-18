package com.buschmais.jqassistant.sonar.plugin.rule;

/**
 * Defines the parameters of a rule (i.e. concept, constraint).
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
