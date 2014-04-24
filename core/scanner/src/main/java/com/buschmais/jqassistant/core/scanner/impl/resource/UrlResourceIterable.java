package com.buschmais.jqassistant.core.scanner.impl.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;

/**
 * A resource iterable for processing an array of URLs.
 */
public class UrlResourceIterable extends AbstractResourceIterable<URL> {
    private final URL[] urls;
    int index;

    public UrlResourceIterable(Collection<FileScannerPlugin> plugins, URL... urls) {
        super(plugins);
        this.urls = urls;
        index = 0;
    }

    @Override
    protected boolean hasNextResource() {
        return index < urls.length;
    }

    @Override
    protected URL nextResource() {
        return urls[index++];
    }

    @Override
    protected boolean isDirectory(URL resource) {
        return false;
    }

    @Override
    protected String getName(URL resource) {
        return resource.getPath();
    }

    @Override
    protected InputStream openInputStream(String fileName, URL resource) throws IOException {
        return resource.openStream();
    }

    @Override
    protected void close() {
    }
}
