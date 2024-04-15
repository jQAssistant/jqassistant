package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.ZipArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractContainerScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractDirectoryResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractVirtualFileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Resource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.ZipFileResource;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

public class ZipFileScannerPlugin
        extends AbstractContainerScannerPlugin<ZipFileResource, ZipArchiveEntry, ZipArchiveDescriptor> {

    @Override
    public Class<? extends ZipFileResource> getType() {
        return ZipFileResource.class;
    }

    @Override
    public Class<ZipArchiveDescriptor> getDescriptorType() {
        return ZipArchiveDescriptor.class;
    }

    @Override
    public boolean accepts(ZipFileResource item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    protected ZipArchiveDescriptor getContainerDescriptor(ZipFileResource zipFile, ScannerContext scannerContext) {
        return scannerContext.peek(ZipArchiveDescriptor.class);
    }

    @Override
    protected Iterable<? extends ZipArchiveEntry> getEntries(ZipFileResource container) throws IOException {
        final Enumeration<? extends ZipArchiveEntry> entries = container.getZipFile().getEntriesInPhysicalOrder();
        return new ZipArchiveEntryIterable(entries);
    }

    @Override
    protected String getContainerPath(ZipFileResource container, String path) {
        return path;
    }

    @Override
    protected String getRelativePath(ZipFileResource container, ZipArchiveEntry entry) {
        String name = entry.getName();
        if (entry.isDirectory()) {
            // strip trailing slash from directory entries
            return "/" + name.substring(0, name.length() - 1);
        } else {
            return "/" + name;
        }
    }

    @Override
    protected void enterContainer(ZipFileResource zipFile, ZipArchiveDescriptor archiveDescriptor, ScannerContext context) throws IOException {
    }

    @Override
    protected void leaveContainer(ZipFileResource zipFile, ZipArchiveDescriptor archiveDescriptor, ScannerContext scannerContext) throws IOException {
        zipFile.close();
    }

    @Override
    protected Resource getEntry(final ZipFileResource container, final ZipArchiveEntry entry) {
        if (entry.isDirectory()) {
            return new AbstractDirectoryResource(entry.getName()) {
            };
        } else {
            return new ZipArchiveEntryResource(container, entry);
        }
    }

    private static class ZipArchiveEntryResource extends AbstractVirtualFileResource {
        private final ZipFileResource container;
        private final ZipArchiveEntry entry;

        public ZipArchiveEntryResource(ZipFileResource container, ZipArchiveEntry entry) {
            this.container = container;
            this.entry = entry;
        }


        @Override
        public InputStream createStream() throws IOException {
            return container.getZipFile().getInputStream(entry);
        }

        @Override
        public String toString() {
            String containerPath = container.getPath();
            String inContainerPath = entry.getName();

            return String.format("%s!%s", containerPath, inContainerPath);
        }

        @Override
        protected String getName() {
            return entry.getName();
        }
    }

    private static class ZipArchiveEntryIterable implements Iterable<ZipArchiveEntry> {
        private final Enumeration<? extends ZipArchiveEntry> entries;

        public ZipArchiveEntryIterable(Enumeration<? extends ZipArchiveEntry> entries) {
            this.entries = entries;
        }

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
    }
}
