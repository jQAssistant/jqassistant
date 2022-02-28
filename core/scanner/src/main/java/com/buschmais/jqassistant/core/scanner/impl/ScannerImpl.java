package com.buschmais.jqassistant.core.scanner.impl;

import java.io.IOException;
import java.util.*;

import com.buschmais.jqassistant.core.scanner.api.*;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.spi.reflection.DependencyResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import static com.buschmais.xo.spi.reflection.DependencyResolver.DependencyProvider;

/**
 * Implementation of the {@link Scanner}.
 */
public class ScannerImpl implements Scanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScannerImpl.class);

    private final Scan configuration;

    private final ScannerContext scannerContext;

    private final ScannerPluginRepository scannerPluginRepository;

    private final Map<String, ScannerPlugin<?, ?>> scannerPlugins;

    private final Map<Class<?>, List<ScannerPlugin<?, ?>>> scannerPluginsPerType = new HashMap<>();

    private final Map<Object, Set<ScannerPlugin<?, ?>>> pipelines = new IdentityHashMap<>();

    /**
     * Constructor.
     *  @param configuration
     *            The configuration.
     * @param pluginProperties
     * @param scannerContext
     *            The scanner context.
     * @param scannerPluginRepository
 *            The {@link ScannerPluginRepository}.
     */
    public ScannerImpl(Scan configuration, Map<String, Object> pluginProperties, ScannerContext scannerContext,
            ScannerPluginRepository scannerPluginRepository) {
        this.configuration = configuration;
        this.scannerContext = scannerContext;
        this.scannerPluginRepository = scannerPluginRepository;
        this.scannerPlugins = scannerPluginRepository.getScannerPlugins(scannerContext, pluginProperties);
        this.scannerContext.push(Scope.class, null);
    }

    @Override
    public <I, D extends Descriptor> D scan(final I item, final String path, final Scope scope) {
        return scan(item, null, path, scope);
    }

    @Override
    public <I, D extends Descriptor> D scan(I item, D descriptor, String path, Scope scope) {
        // Each item may be scanned by multiple plugins, therefore track all plugins
        // that already processed that item in a pipeline
        boolean pipelineCreated;
        Set<ScannerPlugin<?, ?>> pipeline = this.pipelines.get(item);
        if (pipeline == null) {
            pipeline = new LinkedHashSet<>();
            this.pipelines.put(item, pipeline);
            pipelineCreated = true;
        } else {
            pipelineCreated = false;
        }
        Store store = scannerContext.getStore();
        try {
            if (!store.hasActiveTransaction()) {
                // Begin a new transaction if no transaction is active
                store.beginTransaction();
                descriptor = scan(item, descriptor, path, scope, pipeline);
                store.commitTransaction();
            } else {
                // Re-use an existing transaction
                descriptor = scan(item, descriptor, path, scope, pipeline);
            }
        } catch (UnrecoverableScannerException e) {
            // The exception is thrown by a nested scanner invocation, just pass it through
            throw e;
        } catch (RuntimeException e) {
            // An unexpected problem occurred, try to handle it gracefully according to the setting of continueOnError
            if (store.hasActiveTransaction()) {
                store.rollbackTransaction();
            }
            String message = "Unexpected problem encountered while scanning: item='" + item + "', path='" + path + "', scope='" + scope + "', pipeline='"
                    + pipeline + "'. Please report this error including the full stacktrace (continueOnError=" + configuration.continueOnError() + ").";
            if (configuration.continueOnError()) {
                LOGGER.error(message, e);
                LOGGER.info("Continuing scan after error. NOTE: Data might be inconsistent.");
            } else {
                throw new UnrecoverableScannerException(message, e);
            }
        } finally {
            if (pipelineCreated) {
                pipelines.remove(item);
            }
        }
        return descriptor;
    }

    private <I, D extends Descriptor> D scan(I item, D descriptor, String path, Scope scope, Set<ScannerPlugin<?, ?>> pipeline) {
        Class<?> itemClass = item.getClass();
        Class<D> type = null;
        for (ScannerPlugin<?, ?> scannerPlugin : getScannerPluginsForType(itemClass)) {
            ScannerPlugin<I, D> selectedPlugin = (ScannerPlugin<I, D>) scannerPlugin;
            if (!pipeline.contains(selectedPlugin) && accepts(selectedPlugin, item, path, scope) && satisfies(selectedPlugin, descriptor)) {
                pipeline.add(selectedPlugin);
                pushDesriptor(type, descriptor);
                D newDescriptor = null;
                try {
                    newDescriptor = selectedPlugin.scan(item, path, scope, this);
                } catch (IOException e) {
                    LOGGER.warn("Cannot scan item " + path, e);
                } finally {
                    popDescriptor(type, descriptor);
                    descriptor = newDescriptor;
                    type = selectedPlugin.getDescriptorType();
                }
            }
        }
        return descriptor;
    }

    /**
     * Verifies if the selected plugin requires a descriptor.
     *
     * @param selectedPlugin
     *            The selected plugin.
     * @param descriptor
     *            The descriptor.
     * @param <I>
     *            The item type of the plugin.
     * @param <D>
     *            The descriptor type of the plugin.
     * @return <code>true</code> if the selected plugin can be used.
     */
    private <I, D extends Descriptor> boolean satisfies(ScannerPlugin<I, D> selectedPlugin, D descriptor) {
        return !(selectedPlugin.getClass().isAnnotationPresent(Requires.class) && descriptor == null);
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
     * @return <code>true</code> if the plugin accepts the item for scanning.
     */
    protected <I> boolean accepts(ScannerPlugin<I, ?> selectedPlugin, I item, String path, Scope scope) {
        boolean accepted = false;

        try {
            accepted = selectedPlugin.accepts(item, path, scope);
        } catch (IOException e) {
            LOGGER.error("Plugin " + selectedPlugin + " failed to check whether it can accept item " + path, e);
        }

        return accepted;
    }

    /**
     * Push the given descriptor with all it's types to the context.
     *
     * @param descriptor
     *            The descriptor.
     * @param <D>
     *            The descriptor type.
     */
    private <D extends Descriptor> void pushDesriptor(Class<D> type, D descriptor) {
        if (descriptor != null) {
            scannerContext.push(type, descriptor);
            scannerContext.setCurrentDescriptor(descriptor);
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
    private <D extends Descriptor> void popDescriptor(Class<D> type, D descriptor) {
        if (descriptor != null) {
            scannerContext.setCurrentDescriptor(null);
            scannerContext.pop(type);
        }
    }

    @Override
    public ScannerContext getContext() {
        return scannerContext;
    }

    @Override
    public Scope resolveScope(String name) {
        if (name == null) {
            return DefaultScope.NONE;
        }

        Scope scope = scannerPluginRepository.getScopes().get(name);
        if (scope == null) {
            LOGGER.warn("No scope found for name '" + name + "'.");
            scope = DefaultScope.NONE;
        }
        return scope;
    }

    /**
     * Determine the list of scanner plugins that handle the given type.
     *
     * @param type
     *            The type.
     * @return The list of plugins.
     */
    private List<ScannerPlugin<?, ?>> getScannerPluginsForType(final Class<?> type) {
        List<ScannerPlugin<?, ?>> plugins = scannerPluginsPerType.get(type);
        if (plugins == null) {
            // The list of all scanner plugins which accept the given type
            final List<ScannerPlugin<?, ?>> candidates = new LinkedList<>();
            // The map of scanner plugins which produce a descriptor type
            Map<Class<? extends Descriptor>, Set<ScannerPlugin<?, ?>>> pluginsByDescriptor = new HashMap<>();
            for (ScannerPlugin<?, ?> scannerPlugin : scannerPlugins.values()) {
                Class<?> scannerPluginType = scannerPlugin.getType();
                if (scannerPluginType.isAssignableFrom(type)) {
                    Class<? extends Descriptor> descriptorType = scannerPlugin.getDescriptorType();
                    Set<ScannerPlugin<?, ?>> pluginsForDescriptorType = pluginsByDescriptor.get(descriptorType);
                    if (pluginsForDescriptorType == null) {
                        pluginsForDescriptorType = new HashSet<>();
                        pluginsByDescriptor.put(descriptorType, pluginsForDescriptorType);
                    }
                    pluginsForDescriptorType.add(scannerPlugin);
                    candidates.add(scannerPlugin);
                }
            }
            // Order plugins by the values of their optional @Requires
            // annotation
            plugins = DependencyResolver.newInstance(candidates, new DependencyProvider<ScannerPlugin<?, ?>>() {
                @Override
                public Set<ScannerPlugin<?, ?>> getDependencies(ScannerPlugin<?, ?> dependent) {
                    Set<ScannerPlugin<?, ?>> dependencies = new HashSet<>();
                    Requires annotation = dependent.getClass().getAnnotation(Requires.class);
                    if (annotation != null) {
                        for (Class<? extends Descriptor> descriptorType : annotation.value()) {
                            Set<ScannerPlugin<?, ?>> pluginsByDescriptorType = pluginsByDescriptor.get(descriptorType);
                            if (pluginsByDescriptorType != null) {
                                for (ScannerPlugin<?, ?> scannerPlugin : pluginsByDescriptorType) {
                                    if (!scannerPlugin.equals(dependent)) {
                                        dependencies.add(scannerPlugin);
                                    }
                                }
                            }
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
