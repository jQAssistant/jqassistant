package com.buschmais.jqassistant.core.plugin.impl;

import com.buschmais.jqassistant.core.plugin.api.*;

/**
 * The plugin repository.
 */
public class PluginRepositoryImpl implements PluginRepository {

    private ModelPluginRepository modelPluginRepository;
    private ScannerPluginRepository scannerPluginRepository;
    private ScopePluginRepository scopePluginRepository;
    private RulePluginRepository rulePluginRepository;
    private RuleInterpreterPluginRepository ruleInterpreterPluginRepository;
    private RuleParserPluginRepository ruleParserPluginRepository;
    private ReportPluginRepository reportPluginRepository;

    private ClassLoader classLoader;

    /**
     * Constructor.
     *
     * @param pluginConfigurationReader
     *            The plugin configuration reader.
     */
    public PluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) throws PluginRepositoryException {
        this.modelPluginRepository = new ModelPluginRepositoryImpl(pluginConfigurationReader);
        this.scannerPluginRepository = new ScannerPluginRepositoryImpl(pluginConfigurationReader);
        this.scopePluginRepository = new ScopePluginRepositoryImpl(pluginConfigurationReader);
        this.rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
        this.ruleInterpreterPluginRepository = new RuleInterpreterPluginRepositoryImpl(pluginConfigurationReader);
        this.ruleParserPluginRepository = new RuleParserPluginRepositoryImpl(pluginConfigurationReader);
        this.reportPluginRepository = new ReportPluginRepositoryImpl(pluginConfigurationReader);
        classLoader = pluginConfigurationReader.getClassLoader();
    }

    @Override
    public ModelPluginRepository getModelPluginRepository() {
        return modelPluginRepository;
    }

    @Override
    public ScannerPluginRepository getScannerPluginRepository() {
        return scannerPluginRepository;
    }

    @Override
    public ScopePluginRepository getScopePluginRepository() {
        return scopePluginRepository;
    }

    @Override
    public RulePluginRepository getRulePluginRepository() {
        return rulePluginRepository;
    }

    @Override
    public RuleInterpreterPluginRepository getRuleInterpreterPluginRepository() {
        return ruleInterpreterPluginRepository;
    }

    @Override
    public RuleParserPluginRepository getRuleParserPluginRepository() {
        return ruleParserPluginRepository;
    }

    @Override
    public ReportPluginRepository getReportPluginRepository() {
        return reportPluginRepository;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
