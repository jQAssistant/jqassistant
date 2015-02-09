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
public abstract class AbstractArchiveScannerPlugin<D extends ArchiveDescriptor> extends AbstractScannerPlugin<FileResource, D> {

    @Override
    public Class<? extends FileResource> getType() {
        return FileResource.class;
    }

    @Override
    public Class<? extends D> getDescriptorType() {
        return getTypeParameter(AbstractArchiveScannerPlugin.class, 0);
    }

    @Override
    public boolean accepts(FileResource file, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith(getExtension());
    }

    @Override
    public D scan(FileResource file, String path, Scope currentScope, Scanner scanner) throws IOException {
        D archive = createArchive(file, path, scanner.getContext());
        ZipFile zipFile = new ZipFile(file.getFile());
        scanner.getContext().push(ArchiveDescriptor.class, archive);
        Scope zipScope = createScope(currentScope, archive, scanner.getContext());
        try {
            scanner.scan(zipFile, path, zipScope);
        } finally {
            destroyScope(scanner.getContext());
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
     * @param archiveDescriptor
     *            The created archive descriptor.
     * @param scannerContext
     *            The scanner context
     * @return The new scope.
     */
    protected abstract Scope createScope(Scope currentScope, D archiveDescriptor, ScannerContext scannerContext);

    /**
     * Destroy the scope.
     *
     * @param scannerContext
     *            The scanner context
     * @return The new scope.
     */
    protected abstract void destroyScope(ScannerContext scannerContext);

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
    protected abstract D createArchive(FileResource file, String path, ScannerContext scannerContext);
}
