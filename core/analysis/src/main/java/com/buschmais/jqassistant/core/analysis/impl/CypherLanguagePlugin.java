package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Collection;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Executable;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

import static java.util.Collections.singletonList;

/**
 * Implementation of a
 * {@link com.buschmais.jqassistant.core.analysis.api.RuleLanguagePlugin} for
 * executing Cypher rules.
 */
public class CypherLanguagePlugin extends AbstractCypherLanguagePlugin {

    private static final Collection<String> LANGUAGES = singletonList("cypher");

    @Override
    public Collection<String> getLanguages() {
        return LANGUAGES;
    }

    @Override
    public <T extends ExecutableRule<?>> boolean accepts(T executableRule) {
        return true;
    }

    @Override
    public <T extends ExecutableRule<?>> Result<T> execute(T executableRule, Map<String, Object> parameters, Severity severity, AnalyzerContext context)
            throws RuleException {
        Executable<String> executable = executableRule.getExecutable();
        String cypher = executable.getSource();
        return execute(cypher, executableRule, parameters, severity, context);
    }

}
