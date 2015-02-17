package com.buschmais.jqassistant.plugin.common.test.scanner;

import static com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.test.scanner.model.DependentDirectoryDescriptor;

/**
 * A dependent scanner plugin.
 */
@Requires(DirectoryDescriptor.class)
public class DependentDirectoryScannerPlugin extends AbstractScannerPlugin<File, DependentDirectoryDescriptor> {

    @Override
    public boolean accepts(File item, String path, Scope scope) throws IOException {
        return DefaultScope.NONE.equals(scope);
    }

    @Override
    public DependentDirectoryDescriptor scan(File item, String path, Scope scope, Scanner scanner) throws IOException {
        DirectoryDescriptor descriptor = scanner.getContext().peek(DirectoryDescriptor.class);
        DependentDirectoryDescriptor dependentDirectoryDescriptor = scanner.getContext().getStore()
                .addDescriptorType(descriptor, DependentDirectoryDescriptor.class);
        dependentDirectoryDescriptor.setValue("TEST");
        return dependentDirectoryDescriptor;
    }
}
