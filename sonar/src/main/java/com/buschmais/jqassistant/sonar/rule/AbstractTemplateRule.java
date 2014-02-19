package com.buschmais.jqassistant.sonar.rule;

import org.sonar.check.RuleProperty;

import java.util.List;

public abstract class AbstractTemplateRule {

    @RuleProperty(key = "Cypher", description = "The cypher query representing this rule.")
    private String cypher;

    @RuleProperty(key = "Requires Concepts", description = "A list of concepts which are required to be executed prior to this rule.")
    private List<String> requiresConcepts;

    public void setCypher(String cypher) {
        this.cypher = cypher;
    }

    public void setRequiresConcepts(List<String> requiresConcepts) {
        this.requiresConcepts = requiresConcepts;
    }
}
