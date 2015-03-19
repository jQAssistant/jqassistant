package com.buschmais.jqassistant.core.analysis.api.rule;

/**
 * Report definition for a rule.
 */
public class Report {

    private String primaryColumn;

    public Report(String primaryColumn) {
        this.primaryColumn = primaryColumn;
    }

    public String getPrimaryColumn() {
        return primaryColumn;
    }
}
