package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;

import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.report.api.model.VerificationResult;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Verification;

/**
 * Defines the interface for strategies to verify the result of an executable
 * rule.
 */
public interface VerificationStrategy<V extends Verification> {

    Class<V> getVerificationType();

    <T extends ExecutableRule> VerificationResult verify(T executable, V verification, List<String> columnNames, List<Row> rows) throws RuleException;
}
