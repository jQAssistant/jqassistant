package com.buschmais.jqassistant.core.plugin.impl;

import java.util.Map;

import com.buschmais.jqassistant.core.plugin.api.*;

/**
 * The plugin repository.
 */
public class PluginRepositoryImpl implements PluginRepository {

    private PluginConfigurationReader pluginConfigurationReader;

    private ClassLoader classLoader;

    /**
     * Constructor.
     * 
     * @param pluginConfigurationReader
     *            The plugin configuration reader.
     */
    public PluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) {
        this.pluginConfigurationReader = pluginConfigurationReader;
        classLoader = pluginConfigurationReader.getClassLoader();
    }

    @Override
    public ModelPluginRepository getModelPluginRepository() throws PluginRepositoryException {
        return new ModelPluginRepositoryImpl(pluginConfigurationReader);
    }

    @Override
    public ScannerPluginRepository getScannerPluginRepository(Map<String, Object> properties) throws PluginRepositoryException {
        return new ScannerPluginRepositoryImpl(pluginConfigurationReader, properties);
    }

    @Override
    public ScopePluginRepository getScopePluginRepository() throws PluginRepositoryException {
        return new ScopePluginRepositoryImpl(pluginConfigurationReader);
    }

    @Override
    public RulePluginRepository getRulePluginRepository() throws PluginRepositoryException {
        return new RulePluginRepositoryImpl(pluginConfigurationReader);
    }

    @Override
    public ReportPluginRepository getReportPluginRepository(Map<String, Object> properties) throws PluginRepositoryException {
        return new ReportPluginRepositoryImpl(pluginConfigurationReader, properties);
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
