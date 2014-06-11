package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.api.iterable.AggregatingIterable;
import com.buschmais.jqassistant.core.scanner.api.iterable.MappingIterable;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                };
            }
        };
        final Scope scope = createScope(currentScope);
        MappingIterable<ZipEntry, Iterable<? extends FileDescriptor>> fileDescriptors = new MappingIterable<ZipEntry, Iterable<? extends FileDescriptor>>(
                zipEntries) {
            @Override
            protected Iterable<? extends FileDescriptor> map(ZipEntry zipEntry) throws IOException {
                String name = "/" + zipEntry.getName();
                InputStream stream = zipFile.getInputStream(zipEntry);
                beforeEntry(path, scope);
                LOGGER.info("Scanning entry '{}'.", name);
                Iterable<? extends FileDescriptor> descriptors = scanner.scan(stream, name, scope);
                return afterEntry(descriptors);
            }
        };
        return afterArchive(new AggregatingIterable<>(fileDescriptors));
    }

    protected abstract void beforeArchive(String path, Scope scope);

    protected abstract void beforeEntry(String path, Scope scope);

    protected abstract Iterable<? extends FileDescriptor> afterEntry(Iterable<? extends FileDescriptor> fileDescriptor);

    protected abstract Iterable<? extends FileDescriptor> afterArchive(Iterable<? extends FileDescriptor> fileDescriptor);

    protected abstract String getExtension();

    protected abstract Scope createScope(Scope currentScope);
}
