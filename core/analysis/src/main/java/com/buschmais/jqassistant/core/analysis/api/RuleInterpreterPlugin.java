package com.buschmais.jqassistant.core.analysis.api;

import java.util.Collection;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Rule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.shared.lifecycle.ConfigurableLifecycleAware;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Plugin interface for rule interpreters.
 */
public interface RuleInterpreterPlugin extends ConfigurableLifecycleAware<Map<String, Object>> {

    /**
     * Initialize the plugin.
     *
     * Life cycle callback for a plugin to do static initialization. Will be exactly
     * invoked once after the plugin has been instantiated.
     */
    @Override
    default void initialize() {
    }

    /**
     * Configure the plugin.
     *
     * This method is always called at least once after {@link #initialize()} and
     * allows re-configuring a plugin instance at runtime (e.g. in a Maven
     * multi-module build process).
     *
     * @param properties
     *            The plugin properties.
     */
    @Override
    default void configure(Map<String, Object> properties) {
    }

    @Override
    default void destroy() {
    }

    /**
     * Return the languages supported by this plugin.
     *
     * @return The languag
     */
    Collection<String> getLanguages();

    /**
     * Determines if the plugin is able to execute the given {@link ExecutableRule}.
     *
     * @param executableRule
     *            The {@link ExecutableRule}.
     * @param <T>
     *            The {@link ExecutableRule} type.
     * @return <code>true</code> if the plugin is able to execute the given
     *         {@link ExecutableRule}.
     */
    <T extends ExecutableRule<?>> boolean accepts(T executableRule);

    /**
     * Execute a {@link ExecutableRule}.
     *
     * @param executableRule
     *            The {@link ExecutableRule}.
     * @param ruleParameters
     *            The rule parameters.
     * @param severity
     *            The effective {@link Severity}.
     * @param context
     *            The {@link AnalyzerContext} providing access to the {@link Store}.
     * @param <T>
     *            The {@link ExecutableRule} type.
     * @return The {@link Result}.
     * @throws RuleException
     *             If execution fails due to an invalid {@link Rule}.
     */
    <T extends ExecutableRule<?>> Result<T> execute(T executableRule, Map<String, Object> ruleParameters, Severity severity, AnalyzerContext context)
            throws RuleException;

}
