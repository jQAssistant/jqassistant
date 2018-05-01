package com.buschmais.jqassistant.core.analysis.impl;

import java.util.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.RuleLanguagePlugin;
import com.buschmais.jqassistant.core.analysis.api.rule.Executable;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptLanguagePlugin implements RuleLanguagePlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptLanguagePlugin.class);

    /**
     * Defines the available variables for scripts.
     */
    private enum ScriptVariable {

        @Deprecated STORE, CONTEXT, RULE, SEVERITY;

        String getVariableName() {
            return name().toLowerCase();
        }
    }

    private ScriptEngineManager scriptEngineManager;
    private Set<String> languages = new TreeSet<>();

    public ScriptLanguagePlugin() {
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
        return true;
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
