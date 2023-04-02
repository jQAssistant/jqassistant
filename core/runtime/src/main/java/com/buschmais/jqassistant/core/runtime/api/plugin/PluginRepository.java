package com.buschmais.jqassistant.core.runtime.api.plugin;

import java.util.Collection;

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

    /**
     * Returns information on all plugins known by jQAssistant,
     * independently if the plugin is enabled or has been disabled
     * or of the type of the plugin.
     *
     * @return unmodifiable collection of all known plugins.
     */
    Collection<PluginInfo> getPluginOverview();
}
