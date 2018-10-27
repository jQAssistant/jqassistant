package com.buschmais.jqassistant.core.plugin.impl;

import com.buschmais.jqassistant.core.plugin.api.*;

/**
 * The plugin repository.
 */
public class PluginRepositoryImpl implements PluginRepository {

    private PluginConfigurationReader pluginConfigurationReader;

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
    public PluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) {
        this.pluginConfigurationReader = pluginConfigurationReader;
    }

    @Override
    public void initialize() throws PluginRepositoryException {
        this.modelPluginRepository = new ModelPluginRepositoryImpl(pluginConfigurationReader);
        this.scannerPluginRepository = new ScannerPluginRepositoryImpl(pluginConfigurationReader);
        this.scannerPluginRepository.initialize();
        this.scopePluginRepository = new ScopePluginRepositoryImpl(pluginConfigurationReader);
        this.rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
        this.ruleInterpreterPluginRepository = new RuleInterpreterPluginRepositoryImpl(pluginConfigurationReader);
        this.ruleInterpreterPluginRepository.initialize();
        this.ruleParserPluginRepository = new RuleParserPluginRepositoryImpl(pluginConfigurationReader);
        this.ruleParserPluginRepository.initialize();
        this.reportPluginRepository = new ReportPluginRepositoryImpl(pluginConfigurationReader);
        this.reportPluginRepository.initialize();
        this.classLoader = pluginConfigurationReader.getClassLoader();
    }

    @Override
    public void destroy() throws PluginRepositoryException {
        if (scannerPluginRepository != null) {
            this.scannerPluginRepository.destroy();
        }
        if (ruleInterpreterPluginRepository != null) {
            this.ruleInterpreterPluginRepository.destroy();
        }
        if (ruleParserPluginRepository != null) {
            this.ruleParserPluginRepository.destroy();
        }
        if (reportPluginRepository != null) {
            this.reportPluginRepository.destroy();
        }
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
