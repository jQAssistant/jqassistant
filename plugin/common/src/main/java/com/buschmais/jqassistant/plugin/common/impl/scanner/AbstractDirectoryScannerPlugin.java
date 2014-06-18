package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.scanner.api.iterable.AggregatingIterable;
import com.buschmais.jqassistant.core.scanner.api.iterable.MappingIterable;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;

public abstract class AbstractDirectoryScannerPlugin<I> extends AbstractScannerPlugin<I> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDirectoryScannerPlugin.class);

    @Override
    protected void initialize() {
    }

    @Override
    public Iterable<? extends FileDescriptor> scan(final I item, String path, final Scope scope, final Scanner scanner) throws IOException {
        final File directory = getDirectory(item);
        final List<File> files = new ArrayList<>();
        new DirectoryWalker<File>() {

            @Override
            protected boolean handleDirectory(File subdirectory, int depth, Collection<File> results) throws IOException {
                results.add(subdirectory);
                return true;
            }

            @Override
            protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
                results.add(file);
            }

            public void scan(File directory) throws IOException {
                super.walk(directory, files);
            }
        }.scan(directory);
        LOGGER.info("Scanning directory '{}' [{} entries].", directory.getAbsolutePath(), files.size());
        beforeDirectory(item, path);
        MappingIterable<File, Iterable<? extends FileDescriptor>> fileDescriptors = new MappingIterable<File, Iterable<? extends FileDescriptor>>(files) {
            @Override
            protected Iterable<? extends FileDescriptor> map(File file) throws IOException {
                String relativePath;
                if (file.equals(directory)) {
                    relativePath = "/";
                } else {
                    String filePath = file.getAbsolutePath();
                    String directoryPath = directory.getAbsolutePath();
                    relativePath = filePath.substring(directoryPath.length()).replace(File.separator, "/");
                }
                return scanner.scan(file, relativePath, createScope(scope));
            }
        };
        return afterDirectory(item, new AggregatingIterable<>(fileDescriptors));
    }

    protected abstract File getDirectory(I item);

    protected abstract Scope createScope(Scope currentScope);

    protected abstract void beforeDirectory(I item, String path);

    protected abstract Iterable<? extends FileDescriptor> afterDirectory(I item, Iterable<? extends FileDescriptor> descriptors);

}
