package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;

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
    public Iterable<? extends FileDescriptor> scan(File file, String path, Scope scope, Scanner scanner) throws IOException {
        LOGGER.info("Scanning file '{}'.", file.getAbsolutePath());
        return scanner.scan(new BufferedInputStream(new FileInputStream(file)), path, scope);
    }
}
