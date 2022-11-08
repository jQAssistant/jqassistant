package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Scanner plugin which handles jQAssistant plugin URIs.
 */
public class PluginUriScannerPlugin extends AbstractUriScannerPlugin<URL> {

    public static final String JQASSISTANT_PLUGIN = "jqassistant-plugin";

    @Override
    public boolean accepts(URI uri, String path, Scope scope) throws IOException {
        return JQASSISTANT_PLUGIN.equalsIgnoreCase(uri.getScheme());
    }

    @Override
    protected Optional<URL> getResource(URI uri, ScannerContext context) {
        return resolve(uri, () -> context.getClassLoader().getResource(uri.getSchemeSpecificPart()), context);
    }

}
