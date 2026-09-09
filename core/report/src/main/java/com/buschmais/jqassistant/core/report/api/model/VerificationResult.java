package com.buschmais.jqassistant.core.report.api.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * The result of the verification.
 * <p>
 * The outcome is used to calculate the status based on the configured severity thresholds.
 */
@Builder
@Getter
@ToString
public class VerificationResult {

    /**
     * <code>true</code> if the rule execution returned successfully.
     */
    private boolean success;

    /**
     * The count of non-hidden rows returned by the rule, in case of aggregation the sum of the values of the aggregation column.
     */
    private int rowCount;

    /**
     * The count of hidden rows returned by the rule, in case of aggregation the sum of the values of the aggregation column.
     */
    private int hiddenRowCount;

}
