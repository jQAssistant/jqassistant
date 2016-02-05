package com.buschmais.jqassistant.sonar.sonarrules.rule;

import org.sonar.check.RuleProperty;

public abstract class AbstractTemplateRule {

    @RuleProperty(key = "Cypher", description = "The cypher query representing this rule.")
    private String cypher;

    @RuleProperty(key = "Requires Concepts", description = "A list of concepts which are required to be executed prior to this rule.")
    private String requiresConcepts;

    @RuleProperty(key = "Aggregated Result", description = "If set the result will be interpreted as an aggregation.")
    private boolean aggregation;

    @RuleProperty(key = "Aggregation Column", description = "The column to use for verifying an aggregated result.")
    private String aggregationColumn;

    @RuleProperty(key = "Primary Report Column", description = "The primary column of the result, e.g. for creating issues.")
    private String primaryReportColumn;

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

    public String getAggregationColumn() {
        return aggregationColumn;
    }

    public void setAggregationColumn(String aggregationColumn) {
        this.aggregationColumn = aggregationColumn;
    }

    public String getPrimaryReportColumn() {
        return primaryReportColumn;
    }

    public void setPrimaryReportColumn(String primaryReportColumn) {
        this.primaryReportColumn = primaryReportColumn;
    }
}
