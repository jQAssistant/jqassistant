package com.buschmais.jqassistant.core.plugin.api;

/**
 * Defines the interface for the plugin repository.
 */
public interface PluginRepository {

    ModelPluginRepository getModelPluginRepository();

    ScannerPluginRepository getScannerPluginRepository();

    ScopePluginRepository getScopePluginRepository();

    RulePluginRepository getRulePluginRepository();

    RuleInterpreterPluginRepository getRuleInterpreterPluginRepository();

    RuleParserPluginRepository getRuleParserPluginRepository();

    ReportPluginRepository getReportPluginRepository();

    ClassLoader getClassLoader();
}
