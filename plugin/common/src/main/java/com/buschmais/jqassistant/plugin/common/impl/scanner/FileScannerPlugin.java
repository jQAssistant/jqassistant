package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.File;

public class FileScannerPlugin extends AbstractScannerPlugin<java.io.File> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileScannerPlugin.class);

    private static final int MAX_BUFFER_SIZE = 1024 * 1024; // One MB

    @Override
    protected void initialize() {
    }

    @Override
    public Class<? super java.io.File> getType() {
        return java.io.File.class;
    }

    @Override
    public boolean accepts(java.io.File item, String path, Scope scope) throws IOException {
        return !item.isDirectory();
    }

    @Override
    public FileDescriptor scan(final java.io.File file, String path, Scope scope, Scanner scanner) throws IOException {
        LOGGER.info("Scanning file '{}'.", file.getAbsolutePath());
        FileDescriptor fileDescriptor = scanner.scan(new File() {

            @Override
            public InputStream createStream() throws IOException {
                long length = file.length();
                long bufferSize = length <= MAX_BUFFER_SIZE ? length : MAX_BUFFER_SIZE;
                return new BufferedInputStream(new FileInputStream(file), (int) bufferSize);
            }

        }, path, scope);
        if (fileDescriptor != null) {
            fileDescriptor.setFileName(path);
        }
        return fileDescriptor;
    }
}
