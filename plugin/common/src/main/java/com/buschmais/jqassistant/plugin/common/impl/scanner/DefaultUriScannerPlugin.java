package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;

/**
 * Scanner plugin which handles URIs with defined default schemas as input.
 */
public class DefaultUriScannerPlugin extends AbstractScannerPlugin<URI, Descriptor, DefaultUriScannerPlugin> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUriScannerPlugin.class);

    private Set<String> schemes;

    @Override
    protected DefaultUriScannerPlugin getThis() {
        return this;
    }

    @Override
    public void initialize() {
        schemes = new HashSet<>();
        schemes.add("file");
        schemes.add("http");
        schemes.add("https");
        schemes.add("ftp");
    }

    @Override
    protected boolean doAccepts(URI item, String path, Scope scope) throws IOException {
        String scheme = item.getScheme().toLowerCase();
        return schemes.contains(scheme);
    }

    @Override
    public Descriptor scan(final URI item, String path, Scope scope, Scanner scanner) throws IOException {
        URL url = item.toURL();
        LOGGER.debug("Scanning url '{}'.", url.toString());
        return scanner.scan(url, path, scope);
    }
}
