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
import com.buschmais.jqassistant.core.store.api.type.DirectoryDescriptor;
import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;

public abstract class AbstractDirectoryScannerPlugin<I> extends AbstractScannerPlugin<I> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDirectoryScannerPlugin.class);

    @Override
    protected void initialize() {
    }

    @Override
    public FileDescriptor scan(final I item, String path, final Scope scope, final Scanner scanner) throws IOException {
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
        DirectoryDescriptor directoryDescriptor = beforeDirectory(item, path);
        for (File file : files) {
            String relativePath;
            if (file.equals(directory)) {
                relativePath = "/";
            } else {
                String filePath = file.getAbsolutePath();
                String directoryPath = directory.getAbsolutePath();
                relativePath = filePath.substring(directoryPath.length()).replace(File.separator, "/");
            }
            FileDescriptor fileDescriptor = scanner.scan(file, relativePath, createScope(scope));
            if (fileDescriptor != null) {
                directoryDescriptor.getContains().add(fileDescriptor);
            }
        }
        return directoryDescriptor;
    }

    protected abstract File getDirectory(I item);

    protected abstract Scope createScope(Scope currentScope);

    protected abstract DirectoryDescriptor beforeDirectory(I item, String path);

}
