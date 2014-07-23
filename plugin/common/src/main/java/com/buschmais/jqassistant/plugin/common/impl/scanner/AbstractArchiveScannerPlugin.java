package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.api.iterable.AggregatingIterable;
import com.buschmais.jqassistant.core.scanner.api.iterable.MappingIterable;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.StreamFactory;
import com.buschmais.jqassistant.plugin.common.api.type.ArchiveDescriptor;

public abstract class AbstractArchiveScannerPlugin extends AbstractScannerPlugin<File> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractArchiveScannerPlugin.class);

    @Override
    protected void initialize() {
    }

    @Override
    public Class<File> getType() {
        return File.class;
    }

    @Override
    public boolean accepts(File item, String path, Scope scope) throws IOException {
        return item.isFile() && item.getName().endsWith(getExtension());
    }

    @Override
    public Iterable<? extends FileDescriptor> scan(File file, final String path, final Scope currentScope, final Scanner scanner) throws IOException {
        beforeArchive(path, currentScope);
        final ZipFile zipFile = new ZipFile(file);
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
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
        final Scope scope = createScope(currentScope);
        MappingIterable<ZipEntry, Iterable<? extends FileDescriptor>> fileDescriptors = new MappingIterable<ZipEntry, Iterable<? extends FileDescriptor>>(
                zipEntries) {
            @Override
            protected Iterable<? extends FileDescriptor> map(final ZipEntry zipEntry) throws IOException {
                String name = "/" + zipEntry.getName();
                StreamFactory streamFactory = new StreamFactory() {
                    @Override
                    public InputStream createStream() throws IOException {
                        return new BufferedInputStream(zipFile.getInputStream(zipEntry));
                    }
                };
                beforeEntry(path, scope);
                LOGGER.info("Scanning entry '{}'.", name);
                Iterable<? extends FileDescriptor> descriptors = scanner.scan(streamFactory, name, scope);
                
                return afterEntry(descriptors);
            }
        };
        return afterArchive(new AggregatingIterable<>(fileDescriptors));
    }

    protected abstract void beforeArchive(String path, Scope scope);

    protected abstract void beforeEntry(String path, Scope scope);

    protected abstract Iterable<? extends FileDescriptor> afterEntry(Iterable<? extends FileDescriptor> fileDescriptors);

    protected abstract Iterable<? extends FileDescriptor> afterArchive(Iterable<? extends FileDescriptor> fileDescriptors);

    protected abstract String getExtension();

    protected abstract Scope createScope(Scope currentScope);
    
}
