package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Collection;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.executor.RuleSetExecutor;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSelection;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * Implementation of the {@link Analyzer}.
 */
public class AnalyzerImpl implements Analyzer {

    private final Analyze configuration;

    private final AnalyzerContext analyzerContext;

    private final Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins;
    private final ReportPlugin reportPlugin;

    /**
     * Constructor.
     *
     * @param configuration
     *     The configuration.
     * @param classLoader
     *     The plugin {@link ClassLoader}
     * @param store
     *     The store
     * @param ruleInterpreterPlugins
     *     The {@link RuleInterpreterPlugin}s.
     * @param reportPlugin
     *     The report writer.
     */
    public AnalyzerImpl(Analyze configuration, ClassLoader classLoader, Store store, Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins,
        ReportPlugin reportPlugin) throws RuleException {
        this.configuration = configuration;
        this.analyzerContext = new AnalyzerContextImpl(configuration, classLoader, store);
        this.ruleInterpreterPlugins = ruleInterpreterPlugins;
        this.reportPlugin = reportPlugin;
    }

    @Override
    public Analyze getConfiguration() {
        return configuration;
    }

    @Override
    public void execute(RuleSet ruleSet, RuleSelection ruleSelection) throws RuleException {
        AnalyzerRuleVisitor visitor = new AnalyzerRuleVisitor(configuration, analyzerContext, ruleInterpreterPlugins, reportPlugin);
        RuleSetExecutor<Result.Status> executor = new RuleSetExecutor<>(visitor, configuration.rule());
        executor.execute(ruleSet, ruleSelection);
    }
}
