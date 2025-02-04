package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;

import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Verification;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Defines the interface for strategies to verify the result of an executable
 * rule.
 */
public interface VerificationStrategy<V extends Verification> {

    @Builder
    @Getter
    @ToString
    class Result {

        private boolean successful;

        private int rowCount;

    }

    Class<V> getVerificationType();

    <T extends ExecutableRule> VerificationStrategy.Result verify(T executable, V verification, List<String> columnNames, List<Row> rows) throws RuleException;
}
