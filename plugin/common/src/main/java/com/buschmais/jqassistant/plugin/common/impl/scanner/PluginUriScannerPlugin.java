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

    public static final String PROPERTY_IGNORE_NON_EXISTING_RESOURCES = "jqassistant-plugin.ignore-non-existing-resources";

    private boolean ignoreNonExistingResources;

    @Override
    protected void configure() {
        this.ignoreNonExistingResources = getBooleanProperty(PROPERTY_IGNORE_NON_EXISTING_RESOURCES, false);
    }

    @Override
    public boolean accepts(URI uri, String path, Scope scope) throws IOException {
        return JQASSISTANT_PLUGIN.equalsIgnoreCase(uri.getScheme());
    }

    @Override
    protected Optional<URL> getResource(URI uri, ScannerContext context) {
        return resolve(uri, () -> {
            URL resource = context.getClassLoader()
                .getResource(uri.getSchemeSpecificPart());
            if (resource == null && !ignoreNonExistingResources) {
                throw new IllegalArgumentException("Cannot resolve resource '" + uri + "' from plugins.");
            }
            return resource;
        }, context);
    }

}
