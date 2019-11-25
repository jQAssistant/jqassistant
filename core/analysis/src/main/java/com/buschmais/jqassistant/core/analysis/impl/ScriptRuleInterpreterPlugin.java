package com.buschmais.jqassistant.core.analysis.impl;

import java.util.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.rule.api.model.Executable;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.impl.SourceExecutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptRuleInterpreterPlugin implements RuleInterpreterPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptRuleInterpreterPlugin.class);

    /**
     * Defines the available variables for scripts.
     */
    private enum ScriptVariable {

        STORE, CONTEXT, RULE, SEVERITY;

        String getVariableName() {
            return name().toLowerCase();
        }
    }

    private ScriptEngineManager scriptEngineManager;
    private Set<String> languages = new TreeSet<>();

    public ScriptRuleInterpreterPlugin() {
        this.scriptEngineManager = new ScriptEngineManager();
        for (ScriptEngineFactory factory : scriptEngineManager.getEngineFactories()) {
            for (String name : factory.getNames()) {
                languages.add(name.toLowerCase());
            }
        }
        LOGGER.debug("Supported languages: {}.", languages);
    }

    @Override
    public Collection<String> getLanguages() {
        return languages;
    }

    @Override
    public <T extends ExecutableRule<?>> boolean accepts(T executableRule) {
        return executableRule.getExecutable() instanceof SourceExecutable;
    }

    @Override
    public <T extends ExecutableRule<?>> Result<T> execute(T executableRule, Map<String, Object> ruleParameters, Severity severity, AnalyzerContext context)
            throws RuleException {
        Executable<String> executable = executableRule.getExecutable();
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
        scriptEngine.put(ScriptVariable.CONTEXT.getVariableName(), context);
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
