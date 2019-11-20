package com.buschmais.jqassistant.core.plugin.api;

import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;
import com.buschmais.jqassistant.core.shared.lifecycle.LifecycleAware;
import com.buschmais.jqassistant.core.store.spi.StorePluginRepository;

/**
 * Defines the interface for the plugin repository.
 */
public interface PluginRepository extends LifecycleAware {

    @Override
    void initialize();

    @Override
    void destroy ();

    StorePluginRepository getStorePluginRepository();

    ScannerPluginRepository getScannerPluginRepository();

    RulePluginRepository getRulePluginRepository();

    RuleInterpreterPluginRepository getRuleInterpreterPluginRepository();

    RuleParserPluginRepository getRuleParserPluginRepository();

    ReportPluginRepository getReportPluginRepository();

    ClassLoader getClassLoader();
}
