package com.buschmais.jqassistant.scm.cli;

import java.util.Map;

import com.buschmais.jqassistant.core.plugin.api.*;
import com.buschmais.jqassistant.core.plugin.impl.*;

/**
 * The plugin repository for the CLI.
 */
public class PluginRepository {

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
     * @param properties
     *            The properties to be passed to the plugins.
     * @throws PluginRepositoryException
     *             If initialization fails.
     */
    public PluginRepository(PluginConfigurationReader pluginConfigurationReader, Map<String, Object> properties) throws PluginRepositoryException {
        modelPluginRepository = new ModelPluginRepositoryImpl(pluginConfigurationReader);
        scannerPluginRepository = new ScannerPluginRepositoryImpl(pluginConfigurationReader, properties);
        scopePluginRepository = new ScopePluginRepositoryImpl(pluginConfigurationReader);
        rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
        reportPluginRepository = new ReportPluginRepositoryImpl(pluginConfigurationReader, properties);
        classLoader = pluginConfigurationReader.getClassLoader();
    }

    public ModelPluginRepository getModelPluginRepository() {
        return modelPluginRepository;
    }

    public ScannerPluginRepository getScannerPluginRepository() {
        return scannerPluginRepository;
    }

    public ScopePluginRepository getScopePluginRepository() {
        return scopePluginRepository;
    }

    public RulePluginRepository getRulePluginRepository() {
        return rulePluginRepository;
    }

    public ReportPluginRepository getReportPluginRepository() {
        return reportPluginRepository;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }
}
