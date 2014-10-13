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

import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractVirtualDirectory;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.VirtualEntry;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.VirtualFile;

public abstract class AbstractDirectoryScannerPlugin extends AbstractContainerScannerPlugin<File, File> {

    @Override
    public Class<? extends File> getType() {
        return File.class;
    }

    @Override
    public boolean accepts(File item, String path, Scope scope) throws IOException {
        return item.isDirectory() && getScope().equals(scope);
    }

    @Override
    protected Scope createScope(Scope currentScope) {
        return currentScope;
    }

    @Override
    protected Iterable<? extends File> getEntries(File container) throws IOException {
        final Path directoryPath = container.toPath();
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
        return files;
    }

    @Override
    protected String getRelativePath(File container, File entry) {
        return getDirectoryPath(container, entry);
    }

    @Override
    protected VirtualEntry getEntry(File container, final File entry) {
        if (entry.isDirectory()) {
            return new AbstractVirtualDirectory() {
            };
        } else {
            return new VirtualFile() {
                @Override
                public InputStream createStream() throws IOException {
                    return new BufferedInputStream(new FileInputStream(entry));
                }

                @Override
                public File getFile() {
                    return entry;
                }

                @Override
                public void close() {
                }
            };
        }
    }

    protected abstract Scope getScope();
}
