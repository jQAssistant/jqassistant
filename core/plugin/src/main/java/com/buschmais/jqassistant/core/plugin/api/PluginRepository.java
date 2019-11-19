package com.buschmais.jqassistant.core.plugin.api;

import com.buschmais.jqassistant.core.shared.lifecycle.LifecycleAware;

/**
 * Defines the interface for the plugin repository.
 */
public interface PluginRepository extends LifecycleAware {

    @Override
    void initialize();

    @Override
    void destroy ();

    ModelPluginRepository getModelPluginRepository();

    ScannerPluginRepository getScannerPluginRepository();

    ScopePluginRepository getScopePluginRepository();

    RulePluginRepository getRulePluginRepository();

    RuleInterpreterPluginRepository getRuleInterpreterPluginRepository();

    RuleParserPluginRepository getRuleParserPluginRepository();

    ReportPluginRepository getReportPluginRepository();

    ClassLoader getClassLoader();
}
