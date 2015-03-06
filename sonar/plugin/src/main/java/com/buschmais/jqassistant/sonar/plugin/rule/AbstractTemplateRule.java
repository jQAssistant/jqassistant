package com.buschmais.jqassistant.sonar.plugin.rule;

import org.sonar.check.RuleProperty;

public abstract class AbstractTemplateRule {

    @RuleProperty(key = "Cypher", description = "The cypher query representing this rule.")
    private String cypher;

    @RuleProperty(key = "Requires Concepts", description = "A list of concepts which are required to be executed prior to this rule.")
    private String requiresConcepts;

    @RuleProperty(key = "Aggregated Result", description = "If set the result will be interpreted as an aggregation.")
    private boolean aggregation;

    @RuleProperty(key = "Primary Column", description = "The primary column of the result.")
    private String primaryColumn;

    public void setCypher(String cypher) {
        this.cypher = cypher;
    }

    public void setRequiresConcepts(String requiresConcepts) {
        this.requiresConcepts = requiresConcepts;
    }

    public String getCypher() {
        return cypher;
    }

    public String getRequiresConcepts() {
        return requiresConcepts;
    }

    public boolean isAggregation() {
        return aggregation;
    }

    public void setAggregation(boolean aggregation) {
        this.aggregation = aggregation;
    }

    public String getPrimaryColumn() {
        return primaryColumn;
    }

    public void setPrimaryColumn(String primaryColumn) {
        this.primaryColumn = primaryColumn;
    }
}
