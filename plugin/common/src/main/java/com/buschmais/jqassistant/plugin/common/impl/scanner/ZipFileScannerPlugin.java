package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.ZipArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractContainerScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractDirectoryResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractFileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Resource;

public class ZipFileScannerPlugin extends AbstractContainerScannerPlugin<ZipFile, ZipArchiveEntry, ZipArchiveDescriptor> {

    @Override
    public Class<? extends ZipFile> getType() {
        return ZipFile.class;
    }

    @Override
    public Class<ZipArchiveDescriptor> getDescriptorType() {
        return ZipArchiveDescriptor.class;
    }

    @Override
    public boolean accepts(ZipFile item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    protected ZipArchiveDescriptor getContainerDescriptor(ZipFile zipFile, ScannerContext scannerContext) {
        return scannerContext.peek(ZipArchiveDescriptor.class);
    }

    @Override
    protected Iterable<? extends ZipArchiveEntry> getEntries(ZipFile container) throws IOException {
        final Enumeration<? extends ZipArchiveEntry> entries = container.getEntriesInPhysicalOrder();
        return new Iterable<ZipArchiveEntry>() {
            @Override
            public Iterator<ZipArchiveEntry> iterator() {
                return new Iterator<ZipArchiveEntry>() {
                    @Override
                    public boolean hasNext() {
                        return entries.hasMoreElements();
                    }

                    @Override
                    public ZipArchiveEntry next() {
                        return entries.nextElement();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("remove");
                    }
                };
            }
        };
    }

    @Override
    protected String getContainerPath(ZipFile container, String path) {
        return path;
    }

    @Override
    protected String getRelativePath(ZipFile container, ZipArchiveEntry entry) {
        String name = entry.getName();
        if (entry.isDirectory()) {
            // strip trailing slash from directory entries
            return "/" + name.substring(0, name.length() - 1);
        } else {
            return "/" + name;
        }
    }

    @Override
    protected void enterContainer(ZipFile zipFile, ZipArchiveDescriptor archiveDescriptor, ScannerContext context) throws IOException {
    }

    @Override
    protected void leaveContainer(ZipFile zipFile, ZipArchiveDescriptor archiveDescriptor, ScannerContext scannerContext) throws IOException {
        zipFile.close();
    }

    @Override
    protected Resource getEntry(final ZipFile container, final ZipArchiveEntry entry) {
        if (entry.isDirectory()) {
            return new AbstractDirectoryResource() {
            };
        } else {
            return new AbstractFileResource() {
                @Override
                public InputStream createStream() throws IOException {
                    return container.getInputStream(entry);
                }
            };
        }
    }

}
