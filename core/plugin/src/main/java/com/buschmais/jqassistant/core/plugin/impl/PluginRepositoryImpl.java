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
    private RuleLanguagePluginRepository ruleLanguagePluginRepository;
    private RuleSourceReaderPluginRepository ruleSourceReaderPluginRepository;
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
        this.ruleLanguagePluginRepository = new RuleLanguagePluginRepositoryImpl(pluginConfigurationReader);
        this.ruleSourceReaderPluginRepository = new RuleSourceReaderPluginRepositoryImpl(pluginConfigurationReader);
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
    public RuleLanguagePluginRepository getRuleLanguagePluginRepository() {
        return ruleLanguagePluginRepository;
    }

    @Override
    public RuleSourceReaderPluginRepository getRuleSourceReaderPluginRepository() {
        return ruleSourceReaderPluginRepository;
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
