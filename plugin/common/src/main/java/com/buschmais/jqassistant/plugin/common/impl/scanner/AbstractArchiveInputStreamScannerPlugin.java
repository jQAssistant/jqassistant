package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.ArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractContainerScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractDirectoryResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractVirtualFileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Resource;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for archive scanners based on commons compress.
 *
 * @param <S>
 *            The ArchiveInputStream type.
 * @param <E>
 *            The ArchiveEntry type.
 * @param <D>
 *            The ArchiveDescriptor type.
 */
public abstract class AbstractArchiveInputStreamScannerPlugin<S extends ArchiveInputStream, E extends ArchiveEntry, D extends ArchiveDescriptor>
        extends AbstractContainerScannerPlugin<S, E, D> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractArchiveInputStreamScannerPlugin.class);

    protected abstract E getNextEntry(S container) throws IOException;

    @Override
    public boolean accepts(S item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    protected Iterable<? extends E> getEntries(final S container) throws IOException {
        return new Iterable<E>() {
            @Override
            public Iterator<E> iterator() {
                return new Iterator<E>() {

                    private E entry = null;

                    @Override
                    public boolean hasNext() {
                        if (entry == null) {
                            try {
                                entry = getNextEntry(container);
                            } catch (IOException e) {
                                LOGGER.warn("Cannot get next entry from archive.", e);
                            }
                            return entry != null;
                        }
                        return true;
                    }

                    @Override
                    public E next() {
                        E next = entry;
                        entry = null;
                        return next;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    @Override
    protected String getContainerPath(S container, String path) {
        return path;
    }

    @Override
    protected String getRelativePath(S container, E entry) {
        String name = entry.getName();
        if (entry.isDirectory()) {
            // strip trailing slash from directory entries
            return "/" + name.substring(0, name.length() - 1);
        } else {
            return "/" + name;
        }
    }

    @Override
    protected void enterContainer(S container, D containerDescriptor, ScannerContext scannerContext) throws IOException {
    }

    @Override
    protected void leaveContainer(S container, D containerDescriptor, ScannerContext scannerContext) throws IOException {
    }

    @Override
    protected Resource getEntry(final S container, final E entry) {
        if (entry.isDirectory()) {
            return new AbstractDirectoryResource(container.toString()) {};
        } else {
            return new AbstractVirtualFileResource() {
                @Override
                public InputStream createStream() {
                    return new InputStream() {

                        int read = 0;

                        @Override
                        public int read() throws IOException {
                            if (read < entry.getSize()) {
                                read++;
                                return container.read();
                            }
                            return -1;
                        }
                    };
                }

                @Override
                protected String getName() {
                    return entry.getName();
                }
            };
        }
    }

}
