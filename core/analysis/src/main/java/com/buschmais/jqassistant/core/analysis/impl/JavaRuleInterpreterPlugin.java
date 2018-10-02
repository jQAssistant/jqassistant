package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Collection;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AbstractRuleInterpreterPlugin;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.rule.impl.SourceExecutable;

import static java.util.Collections.singletonList;

public class JavaRuleInterpreterPlugin extends AbstractRuleInterpreterPlugin {

    @Override
    public Collection<String> getLanguages() {
        return singletonList("java");
    }

    @Override
    public <T extends ExecutableRule<?>> boolean accepts(T executableRule) {
        return executableRule.getExecutable() instanceof SourceExecutable && String.class.equals(executableRule.getExecutable().getType());
    }

    @Override
    public <T extends ExecutableRule<?>> Result<T> execute(T executableRule, Map<String, Object> ruleParameters, Severity severity, AnalyzerContext context) throws RuleException {
        SourceExecutable<String> executable = (SourceExecutable<String>) executableRule.getExecutable();
        String className = executable.getSource();
        return null;
    }

}
