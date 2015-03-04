package com.buschmais.jqassistant.core.plugin.api;

import java.util.Map;

/**
 * Defines the interface for the plugin repository.
 */
public interface PluginRepository {

    ModelPluginRepository getModelPluginRepository() throws PluginRepositoryException;

    ScannerPluginRepository getScannerPluginRepository(Map<String, Object> properties) throws PluginRepositoryException;

    ScopePluginRepository getScopePluginRepository() throws PluginRepositoryException;

    RulePluginRepository getRulePluginRepository() throws PluginRepositoryException;

    ReportPluginRepository getReportPluginRepository(Map<String, Object> properties) throws PluginRepositoryException;

    ClassLoader getClassLoader();
}
