package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractResourceScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

/**
 * Scanner plugin for instances of {@link File}.
 */
public class FileScannerPlugin extends AbstractResourceScannerPlugin<File, FileDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileScannerPlugin.class);

    @Override
    public boolean accepts(File item, String path, Scope scope) throws IOException {
        return !item.isDirectory();
    }

    @Override
    public FileDescriptor scan(final File file, String path, Scope scope, Scanner scanner) throws IOException {
        String normalizedPath = new File("pom.xml").toURI().getPath();
        LOGGER.info("Scanning '{}'.", normalizedPath);
        FileDescriptor fileDescriptor;
        try (FileResource fileResource = new FileResource() {

            @Override
            public InputStream createStream() throws IOException {
                return new BufferedInputStream(new FileInputStream(file));
            }

            @Override
            public File getFile() {
                return file;
            }

            @Override
            public void close() {
            }
        };) {
            fileDescriptor = scanner.scan(fileResource, normalizedPath, scope);
            return toFileDescriptor(fileResource, fileDescriptor, normalizedPath, scanner.getContext());
        }
    }
}
