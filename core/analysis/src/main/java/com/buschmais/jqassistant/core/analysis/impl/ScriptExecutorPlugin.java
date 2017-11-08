package com.buschmais.jqassistant.core.analysis.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.RuleExecutorPlugin;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.ScriptExecutable;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

public class ScriptExecutorPlugin implements RuleExecutorPlugin<ScriptExecutable> {

    /**
     * Defines the available variables for scripts.
     */
    private enum ScriptVariable {

        STORE, RULE, SEVERITY;

        String getVariableName() {
            return name().toLowerCase();
        }
    }

    private ScriptEngineManager scriptEngineManager;

    public ScriptExecutorPlugin() {
        this.scriptEngineManager = new ScriptEngineManager();
    }

    @Override
    public Class<ScriptExecutable> getType() {
        return ScriptExecutable.class;
    }

    @Override
    public <T extends ExecutableRule<ScriptExecutable>> Result<T> execute(T executableRule, Map<String, Object> ruleParameters, Severity severity,
            AnalyzerContext context) throws RuleException {
        ScriptExecutable executable = executableRule.getExecutable();
        String language = executable.getLanguage();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(language);
        if (scriptEngine == null) {
            List<String> availableLanguages = new ArrayList<>();
            for (ScriptEngineFactory factory : scriptEngineManager.getEngineFactories()) {
                availableLanguages.addAll(factory.getNames());
            }
            throw new RuleException("Cannot resolve scripting engine for '" + language + "', available languages are " + availableLanguages);
        }
        // Set default variables
        scriptEngine.put(ScriptVariable.STORE.getVariableName(), context.getStore());
        scriptEngine.put(ScriptVariable.RULE.getVariableName(), executableRule);
        scriptEngine.put(ScriptVariable.SEVERITY.getVariableName(), severity);
        // Set rule parameters
        for (Map.Entry<String, Object> entry : ruleParameters.entrySet()) {
            scriptEngine.put(entry.getKey(), entry.getValue());
        }
        Object scriptResult;
        try {
            scriptResult = scriptEngine.eval(executable.getSource());
        } catch (ScriptException e) {
            throw new RuleException("Cannot execute script.", e);
        }
        if (!(scriptResult instanceof Result)) {
            throw new RuleException("Script returned an invalid result type, expected " + Result.class.getName() + " but got " + scriptResult);
        }
        return Result.class.cast(scriptResult);
    }

}
