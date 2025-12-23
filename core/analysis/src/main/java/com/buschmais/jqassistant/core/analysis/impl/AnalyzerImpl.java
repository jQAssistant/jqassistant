package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Collection;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.analysis.api.baseline.BaselineManager;
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

    private final ClassLoader classLoader;

    private final Store store;

    private final Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins;

    private final BaselineManager baselineManager;

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
     * @param baselineManager
     *     The {@link BaselineManager}.
     * @param reportPlugin
     *     The report writer.
     */
    public AnalyzerImpl(Analyze configuration, ClassLoader classLoader, Store store, Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins,
        BaselineManager baselineManager, ReportPlugin reportPlugin) {
        this.configuration = configuration;
        this.classLoader = classLoader;
        this.store = store;
        this.ruleInterpreterPlugins = ruleInterpreterPlugins;
        this.baselineManager = baselineManager;
        this.reportPlugin = reportPlugin;
    }

    @Override
    public void execute(RuleSet ruleSet, RuleSelection ruleSelection) throws RuleException {
        AnalyzerContext analyzerContext = new AnalyzerContextImpl(configuration, classLoader, store, baselineManager);
        baselineManager.start();
        AnalyzerRuleVisitor visitor = new AnalyzerRuleVisitor(configuration, analyzerContext, ruleInterpreterPlugins, reportPlugin);
        AnalyzerRuleVisitorAuditDecorator visitorDelegate = new AnalyzerRuleVisitorAuditDecorator(visitor, store);
        RuleSetExecutor<Result.Status> executor = new RuleSetExecutor<>(visitorDelegate, configuration.rule());
        executor.execute(ruleSet, ruleSelection);
        baselineManager.stop();
    }
}
