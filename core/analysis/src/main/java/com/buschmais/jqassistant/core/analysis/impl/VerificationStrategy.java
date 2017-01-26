package com.buschmais.jqassistant.core.analysis.impl;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Verification;
import com.buschmais.jqassistant.core.rule.api.executor.RuleExecutorException;

/**
 * Defines the interface for strategies to verify the result of an executable
 * rule.
 */
public interface VerificationStrategy<V extends Verification> {

    Class<V> getVerificationType();

    <T extends ExecutableRule> Result.Status verify(T executable, V verification, List<String> columnNames, List<Map<String, Object>> rows)
            throws RuleExecutorException;

}
