package com.buschmais.jqassistant.core.scanner.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerListener;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;

/**
 * Implementation of the {@link Scanner}.
 */
public class ScannerImpl implements Scanner {

    private final List<ScannerPlugin<?>> scannerPlugins;

    private final ScannerListener scannerListener;

    private final Map<Class<?>, List<ScannerPlugin<?>>> scannerPluginsPerType = new HashMap<>();

    /**
     * Constructor.
     *
     * @param scannerPlugins
     *            The configured plugins.
     */
    public ScannerImpl(List<ScannerPlugin<?>> scannerPlugins) {
        this(scannerPlugins, null);
    }

    /**
     * Constructor.
     *
     * @param scannerPlugins
     *            The configured plugins.
     * @param scannerListener
     */
    public ScannerImpl(List<ScannerPlugin<?>> scannerPlugins, ScannerListener scannerListener) {
        this.scannerPlugins = scannerPlugins;
        this.scannerListener = scannerListener;
    }

    @Override
    public <I> FileDescriptor scan(I item, Scope scope) throws IOException {
        return scan(item, null, scope);
    }

    @Override
    public <I> FileDescriptor scan(final I item, final String path, final Scope scope) throws IOException {
        FileDescriptor fileDescriptor = null;
        Class<?> itemClass = item.getClass();
        for (ScannerPlugin<?> scannerPlugin : getScannerPluginsForType(itemClass)) {
            ScannerPlugin<I> selectedPlugin = (ScannerPlugin<I>) scannerPlugin;
            if (selectedPlugin.accepts(item, path, scope)) {
                if (scannerListener != null) {
                    scannerListener.before(item, path, scope);
                }
                fileDescriptor = selectedPlugin.scan(item, path, scope, this);
                if (scannerListener != null) {
                    scannerListener.after(item, path, scope, fileDescriptor);
                }
            }
        }
        return fileDescriptor;
    }

    /**
     * Determine the list of scanner plugins that handle the given type.
     * 
     * @param type
     *            The type.
     * @return The list of plugins.
     */
    private List<ScannerPlugin<?>> getScannerPluginsForType(Class<?> type) {
        List<ScannerPlugin<?>> plugins = scannerPluginsPerType.get(type);
        if (plugins == null) {
            plugins = new ArrayList<>();
            for (ScannerPlugin<?> scannerPlugin : scannerPlugins) {
                Class<?> scannerPluginType = scannerPlugin.getType();
                if (scannerPluginType.isAssignableFrom(type)) {
                    plugins.add(scannerPlugin);
                }
                scannerPluginsPerType.put(type, plugins);
            }
        }
        return plugins;
    }
}
