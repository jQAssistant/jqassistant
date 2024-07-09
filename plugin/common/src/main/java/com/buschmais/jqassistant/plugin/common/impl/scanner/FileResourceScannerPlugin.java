package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.LocalFileSystemFileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FilePatternMatcher;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

public class FileResourceScannerPlugin extends AbstractScannerPlugin<FileResource, FileDescriptor> {

    public static final String PROPERTY_INCLUDE = "file.include";
    public static final String PROPERTY_EXCLUDE = "file.exclude";

    private FilePatternMatcher filePatternMatcher;

    @Override
    protected void configure() {
        getScannerContext().push(FileResolver.class, new LocalFileSystemFileResolver());
        filePatternMatcher = FilePatternMatcher.builder()
            .include(getStringProperty(PROPERTY_INCLUDE, null))
            .exclude(getStringProperty(PROPERTY_EXCLUDE, null))
            .build();
    }

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return filePatternMatcher.accepts(path);
    }

    @Override
    public FileDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        return scanner.getContext()
            .peek(FileResolver.class)
            .match(path, FileDescriptor.class, scanner.getContext());
    }

}
