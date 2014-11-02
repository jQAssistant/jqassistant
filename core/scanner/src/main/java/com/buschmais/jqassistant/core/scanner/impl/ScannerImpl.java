package com.buschmais.jqassistant.core.scanner.impl;

import static com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import static com.buschmais.xo.spi.reflection.DependencyResolver.DependencyProvider;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
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
    }

    @Override
    public <I, D extends Descriptor> D scan(final I item, final String path, final Scope scope) {
        D descriptor = null;
        Class<?> itemClass = item.getClass();
        for (ScannerPlugin<?, ?> scannerPlugin : getScannerPluginsForType(itemClass)) {
            ScannerPlugin<I, D> selectedPlugin = (ScannerPlugin<I, D>) scannerPlugin;
            if (accepts(selectedPlugin, item, path, scope)) {
                pushDesriptor(descriptor);
                D newDescriptor = null;
                try {
                    newDescriptor = selectedPlugin.scan(item, path, scope, this);
                } catch (IOException e) {
                    LOGGER.error("Cannot scan item " + path, e);
                }
                popDescriptor(descriptor);
                descriptor = newDescriptor;
            }
        }
        return descriptor;
    }

    /**
     * Checks whether a plugin accepts an item.
     * 
     * @param selectedPlugin
     *            The plugin.
     * @param item
     *            The item.
     * @param path
     *            The path.
     * @param scope
     *            The scope.
     * @param <I>
     *            The item type.
     * @param <D>
     *            The descriptor type.
     * @return <code>true</code> if the plugin accepts the item for scanning.
     */
    private <I, D extends Descriptor> boolean accepts(ScannerPlugin<I, ?> selectedPlugin, I item, String path, Scope scope) {
        try {
            return selectedPlugin.accepts(item, path, scope);
        } catch (IOException e) {
            LOGGER.error("Plugin " + selectedPlugin + " cannot check if it accepts item " + path, e);
            return false;
        }
    }

    /**
     * Push the given descriptor with all it's types to the context.
     * 
     * @param descriptor
     *            The descriptor.
     * @param <D>
     *            The descriptor type.
     */
    private <D extends Descriptor> void pushDesriptor(D descriptor) {
        if (descriptor != null) {
            for (Class<?> type : descriptor.getClass().getInterfaces()) {
                scannerContext.push((Class<Object>) type, descriptor);
            }
        }
    }

    /**
     * Pop the given descriptor from the context.
     * 
     * @param descriptor
     *            The descriptor.
     * @param <D>
     *            The descriptor type.
     */
    private <D extends Descriptor> void popDescriptor(D descriptor) {
        if (descriptor != null) {
            for (Class<?> type : descriptor.getClass().getInterfaces()) {
                scannerContext.pop(type);
            }
        }
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
                    Requires annotation = dependent.getClass().getAnnotation(Requires.class);
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
