package com.buschmais.jqassistant.plugin.common.api.rule;

import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Severity;

public interface JavaRule {

    <T extends ExecutableRule<?>> Result<T> execute(T executableRule, Map<String, Object> configuration, Map<String, Object> ruleParameters, Severity severity, AnalyzerContext context);

}
