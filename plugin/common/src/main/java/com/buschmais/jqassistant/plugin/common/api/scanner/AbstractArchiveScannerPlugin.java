package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.io.IOException;
import java.util.zip.ZipFile;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.ArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

/**
 * Abstract base implementation for archive scanners.
 */
public abstract class AbstractArchiveScannerPlugin extends AbstractScannerPlugin<FileResource, ArchiveDescriptor> {

    @Override
    public Class<? extends FileResource> getType() {
        return FileResource.class;
    }

    @Override
    public boolean accepts(FileResource file, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith(getExtension());
    }

    @Override
    public ArchiveDescriptor scan(FileResource file, String path, Scope currentScope, Scanner scanner) throws IOException {
        Scope zipScope = createScope(currentScope);
        ArchiveDescriptor archive = createArchive(file, path, scanner.getContext());
        ZipFile zipFile = new ZipFile(file.getFile());
        scanner.getContext().push(ArchiveDescriptor.class, archive);
        try {
            scanner.scan(zipFile, path, zipScope);
        } finally {
            scanner.getContext().pop(ArchiveDescriptor.class);
        }
        return archive;
    }

    /**
     * Return the file extension to match.
     * 
     * @return The file extension
     */
    protected abstract String getExtension();

    /**
     * Create a scope depending of the archive type.
     * 
     * @param currentScope
     *            The current scope.
     * @return The new scope.
     */
    protected abstract Scope createScope(Scope currentScope);

    /**
     * Create descriptor which represents the archive type.
     * 
     * @param file
     *            The file resource.
     * @param path
     *            The path.
     * @param scannerContext
     *            The scanner context.
     * @return The descriptor.
     */
    protected abstract ArchiveDescriptor createArchive(FileResource file, String path, ScannerContext scannerContext);
}
