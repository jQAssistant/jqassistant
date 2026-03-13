package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ZipArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.ZipFileResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base implementation for archive scanners.
 */
@Requires(FileDescriptor.class)
public abstract class AbstractZipArchiveScannerPlugin<D extends ZipArchiveDescriptor>
        extends AbstractScannerPlugin<FileResource, D> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractZipArchiveScannerPlugin.class);

    @Override
    public Class<? extends FileResource> getType() {
        return FileResource.class;
    }

    @Override
    public Class<D> getDescriptorType() {
        return getTypeParameter(AbstractZipArchiveScannerPlugin.class, 0);
    }

    @Override
    public boolean accepts(FileResource file, String path, Scope scope) throws IOException {
        return path.toLowerCase().endsWith(getExtension());
    }

    @Override
    public D scan(FileResource file, String path, Scope currentScope, Scanner scanner) throws IOException {
        ScannerContext scannerContext = scanner.getContext();
        FileDescriptor fileDescriptor = scannerContext.getCurrentDescriptor();
        D archive = scannerContext.getStore().addDescriptorType(fileDescriptor, getDescriptorType());
        scannerContext.push(ZipArchiveDescriptor.class, archive);
        Scope archiveScope = createScope(currentScope, archive, scannerContext);

        try (ZipFileResource zipFile = new ZipFileResource(file.getFile())) {
            scanner.scan(zipFile, path, archiveScope);
            archive.setValid(true);
        } catch (IOException e) {
            LOGGER.warn("Cannot read ZIP file '" + path + "'.", e);
            archive.setValid(false);
        }
        finally {
            destroyScope(scannerContext);
            scannerContext.pop(ZipArchiveDescriptor.class);
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
     */
    protected abstract void destroyScope(ScannerContext scannerContext);
}
