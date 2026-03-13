package com.buschmais.jqassistant.core.rule.api.reader;

import com.buschmais.jqassistant.core.rule.api.model.Verification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import static lombok.AccessLevel.PRIVATE;

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
