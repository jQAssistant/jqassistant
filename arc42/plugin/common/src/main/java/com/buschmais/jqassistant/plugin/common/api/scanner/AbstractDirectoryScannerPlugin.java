package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.shared.io.FileNameNormalizer;
import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractDirectoryResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Resource;
import com.buschmais.jqassistant.plugin.common.impl.scanner.BufferedFileResource;

import lombok.extern.slf4j.Slf4j;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static java.nio.file.Files.walkFileTree;

/**
 * Abstract base implementation for directory scanners.
 */
@Slf4j
public abstract class AbstractDirectoryScannerPlugin<D extends DirectoryDescriptor> extends AbstractContainerScannerPlugin<File, File, D> {

    public static final String PROPERTY_FOLLOW_SYMLINKS = "directory.follow-symbolic-links";

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
        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (!directoryPath.equals(dir)) {
                    files.add(dir.toFile());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                files.add(file.toFile());
                return FileVisitResult.CONTINUE;
            }
        };
        boolean followSymlinks = getBooleanProperty(PROPERTY_FOLLOW_SYMLINKS, false);
        if (followSymlinks) {
            log.info("Following symbolic links.");
            walkFileTree(directoryPath, EnumSet.of(FOLLOW_LINKS), Integer.MAX_VALUE, visitor);
        } else {
            walkFileTree(directoryPath, visitor);
        }
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
        return FileNameNormalizer.normalize(path);
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
