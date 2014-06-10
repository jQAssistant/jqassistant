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

public class DirectoryScannerPlugin extends AbstractScannerPlugin<File> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryScannerPlugin.class);

    @Override
    protected void initialize() {
    }

    @Override
    public Class<File> getType() {
        return File.class;
    }

    @Override
    public boolean accepts(File item, String path, Scope scope) throws IOException {
        return item.isDirectory() && path == null;
    }

    @Override
    public Iterable<FileDescriptor> scan(final File directory, String path, final Scope scope, final Scanner scanner) throws IOException {
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
                return scanner.scan(file, relativePath, scope);
            }
        };
        return new AggregatingIterable<>(fileDescriptors);
    }
}
