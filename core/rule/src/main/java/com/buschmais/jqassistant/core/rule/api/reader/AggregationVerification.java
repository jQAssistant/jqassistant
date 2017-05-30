package com.buschmais.jqassistant.core.rule.api.reader;

import static lombok.AccessLevel.PRIVATE;

import com.buschmais.jqassistant.core.analysis.api.rule.Verification;

import lombok.*;

/**
 * Indicates that a result shall be verified by evaluating aggregated values
 * from a result column.
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class AggregationVerification implements Verification {

    /**
     * Specifies the column containing the aggregation value to verify.
     */
    private String column;

    private Integer min;

    private Integer max;

}
