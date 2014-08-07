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
        for (ScannerPlugin<?> scannerPlugin : scannerPlugins) {
            Class<?> scannerPluginType = scannerPlugin.getType();
            if (scannerPluginType.isAssignableFrom(item.getClass())) {
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
        }
        return fileDescriptor;
    }
}
