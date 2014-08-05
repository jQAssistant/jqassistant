package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResource;

public class UrlScannerPlugin extends AbstractScannerPlugin<URL> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlScannerPlugin.class);

    @Override
    protected void initialize() {
    }

    @Override
    public Class<? super URL> getType() {
        return URL.class;
    }

    @Override
    public boolean accepts(URL item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public FileDescriptor scan(final URL item, final String path, Scope scope, Scanner scanner) throws IOException {
        LOGGER.info("Scanning url '{}'.", item.toString());
        return scanner.scan(new FileResource() {
            @Override
            public InputStream createStream() throws IOException {
                return new BufferedInputStream(item.openStream());
            }
        }, item.getPath(), scope);
    }
}
