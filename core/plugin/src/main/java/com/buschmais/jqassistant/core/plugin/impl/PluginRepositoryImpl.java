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
        this.reportPluginRepository = new ReportPluginRepositoryImpl(pluginConfigurationReader);
        classLoader = pluginConfigurationReader.getClassLoader();
    }

    @Override
    public ModelPluginRepository getModelPluginRepository() throws PluginRepositoryException {
        return modelPluginRepository;
    }

    @Override
    public ScannerPluginRepository getScannerPluginRepository() throws PluginRepositoryException {
        return scannerPluginRepository;
    }

    @Override
    public ScopePluginRepository getScopePluginRepository() throws PluginRepositoryException {
        return scopePluginRepository;
    }

    @Override
    public RulePluginRepository getRulePluginRepository() throws PluginRepositoryException {
        return rulePluginRepository;
    }

    @Override
    public ReportPluginRepository getReportPluginRepository() throws PluginRepositoryException {
        return reportPluginRepository;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
