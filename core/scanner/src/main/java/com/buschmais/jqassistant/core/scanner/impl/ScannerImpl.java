package com.buschmais.jqassistant.core.scanner.impl;

import static com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import static com.buschmais.xo.spi.reflection.DependencyResolver.DependencyProvider;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.*;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.spi.reflection.DependencyResolver;

/**
 * Implementation of the {@link Scanner}.
 */
public class ScannerImpl implements Scanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScannerImpl.class);

    private final ScannerContext scannerContext;

    private final List<ScannerPlugin<?, ?>> scannerPlugins;

    private final ScannerListener scannerListener;

    private final Map<Class<?>, List<ScannerPlugin<?, ?>>> scannerPluginsPerType = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param store
     *            The store.
     * @param scannerPlugins
     *            The configured plugins.
     */
    public ScannerImpl(Store store, List<ScannerPlugin<?, ?>> scannerPlugins) {
        this.scannerContext = new ScannerContextImpl(store);
        this.scannerPlugins = scannerPlugins;
        this.scannerListener = new DefaultScannerListener(store);
    }

    @Override
    public <I, D extends Descriptor> D scan(final I item, final String path, final Scope scope) {
        D descriptor = null;
        Class<?> itemClass = item.getClass();
        for (ScannerPlugin<?, ?> scannerPlugin : getScannerPluginsForType(itemClass)) {
            ScannerPlugin<I, D> selectedPlugin = (ScannerPlugin<I, D>) scannerPlugin;
            try {
                if (selectedPlugin.accepts(item, path, scope)) {
                    scannerListener.before(item, path, scope);
                    descriptor = selectedPlugin.scan(item, path, scope, this);
                    scannerListener.after(item, path, scope, descriptor);
                }
            } catch (IOException e) {
                LOGGER.error("Cannot scan item " + path, e);
            }
        }
        return descriptor;
    }

    @Override
    public ScannerContext getContext() {
        return scannerContext;
    }

    /**
     * Determine the list of scanner plugins that handle the given type.
     * 
     * @param type
     *            The type.
     * @return The list of plugins.
     */
    private List<ScannerPlugin<?, ?>> getScannerPluginsForType(Class<?> type) {
        List<ScannerPlugin<?, ?>> plugins = scannerPluginsPerType.get(type);
        if (plugins == null) {
            final Map<Class<?>, ScannerPlugin<?, ?>> pluginsForType = new HashMap<>();
            for (ScannerPlugin<?, ?> scannerPlugin : scannerPlugins) {
                Class<?> scannerPluginType = scannerPlugin.getType();
                if (scannerPluginType.isAssignableFrom(type)) {
                    pluginsForType.put(scannerPlugin.getClass(), scannerPlugin);
                }
            }
            plugins = DependencyResolver.newInstance(pluginsForType.values(), new DependencyProvider<ScannerPlugin<?, ?>>() {
                @Override
                public Set<ScannerPlugin<?, ?>> getDependencies(ScannerPlugin<?, ?> dependent) {
                    Set<ScannerPlugin<?, ?>> dependencies = new HashSet<>();
                    Requires annotation = dependent.getType().getAnnotation(Requires.class);
                    if (annotation != null) {
                        for (Class<? extends ScannerPlugin<?, ?>> pluginType : annotation.value()) {
                            dependencies.add(pluginsForType.get(pluginType));
                        }
                    }
                    return dependencies;
                }
            }).resolve();
            scannerPluginsPerType.put(type, plugins);
        }
        return plugins;
    }
}
