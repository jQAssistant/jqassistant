package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractFileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

/**
 * Scanner plugin which handles URIs with defined default schemas as input.
 */
public class JQAssistantPluginScannerPlugin extends AbstractScannerPlugin<URI, Descriptor> {

    public static final String JQASSISTANT_PLUGIN = "jqassistant-plugin";

    @Override
    public boolean accepts(URI uri, String path, Scope scope) throws IOException {
        return JQASSISTANT_PLUGIN.equalsIgnoreCase(uri.getScheme());
    }

    @Override
    public Descriptor scan(final URI uri, String path, Scope scope, Scanner scanner) throws IOException {
        URL resource = JQAssistantPluginScannerPlugin.class.getResource(uri.getPath());
        if (resource == null) {
            return null;
        }
        FileResource fileResource = new AbstractFileResource() {
            @Override
            public InputStream createStream() throws IOException {
                return resource.openStream();
            }
        };
        return scanner.scan(fileResource, path, scope);
    }
}
