package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Set;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.toSet;

/**
 * Scanner plugin which handles URIs with defined default schemas as input.
 */
public class DefaultUriScannerPlugin extends AbstractScannerPlugin<URI, Descriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultUriScannerPlugin.class);

    private static final Set<String> SCHEMES = Stream.of("file", "http", "https", "ftp")
        .collect(toSet());

    @Override
    public boolean accepts(URI uri, String path, Scope scope) throws IOException {
        String scheme = uri.getScheme()
            .toLowerCase();
        return SCHEMES.contains(scheme);
    }

    @Override
    public Descriptor scan(final URI item, String path, Scope scope, Scanner scanner) throws IOException {
        URL url = item.toURL();
        LOGGER.debug("Scanning url '{}'.", url);
        return scanner.scan(url, path, scope);
    }
}
