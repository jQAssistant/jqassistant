package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.plugin.common.api.scanner.FileSystemResource;

public abstract class AbstractDirectoryScannerPlugin<I> extends AbstractContainerScannerPlugin<I, File> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDirectoryScannerPlugin.class);

    @Override
    protected void initialize() {
    }

    @Override
    protected Iterable<? extends File> getEntries(I container) throws IOException {
        final File directory = getDirectory(container);
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
        return files;
    }

    @Override
    protected String getRelativePath(I container, File entry) {
        File directory = getDirectory(container);
        String relativePath;
        if (entry.equals(directory)) {
            relativePath = "/";
        } else {
            String filePath = entry.getAbsolutePath();
            String directoryPath = directory.getAbsolutePath();
            relativePath = filePath.substring(directoryPath.length()).replace(File.separator, "/");
        }
        return relativePath;
    }

    @Override
    protected FileSystemResource getFileResource(I container, final File entry) {
        return new FileSystemResource() {
            @Override
            public InputStream createStream() throws IOException {
                return new BufferedInputStream(new FileInputStream(entry));
            }

            @Override
            public boolean isDirectory() {
                return entry.isDirectory();
            }
        };
    }

    protected abstract File getDirectory(I item);

}
