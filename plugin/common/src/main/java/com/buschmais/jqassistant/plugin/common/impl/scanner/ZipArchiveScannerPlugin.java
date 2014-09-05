package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.ArchiveDescriptor;
import com.buschmais.jqassistant.core.store.api.type.FileContainerDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Directory;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Entry;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.File;

public class ZipArchiveScannerPlugin extends AbstractContainerScannerPlugin<ZipFile, ZipEntry> {

    @Override
    protected void initialize() {
    }

    @Override
    public Class<? super ZipFile> getType() {
        return ZipFile.class;
    }

    @Override
    public boolean accepts(ZipFile item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    protected FileContainerDescriptor getContainerDescriptor(ZipFile zipFile) {
        return getStore().create(ArchiveDescriptor.class);
    }

    @Override
    protected Iterable<? extends ZipEntry> getEntries(ZipFile container) throws IOException {
        final Enumeration<? extends ZipEntry> entries = container.entries();
        Iterable<ZipEntry> zipEntries = new Iterable<ZipEntry>() {
            @Override
            public Iterator<ZipEntry> iterator() {
                return new Iterator<ZipEntry>() {
                    @Override
                    public boolean hasNext() {
                        return entries.hasMoreElements();
                    }

                    @Override
                    public ZipEntry next() {
                        return entries.nextElement();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("remove");
                    }
                };
            }
        };
        return zipEntries;
    }

    @Override
    protected String getRelativePath(ZipFile container, ZipEntry entry) {
        String name = entry.getName();
        if (entry.isDirectory()) {
            // strip trailing slash from directory entries
            return "/" + name.substring(0, name.length() - 1);
        } else {
            return "/" + name;
        }
    }

    @Override
    protected Scope createScope(Scope currentScope) {
        return currentScope;
    }

    @Override
    protected Entry getEntry(final ZipFile container, final ZipEntry entry) {
        if (entry.isDirectory()) {
            return new Directory() {
            };
        } else {
            return new File() {
                @Override
                public InputStream createStream() throws IOException {
                    return new BufferedInputStream(container.getInputStream(entry));
                }
            };
        }
    }

}
