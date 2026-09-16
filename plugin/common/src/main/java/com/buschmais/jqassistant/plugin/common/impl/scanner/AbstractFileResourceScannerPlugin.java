package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.LocalDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FilePatternMatcher;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.LocalResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Resource;

public abstract class AbstractFileResourceScannerPlugin<R extends Resource, D extends FileDescriptor> extends AbstractScannerPlugin<R, D> {

    public static final String PROPERTY_INCLUDE = "file.include";
    public static final String PROPERTY_EXCLUDE = "file.exclude";

    private FilePatternMatcher filePatternMatcher;

    @Override
    protected final void configure() {
        filePatternMatcher = FilePatternMatcher.builder()
            .include(getStringProperty(PROPERTY_INCLUDE, null))
            .exclude(getStringProperty(PROPERTY_EXCLUDE, null))
            .build();
    }

    @Override
    public final boolean accepts(R item, String location, Scope scope) throws IOException {
        return filePatternMatcher.accepts(location);
    }

    @Override
    public final D scan(R resource, String location, Scope scope, Scanner scanner) throws IOException {
        D fileDescriptor = scanner.getContext()
            .peek(FileResolver.class)
            .match(location, getDescriptorType(), scanner.getContext());
        if (resource instanceof LocalResource) {
            scanner.getContext()
                .getStore()
                .addDescriptorType(fileDescriptor, LocalDescriptor.class);
        }
        return fileDescriptor;
    }
}
