package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractResourceScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractFileResource;

/**
 * Scanner plugin which handles URLs as input.
 */
public class UrlScannerPlugin extends AbstractResourceScannerPlugin<URL, FileDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlScannerPlugin.class);

    @Override
    public boolean accepts(URL item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public FileDescriptor scan(final URL item, String path, Scope scope, Scanner scanner) throws IOException {
        LOGGER.info("Scanning url '{}'.", item.toString());
        Descriptor descriptor;
        try (AbstractFileResource fileResource = new AbstractFileResource() {
            @Override
            public InputStream createStream() throws IOException {
                return new BufferedInputStream(item.openStream());
            }
        }) {
            descriptor = scanner.scan(fileResource, path, scope);
            return toFileDescriptor(fileResource, descriptor, path, scanner.getContext());
        }
    }
}
