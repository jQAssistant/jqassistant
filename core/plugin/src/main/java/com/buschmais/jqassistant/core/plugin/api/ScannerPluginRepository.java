package com.buschmais.jqassistant.core.plugin.api;

import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.shared.lifecycle.LifecycleAware;

/**
 * Defines the interface for the scanner plugin repository.
 */
public interface ScannerPluginRepository extends LifecycleAware {

    /**
     * Return the instances of the configured scanner plugins.
     *
     * @param scannerContext The scannerContext.
     * @param properties     The configuration properties.
     * @return The instances of the configured scanner plugins.
     */
    Map<String, ScannerPlugin<?, ?>> getScannerPlugins(ScannerContext scannerContext, Map<String, Object> properties);

    @Override
    void initialize();

    @Override
    void destroy();
}
