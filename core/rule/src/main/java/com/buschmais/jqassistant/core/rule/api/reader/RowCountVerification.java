package com.buschmais.jqassistant.core.rule.api.reader;

import static lombok.AccessLevel.PRIVATE;

import com.buschmais.jqassistant.core.analysis.api.rule.Verification;

import lombok.*;

/**
 * Indicates that a result shall be verified by the count of returned rows.
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor(access = PRIVATE)
@AllArgsConstructor(access = PRIVATE)
public class RowCountVerification implements Verification {

    private Integer max;

    private Integer min;

}
