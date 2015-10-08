package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractResourceScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractFileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

/**
 * Scanner plugin which handles URLs as input.
 */
public class UrlScannerPlugin extends AbstractResourceScannerPlugin<URL, FileDescriptor> {

    @Override
    public boolean accepts(URL item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public FileDescriptor scan(final URL item, String path, Scope scope, Scanner scanner) throws IOException {
        try (FileResource fileResource = new BufferedFileResource(new AbstractFileResource() {
            @Override
            public InputStream createStream() throws IOException {
                return item.openStream();
            }
        })) {
            return scanner.scan(fileResource, path, scope);
        }
    }
}
