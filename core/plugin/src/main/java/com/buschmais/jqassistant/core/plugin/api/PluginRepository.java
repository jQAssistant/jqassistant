package com.buschmais.jqassistant.core.plugin.api;

import com.buschmais.jqassistant.core.shared.lifecycle.LifecycleAware;

/**
 * Defines the interface for the plugin repository.
 */
public interface PluginRepository extends LifecycleAware {

    @Override
    void initialize() throws PluginRepositoryException;

    @Override
    void destroy () throws PluginRepositoryException;

    ModelPluginRepository getModelPluginRepository();

    ScannerPluginRepository getScannerPluginRepository();

    ScopePluginRepository getScopePluginRepository();

    RulePluginRepository getRulePluginRepository();

    RuleInterpreterPluginRepository getRuleInterpreterPluginRepository();

    RuleParserPluginRepository getRuleParserPluginRepository();

    ReportPluginRepository getReportPluginRepository();

    ClassLoader getClassLoader();
}
