package com.buschmais.jqassistant.scm.neo4jserver.impl.rest;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.RuleSelector;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.RuleSetResolverException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.analysis.impl.RuleSelectorImpl;
import com.buschmais.jqassistant.core.analysis.impl.RuleSetReaderImpl;
import com.buschmais.jqassistant.core.plugin.api.ModelPluginRepository;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.impl.ModelPluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.RulePluginRepositoryImpl;
import com.buschmais.jqassistant.core.report.api.ReportHelper;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.scm.common.console.Slf4jConsole;

import java.util.List;

import javax.xml.transform.Source;

public abstract class AbstractJQARestService {

    /**
     * The rules reader instance.
     */
    private RuleSetReader ruleSetReader;
    private PluginConfigurationReader pluginConfigurationReader;
    private RulePluginRepository rulePluginRepository;

    private Store store = null;

    protected AbstractJQARestService(Store store) throws PluginRepositoryException {
        this.store = store;
        pluginConfigurationReader = new PluginConfigurationReaderImpl();
        rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
        ruleSetReader = new RuleSetReaderImpl();
    }

    protected RuleSet getAvailableRules() {
        List<Source> ruleSources = rulePluginRepository.getRuleSources();
        return ruleSetReader.read(ruleSources);
    }

    protected Store getStore() {
        return store;
    }

    protected ModelPluginRepository getModelPluginRepository() {
        try {
            return new ModelPluginRepositoryImpl(pluginConfigurationReader);
        } catch (PluginRepositoryException e) {
            throw new IllegalStateException("Cannot get model plugin repository", e);
        }
    }

    protected RuleSet getEffectiveRuleSet(List<String> conceptNames, List<String> constraintNames, List<String> groupNames) throws RuleSetResolverException {
        RuleSet availableRules = getAvailableRules();
        RuleSelector ruleSelector = new RuleSelectorImpl();
        return ruleSelector.getEffectiveRuleSet(availableRules, conceptNames, constraintNames, groupNames);
    }

    public InMemoryReportWriter analyze(List<String> conceptNames, List<String> constraintNames, List<String> groupNames) throws Exception {
        RuleSet effectiveRuleSet = getEffectiveRuleSet(conceptNames, constraintNames, groupNames);
        InMemoryReportWriter reportWriter = new InMemoryReportWriter();
        Slf4jConsole console = new Slf4jConsole();
        Analyzer analyzer = new AnalyzerImpl(store, reportWriter, console);
        analyzer.execute(effectiveRuleSet);
        ReportHelper reportHelper = new ReportHelper(console);
        reportHelper.verifyConceptResults(reportWriter);
        reportHelper.verifyConstraintViolations(reportWriter);
        return reportWriter;
    }
}
