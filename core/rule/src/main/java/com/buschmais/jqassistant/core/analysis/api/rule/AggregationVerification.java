package com.buschmais.jqassistant.core.analysis.api.rule;

/**
 * Indicates that a result shall be verified by evaluating aggregated values
 * from a result column.
 */
public class AggregationVerification implements Verification {

    /**
     * Specifies the column containing the aggregation value to verify.
     */
    private String column;

    public AggregationVerification(String primaryColumn) {
        this.column = primaryColumn;
    }

    public String getColumn() {
        return column;
    }
}
