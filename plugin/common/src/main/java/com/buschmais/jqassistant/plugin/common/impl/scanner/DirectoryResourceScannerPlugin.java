package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.DirectoryResource;

public class DirectoryResourceScannerPlugin extends AbstractScannerPlugin<DirectoryResource, DirectoryDescriptor> {

    @Override
    public boolean accepts(DirectoryResource item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public DirectoryDescriptor scan(DirectoryResource item, String path, Scope scope, Scanner scanner) throws IOException {
        return scanner.getContext()
            .peek(FileResolver.class)
            .match(path, DirectoryDescriptor.class, scanner.getContext());
    }

}
