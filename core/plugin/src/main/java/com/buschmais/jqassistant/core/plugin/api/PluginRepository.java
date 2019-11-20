package com.buschmais.jqassistant.core.plugin.api;

import com.buschmais.jqassistant.core.analysis.spi.AnalyzerPluginRepository;
import com.buschmais.jqassistant.core.rule.spi.RulePluginRepository;
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
    void destroy();

    StorePluginRepository getStorePluginRepository();

    ScannerPluginRepository getScannerPluginRepository();

    RulePluginRepository getRulePluginRepository();

    AnalyzerPluginRepository getAnalyzerPluginRepository();

    ClassLoader getClassLoader();
}
