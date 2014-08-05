package com.buschmais.jqassistant.core.scanner.impl;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerListener;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;

/**
 * Implementation of the {@link Scanner}.
 */
public class ScannerImpl implements Scanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScannerImpl.class);

    private final List<ScannerPlugin<?>> scannerPlugins;

    private final ScannerListener scannerListener;

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
    public <I> FileDescriptor scan(final I item, final String relativePath, final Scope scope) throws IOException {
        for (ScannerPlugin<?> scannerPlugin : scannerPlugins) {
            Class<?> scannerPluginType = scannerPlugin.getType();
            if (scannerPluginType.isAssignableFrom(item.getClass())) {
                ScannerPlugin<I> selectedPlugin = (ScannerPlugin<I>) scannerPlugin;
                if (selectedPlugin.accepts(item, relativePath, scope)) {
                    scannerListener.before(item, relativePath, scope);
                    FileDescriptor fileDescriptor = selectedPlugin.scan(item, relativePath, scope, this);
                    scannerListener.after(item, relativePath, scope, fileDescriptor);
                }
            }
        }
        LOGGER.debug("No scanner plugin found for '{}'.", relativePath);
        return null;
    }
}
