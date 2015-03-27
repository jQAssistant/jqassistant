package com.buschmais.jqassistant.core.plugin.api;

/**
 * Defines the interface for the plugin repository.
 */
public interface PluginRepository {

    ModelPluginRepository getModelPluginRepository() throws PluginRepositoryException;

    ScannerPluginRepository getScannerPluginRepository() throws PluginRepositoryException;

    ScopePluginRepository getScopePluginRepository() throws PluginRepositoryException;

    RulePluginRepository getRulePluginRepository() throws PluginRepositoryException;

    ReportPluginRepository getReportPluginRepository() throws PluginRepositoryException;

    ClassLoader getClassLoader();
}
