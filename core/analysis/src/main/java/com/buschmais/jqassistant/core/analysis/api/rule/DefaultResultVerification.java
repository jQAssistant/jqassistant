package com.buschmais.jqassistant.core.analysis.api.rule;

/**
 * The default verification of the result of an executable rule.
 */
public class DefaultResultVerification implements ResultVerification {

    private boolean aggregation;

    private String primaryColumn;

    public DefaultResultVerification(boolean aggregation, String primaryColumn) {
        this.aggregation = aggregation;
        this.primaryColumn = primaryColumn;
    }

    /**
     * Indicates if an aggregation (e.g. count) is returned by the rule.
     * 
     * @return <code>boolean</code> If an aggregated result is expected.
     */
    public boolean isAggregation() {
        return aggregation;
    }

    /**
     * Return the name of the column which shall be treated as primary, i.e. the
     * element to be used for rendering a violation or to verify for an
     * aggregated value.
     * 
     * @return The name of the primary column.
     */
    public String getPrimaryColumn() {
        return primaryColumn;
    }
}
