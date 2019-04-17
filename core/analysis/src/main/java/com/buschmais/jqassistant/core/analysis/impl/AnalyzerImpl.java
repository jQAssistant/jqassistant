package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerConfiguration;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSelection;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.Verification;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.rule.api.executor.RuleSetExecutor;
import com.buschmais.jqassistant.core.rule.api.executor.RuleVisitor;
import com.buschmais.jqassistant.core.store.api.Store;

import org.slf4j.Logger;

/**
 * Implementation of the {@link Analyzer}.
 */
public class AnalyzerImpl implements Analyzer {

    private final AnalyzerConfiguration configuration;

    private final AnalyzerContext analyzerContext;

    private final Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins;
    private final ReportPlugin reportPlugin;

    /**
     * Constructor.
     *
     * @param configuration
     *            The configuration.
     * @param store
     *            The store
     * @param ruleInterpreterPlugins
     *            The {@link RuleInterpreterPlugin}s.
     * @param reportPlugin
     *            The report wrtier.
     * @param log
     *            The {@link Logger}.
     */
    public AnalyzerImpl(AnalyzerConfiguration configuration, Store store, Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins,
            ReportPlugin reportPlugin, Logger log) {
        this.configuration = configuration;
        this.analyzerContext = new AnalyzerContextImpl(store, log, initVerificationStrategies());
        this.ruleInterpreterPlugins = ruleInterpreterPlugins;
        this.reportPlugin = reportPlugin;
    }

    @Override
    public void execute(RuleSet ruleSet, RuleSelection ruleSelection, Map<String, String> ruleParameters) throws RuleException {
        RuleVisitor visitor = new TransactionalRuleVisitor(
                new AnalyzerRuleVisitor(configuration, analyzerContext, ruleParameters, ruleInterpreterPlugins, reportPlugin), analyzerContext.getStore());
        RuleSetExecutor executor = new RuleSetExecutor(visitor, configuration.getRuleSetExecutorConfiguration());
        executor.execute(ruleSet, ruleSelection);
    }

    private Map<Class<? extends Verification>, VerificationStrategy> initVerificationStrategies() {
        Map<Class<? extends Verification>, VerificationStrategy> verificationStrategies = new HashMap<>();
        RowCountVerificationStrategy rowCountVerificationStrategy = new RowCountVerificationStrategy();
        verificationStrategies.put(rowCountVerificationStrategy.getVerificationType(), rowCountVerificationStrategy);
        AggregationVerificationStrategy aggregationVerificationStrategy = new AggregationVerificationStrategy();
        verificationStrategies.put(aggregationVerificationStrategy.getVerificationType(), aggregationVerificationStrategy);
        return verificationStrategies;
    }

}
