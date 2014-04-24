package com.buschmais.jqassistant.core.scanner.impl.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;

/**
 * A resource iterable for processing an array of classes.
 */
public class ClassResourceIterable extends AbstractResourceIterable<Class<?>> {
    private final Class<?>[] classes;
    int index;

    public ClassResourceIterable(Collection<FileScannerPlugin> plugins, Class<?>... classes) {
        super(plugins);
        this.classes = classes;
        index = 0;
    }

    @Override
    protected boolean hasNextResource() {
        return index < classes.length;
    }

    @Override
    protected Class<?> nextResource() {
        return classes[index++];
    }

    @Override
    protected boolean isDirectory(Class<?> resource) {
        return false;
    }

    @Override
    protected String getName(Class<?> resource) {
        return "/" + resource.getName().replace('.', '/') + ".class";
    }

    @Override
    protected InputStream openInputStream(String fileName, Class<?> resource) throws IOException {
        return resource.getResourceAsStream(fileName);
    }

    @Override
    protected void close() {
    }
}
