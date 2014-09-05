package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.BufferedInputStream;
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

import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Directory;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Entry;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.File;

public abstract class AbstractDirectoryScannerPlugin<I> extends AbstractContainerScannerPlugin<I, java.io.File> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDirectoryScannerPlugin.class);

    @Override
    protected void initialize() {
    }

    @Override
    protected Iterable<? extends java.io.File> getEntries(I container) throws IOException {
        final java.io.File directory = getDirectory(container);
        final Path directoryPath = directory.toPath();
        final List<java.io.File> files = new ArrayList<>();
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
    protected String getRelativePath(I container, java.io.File entry) {
        java.io.File directory = getDirectory(container);
        return getDirectoryPath(directory, entry);
    }

    @Override
    protected Entry getEntry(I container, final java.io.File entry) {
        if (entry.isDirectory()) {
            return new Directory() {
            };
        } else {
            return new File() {
                @Override
                public InputStream createStream() throws IOException {
                    return new BufferedInputStream(new FileInputStream(entry));
                }
            };
        }
    }

    protected abstract java.io.File getDirectory(I item);

}
