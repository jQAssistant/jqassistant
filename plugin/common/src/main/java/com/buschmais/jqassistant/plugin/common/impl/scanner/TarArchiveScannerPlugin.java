package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.TarArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractContainerScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractDirectoryResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractFileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Resource;

public class TarArchiveScannerPlugin extends AbstractContainerScannerPlugin<TarArchiveInputStream, TarArchiveEntry, TarArchiveDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TarArchiveScannerPlugin.class);

    @Override
    public Class<? extends TarArchiveInputStream> getType() {
        return TarArchiveInputStream.class;
    }

    @Override
    public Class<TarArchiveDescriptor> getDescriptorType() {
        return TarArchiveDescriptor.class;
    }

    @Override
    public boolean accepts(TarArchiveInputStream item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    protected TarArchiveDescriptor getContainerDescriptor(TarArchiveInputStream item, ScannerContext scannerContext) {
        return scannerContext.getStore().create(TarArchiveDescriptor.class);
    }

    @Override
    protected Iterable<? extends TarArchiveEntry> getEntries(final TarArchiveInputStream container) throws IOException {
        return new Iterable<TarArchiveEntry>() {
            @Override
            public Iterator<TarArchiveEntry> iterator() {
                return new Iterator<TarArchiveEntry>() {

                    private TarArchiveEntry entry = null;

                    @Override
                    public boolean hasNext() {
                        if (entry == null) {
                            try {
                                entry = container.getNextTarEntry();
                            } catch (IOException e) {
                                LOGGER.warn("Cannot get next entry from TAR archive.", e);
                            }
                            return entry != null;
                        }
                        return true;
                    }

                    @Override
                    public TarArchiveEntry next() {
                        TarArchiveEntry next = entry;
                        entry = null;
                        return next;
                    }
                };
            }
        };
    }

    @Override
    protected String getContainerPath(TarArchiveInputStream container, String path) {
        return path;
    }

    @Override
    protected String getRelativePath(TarArchiveInputStream container, TarArchiveEntry entry) {
        String name = entry.getName();
        if (entry.isDirectory()) {
            // strip trailing slash from directory entries
            return "/" + name.substring(0, name.length() - 1);
        } else {
            return "/" + name;
        }
    }

    @Override
    protected void enterContainer(TarArchiveInputStream container, TarArchiveDescriptor containerDescriptor, ScannerContext scannerContext)
            throws IOException {
    }

    @Override
    protected void leaveContainer(TarArchiveInputStream container, TarArchiveDescriptor containerDescriptor, ScannerContext scannerContext)
            throws IOException {
    }

    @Override
    protected Resource getEntry(final TarArchiveInputStream container, final TarArchiveEntry entry) {
        if (entry.isDirectory()) {
            return new AbstractDirectoryResource() {};
        } else {
            return new AbstractFileResource() {
                @Override
                public InputStream createStream() throws IOException {
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
            };
        }
    }

}
