package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractResourceScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.LocalFileSystemFileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.LocalFileResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scanner plugin for instances of {@link File}.
 */
public class FileScannerPlugin extends AbstractResourceScannerPlugin<File, FileDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileScannerPlugin.class);

    @Override
    protected void configure() {
        getScannerContext().push(FileResolver.class, new LocalFileSystemFileResolver());
    }

    @Override
    public boolean accepts(File file, String location, Scope scope) throws IOException {
        return !file.isDirectory();
    }

    @Override
    public FileDescriptor scan(final File file, String location, Scope scope, Scanner scanner) throws IOException {
        String fileName = PathNormalizer.normalizeFileName(file, scanner.getContext());
        LOGGER.debug("Scanning '{}'.", fileName);
        try (FileResource fileResource = new LocalFileResource(file)) {
            return scanner.scan(fileResource, fileName, scope);
        }
    }
}
