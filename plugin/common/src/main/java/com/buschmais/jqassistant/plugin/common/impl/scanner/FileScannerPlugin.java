package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

/**
 * Scanner plugin for instances of {@link File}.
 */
public class FileScannerPlugin extends AbstractResourceScannerPlugin<File, FileDescriptor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileScannerPlugin.class);

    private static final int MAX_BUFFER_SIZE = 1024 * 1024; // One MB

    @Override
    public boolean accepts(File item, String path, Scope scope) throws IOException {
        return !item.isDirectory();
    }

    @Override
    public FileDescriptor scan(final File file, String path, Scope scope, Scanner scanner) throws IOException {
        LOGGER.info("Scanning '{}'.", file.getAbsolutePath());
        FileDescriptor fileDescriptor;
        try (FileResource fileResource = new FileResource() {

            @Override
            public InputStream createStream() throws IOException {
                long length = file.length();
                long bufferSize = length <= MAX_BUFFER_SIZE ? length : MAX_BUFFER_SIZE;
                return new BufferedInputStream(new FileInputStream(file), (int) bufferSize);
            }

            @Override
            public File getFile() {
                return file;
            }

            @Override
            public void close() {
            }
        };) {
            fileDescriptor = scanner.scan(fileResource, path, scope);
        }
        return toFileDescriptor(fileDescriptor, path, scanner.getContext());
    }
}
