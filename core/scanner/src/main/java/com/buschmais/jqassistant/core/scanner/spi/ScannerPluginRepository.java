package com.buschmais.jqassistant.core.scanner.spi;

import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
import com.buschmais.jqassistant.core.shared.lifecycle.LifecycleAware;

/**
 * Defines the interface for the scanner plugin repository.
 */
public interface ScannerPluginRepository extends LifecycleAware {

    /**
     * Return the instances of the configured scanner plugins.
     *
     * @param scan
     *     The scan configuration.
     * @param scannerContext
     *     The scannerContext.
     * @return The instances of the configured scanner plugins.
     */
    Set<ScannerPlugin<?, ?>> getScannerPlugins(Scan scan, ScannerContext scannerContext);

    /**
     * Return the scope for the given name.
     *
     * @param name
     *     The name.
     * @return The scope.
     */
    Scope getScope(String name);

    /**
     * Return a map of all scopes identified by their fully qualified name.
     *
     * @return The map of all scopes.
     */
    Map<String, Scope> getScopes();

    @Override
    void initialize();

    @Override
    void destroy();
}
