package com.buschmais.jqassistant.scm.maven.integration.plugin;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.scanner.ClassFileScannerPlugin;

@Requires(ClassFileDescriptor.class)
public class CustomScannerPlugin extends AbstractScannerPlugin<FileResource, CustomDescriptor> {

    private static final String PROPERTY_VALUE = "custom.scan.value";

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomScannerPlugin.class);

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return path.endsWith(".class");
    }

    @Override
    public CustomDescriptor scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {
        ClassFileDescriptor descriptor = scanner.getContext().peek(ClassFileDescriptor.class);
        String value = (String) getProperties().get(PROPERTY_VALUE);
        LOGGER.info("Using custom plugin to scan " + path + ", setting value to " + value);
        CustomDescriptor customDescriptor = scanner.getContext().getStore().migrate(descriptor, CustomDescriptor.class);
        customDescriptor.setValue(value);
        return customDescriptor;
    }
}
