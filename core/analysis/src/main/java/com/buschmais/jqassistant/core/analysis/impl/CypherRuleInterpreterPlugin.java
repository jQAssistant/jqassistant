package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Collection;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.rule.api.model.Executable;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.impl.SourceExecutable;

import static java.util.Collections.singletonList;

/**
 * Implementation of a {@link RuleInterpreterPlugin} for executing Cypher rules.
 */
public class CypherRuleInterpreterPlugin extends AbstractCypherRuleInterpreterPlugin {

    private static final Collection<String> LANGUAGES = singletonList("cypher");

    @Override
    public Collection<String> getLanguages() {
        return LANGUAGES;
    }

    @Override
    public <T extends ExecutableRule<?>> boolean accepts(T executableRule) {
        return executableRule.getExecutable() instanceof SourceExecutable;
    }

    @Override
    public <T extends ExecutableRule<?>> Result<T> execute(T executableRule, Map<String, Object> parameters, Severity severity, AnalyzerContext context)
            throws RuleException {
        Executable<String> executable = executableRule.getExecutable();
        String cypher = executable.getSource();
        return execute(cypher, executableRule, parameters, severity, context);
    }

}
