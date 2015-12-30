package com.buschmais.jqassistant.plugin.common.api.scanner;

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

import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractDirectoryResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Resource;
import com.buschmais.jqassistant.plugin.common.impl.scanner.BufferedFileResource;

/**
 * Abstract base implementation for directory scanners.
 */
public abstract class AbstractDirectoryScannerPlugin<D extends DirectoryDescriptor, P extends ScannerPlugin<File, D>>
        extends AbstractContainerScannerPlugin<File, File, D, P> {

    @Override
    public Class<? extends File> getType() {
        return File.class;
    }

    @Override
    public Class<D> getDescriptorType() {
        return getTypeParameter(AbstractDirectoryScannerPlugin.class, 0);
    }

    @Override
    public boolean accepts(File item, String path, Scope scope) throws IOException {
        return item.isDirectory() && getRequiredScope().equals(scope);
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

    /**
     * Return the scope the plugin expects for execution.
     *
     * @return The scope.
     */
    protected abstract Scope getRequiredScope();

    @Override
    protected String getContainerPath(File container, String path) {
        return slashify(path);
    }

    @Override
    protected String getRelativePath(File container, File entry) {
        return getDirectoryPath(container, entry);
    }

    @Override
    protected Resource getEntry(File container, final File entry) {
        if (entry.isDirectory()) {
            return new DirectoryResource(entry.getPath());
        } else {
            return new BufferedFileResource(new FileResource(entry));
        }
    }

    /**
     * A directory resource.
     */
    private static class DirectoryResource extends AbstractDirectoryResource {
        public DirectoryResource(String entryPath) {
            super(entryPath);
        }
    }

    /**
     * A file resource.
     */
    private static class FileResource implements com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource {
        private final File entry;

        public FileResource(File entry) {
            this.entry = entry;
        }

        @Override
        public InputStream createStream() throws IOException {
            return new FileInputStream(entry);
        }

        @Override
        public File getFile() {
            return entry;
        }

        @Override
        public void close() {
        }
    }
}
