package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;

import static java.util.Optional.of;
import static java.util.stream.Collectors.toSet;

/**
 * Scanner plugin which handles URIs with defined default schemas as input.
 */
public class DefaultUriScannerPlugin extends AbstractUriScannerPlugin<URL> {

    private static final Set<String> SCHEMES = Stream.of("file", "http", "https", "ftp")
        .collect(toSet());

    @Override
    public boolean accepts(URI uri, String path, Scope scope) throws IOException {
        String scheme = uri.getScheme()
            .toLowerCase();
        return SCHEMES.contains(scheme);
    }

    @Override
    protected Optional<URL> getResource(URI uri, ScannerContext context) throws MalformedURLException {
        return of(uri.toURL());
    }
}
