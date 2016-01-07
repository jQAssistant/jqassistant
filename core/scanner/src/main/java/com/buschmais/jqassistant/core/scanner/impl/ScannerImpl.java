package com.buschmais.jqassistant.core.scanner.impl;

import static com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import static com.buschmais.xo.spi.reflection.DependencyResolver.DependencyProvider;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.*;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
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

    private final Map<String, Scope> scopes;

    private final Map<Object, Set<ScannerPlugin<?, ?>>> pipelines = new IdentityHashMap<>();

    /**
     * Constructor.
     * 
     * @param scannerPlugins
     *            The configured plugins.
     */
    public ScannerImpl(ScannerContext scannerContext, List<ScannerPlugin<?, ?>> scannerPlugins, Map<String, Scope> scopes) {
        this.scannerContext = scannerContext;
        this.scannerPlugins = scannerPlugins;
        this.scopes = scopes;
        this.scannerContext.push(Scope.class, null);
    }

    @Override
    public <I, D extends Descriptor> D scan(final I item, final String path, final Scope scope) {
        boolean pipelineCreated;
        Set<ScannerPlugin<?, ?>> pipeline = this.pipelines.get(item);
        if (pipeline == null) {
            pipeline = new LinkedHashSet<>();
            this.pipelines.put(item, pipeline);
            pipelineCreated = true;
        } else {
            pipelineCreated = false;
        }
        Class<?> itemClass = item.getClass();
        Class<D> type = null;
        D descriptor = null;
        for (ScannerPlugin<?, ?> scannerPlugin : getScannerPluginsForType(itemClass)) {
            ScannerPlugin<I, D> selectedPlugin = (ScannerPlugin<I, D>) scannerPlugin;
            if (!pipeline.contains(selectedPlugin) && accepts(selectedPlugin, item, path, scope)) {
                pipeline.add(selectedPlugin);
                pushDesriptor(type, descriptor);
                enterScope(scope);
                D newDescriptor = null;
                try {
                    newDescriptor = selectedPlugin.scan(item, path, scope, this);
                } catch (IOException e) {
                    LOGGER.warn("Cannot scan item " + path, e);
                } catch (RuntimeException e) {
                    throw new IllegalStateException(
                            "Unexpected problem while scanning: item='" + item + "', path='" + path + "', scope='" + scope + "', pipeline='" + pipeline + "'.",
                            e);
                }
                leaveScope(scope);
                popDescriptor(type, descriptor);
                descriptor = newDescriptor;
                type = selectedPlugin.getDescriptorType();
            }
        }
        if (pipelineCreated) {
            pipelines.remove(item);
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

        Scope scope = scopes.get(name);
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
            final Map<Class<? extends Descriptor>, Set<ScannerPlugin<?, ?>>> pluginsByDescriptor = new HashMap<>();
            for (ScannerPlugin<?, ?> scannerPlugin : scannerPlugins) {
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

    private void enterScope(Scope newScope) {
        Scope oldScope = scannerContext.peekOrDefault(Scope.class, null);
        if (newScope != null && !newScope.equals(oldScope)) {
            newScope.onEnter(scannerContext);
        }
        scannerContext.push(Scope.class, newScope);
    }

    private void leaveScope(Scope newScope) {
        scannerContext.pop(Scope.class);
        Scope oldScope = scannerContext.peekOrDefault(Scope.class, null);
        if (newScope != null && !newScope.equals(oldScope)) {
            newScope.onLeave(scannerContext);
        }
    }
}
