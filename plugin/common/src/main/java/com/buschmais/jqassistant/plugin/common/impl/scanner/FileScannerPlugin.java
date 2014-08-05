package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileSystemResource;

public class FileScannerPlugin extends AbstractScannerPlugin<File> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileScannerPlugin.class);

    @Override
    protected void initialize() {
    }

    @Override
    public Class<? super File> getType() {
        return File.class;
    }

    @Override
    public boolean accepts(File item, String path, Scope scope) throws IOException {
        return !item.isDirectory();
    }

    @Override
    public FileDescriptor scan(final File file, String path, Scope scope, Scanner scanner) throws IOException {
        LOGGER.info("Scanning file '{}'.", file.getAbsolutePath());
        FileDescriptor fileDescriptor = scanner.scan(new FileSystemResource() {
            @Override
            public InputStream createStream() throws IOException {
                return new BufferedInputStream(new FileInputStream(file));
            }

            @Override
            public boolean isDirectory() {
                return file.isDirectory();
            }
        }, path, scope);
        fileDescriptor.setFileName(path);
        return fileDescriptor;
    }
}
