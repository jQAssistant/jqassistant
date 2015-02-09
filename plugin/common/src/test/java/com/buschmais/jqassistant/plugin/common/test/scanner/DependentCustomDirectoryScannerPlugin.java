package com.buschmais.jqassistant.plugin.common.test.scanner;

import static com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;

@Requires(CustomDirectoryDescriptor.class)
public class DependentCustomDirectoryScannerPlugin extends AbstractScannerPlugin<File, DependentCustomDirectoryDescriptor> {

    @Override
    public boolean accepts(File item, String path, Scope scope) throws IOException {
        return CustomScope.CUSTOM.equals(scope);
    }

    @Override
    public DependentCustomDirectoryDescriptor scan(File item, String path, Scope scope, Scanner scanner) throws IOException {
        CustomDirectoryDescriptor descriptor = scanner.getContext().peek(CustomDirectoryDescriptor.class);
        DependentCustomDirectoryDescriptor dependentCustomDirectoryDescriptor = scanner.getContext().getStore()
                .addDescriptorType(descriptor, DependentCustomDirectoryDescriptor.class);
        dependentCustomDirectoryDescriptor.setValue("TEST");
        return dependentCustomDirectoryDescriptor;
    }
}
