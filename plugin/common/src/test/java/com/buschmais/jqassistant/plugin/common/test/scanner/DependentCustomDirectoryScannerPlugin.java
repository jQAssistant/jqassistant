package com.buschmais.jqassistant.plugin.common.test.scanner;

import static com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;

/**
 * A s
 */
@Requires(CustomDirectoryScannerPlugin.class)
public class DependentCustomDirectoryScannerPlugin extends AbstractScannerPlugin<File, CustomDirectoryDescriptor> {

    @Override
    public boolean accepts(File item, String path, Scope scope) throws IOException {
        return CustomScope.CUSTOM.equals(scope);
    }

    @Override
    public CustomDirectoryDescriptor scan(File item, String path, Scope scope, Scanner scanner) throws IOException {
        CustomDirectoryDescriptor descriptor = scanner.getContext().peek(CustomDirectoryDescriptor.class);
        descriptor.setValue("TEST");
        return descriptor;
    }
}
