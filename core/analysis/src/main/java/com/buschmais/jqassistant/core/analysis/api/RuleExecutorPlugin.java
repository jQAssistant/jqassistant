package com.buschmais.jqassistant.core.analysis.api;

import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.rule.Executable;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

/**
 * Plugin interface for rule executor, i.e. providing support for different
 * {@link Executable}s.
 *
 * @param <E>
 *            The {@link Executable} type.
 */
public interface RuleExecutorPlugin<E extends Executable> {

    Class<E> getType();

    <T extends ExecutableRule<E>> Result<T> execute(T executableRule, Map<String, Object> ruleParameters, Severity severity, AnalyzerContext context)
            throws RuleException;

}
