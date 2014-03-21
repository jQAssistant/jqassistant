package com.buschmais.jqassistant.core.scanner.impl.resource;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.stream.StreamSource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Abstract implementation of an {@link Iterable} for scanning resources using a collection of {@link FileScannerPlugin}s.
 *
 * @param <R> The resource type.
 */
public abstract class AbstractResourceIterable<R> implements Iterable<FileDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractResourceIterable.class);

    private Collection<FileScannerPlugin> plugins;

    public AbstractResourceIterable(Collection<FileScannerPlugin> plugins) {
        this.plugins = plugins;
    }

    protected abstract boolean hasNextResource();

    protected abstract R nextResource();

    protected abstract boolean isDirectory(R element);

    protected abstract String getName(R element);

    protected abstract InputStream openInputStream(String name, R element) throws IOException;

    protected abstract void close() throws IOException;

    @Override
    public Iterator<FileDescriptor> iterator() {
        return new Iterator<FileDescriptor>() {

            private FileDescriptor next = null;

            @Override
            public boolean hasNext() {
                try {
                    while (next == null && hasNextResource()) {
                        R resource = nextResource();
                        if (resource == null) {
                            LOGGER.warn("Skipping an unresolvable resource while scanning.");
                        } else {
                            for (FileScannerPlugin plugin : plugins) {
                                String name = getName(resource);
                                boolean isDirectory = isDirectory(resource);
                                if (plugin.matches(name, isDirectory)) {
                                    if (LOGGER.isInfoEnabled()) {
                                        LOGGER.info("Scanning '{}'", name);
                                    }
                                    next = doScan(resource, plugin, name, isDirectory);
                                }
                            }
                        }
                    }
                    if (next != null) {
                        return true;
                    }
                    close();
                    return false;
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot iterate over elements.", e);
                }
            }

            private FileDescriptor doScan(R resource, FileScannerPlugin plugin, String name, boolean directory) throws IOException {
                try {
                    if (directory) {
                        return plugin.scanDirectory(name);
                    } else {
                        BufferedInputStream inputStream = new BufferedInputStream(openInputStream(name, resource));
                        StreamSource streamSource = new StreamSource(inputStream, name);
                        FileDescriptor descriptor = plugin.scanFile(streamSource);
                        inputStream.close();
                        return descriptor;
                    }
                } catch (Exception e) {
                    throw new IOException("Error scanning " + name, e);
                }
            }

            @Override
            public FileDescriptor next() {
                if (hasNext()) {
                    FileDescriptor result = next;
                    next = null;
                    return result;
                }
                throw new NoSuchElementException("No more results.");
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Cannot remove element.");
            }
        };
    }
}
