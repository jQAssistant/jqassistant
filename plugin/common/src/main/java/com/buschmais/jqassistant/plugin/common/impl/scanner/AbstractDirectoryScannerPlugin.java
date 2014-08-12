package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

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
        final Path directoryPath = directory.toPath();
        final List<File> files = new ArrayList<>();
        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (!directoryPath.equals(dir)) {
                    files.add(dir.toFile());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                files.add(file.toFile());
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(directoryPath, visitor);
        LOGGER.info("Scanning directory '{}' [{} entries].", directory.getAbsolutePath(), files.size());
        return files;
    }

    @Override
    protected String getRelativePath(I container, File entry) {
        File directory = getDirectory(container);
        return getDirectoryPath(directory, entry);
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
