package com.buschmais.jqassistant.plugin.javaee6.impl.scanner;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractDirectoryScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractResourceScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Resource;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.WebApplicationScope;

/**
 *
 */
public class AbstractWarClassesResourceScannerPlugin<R extends Resource, F extends FileDescriptor> extends AbstractResourceScannerPlugin<R, F> {

    public static final String CLASSES_DIRECTORY = "/WEB-INF/classes";

    @Override
    public Class<R> getType() {
        return getTypeParameter(AbstractWarClassesResourceScannerPlugin.class, 0);
    }

    @Override
    public Class<F> getDescriptorType() {
        return getTypeParameter(AbstractWarClassesResourceScannerPlugin.class, 1);
    }

    @Override
    public boolean accepts(R item, String path, Scope scope) throws IOException {
        return WebApplicationScope.WAR.equals(scope) && path.startsWith(CLASSES_DIRECTORY + "/");
    }

    @Override
    public F scan(R item, String path, Scope scope, Scanner scanner) throws IOException {
        String resourcePath = path.substring(CLASSES_DIRECTORY.length());
        F resourceDescriptor = scanner.getContext().getCurrentDescriptor();
        return scanner.scan(item, resourceDescriptor, resourcePath, JavaScope.CLASSPATH);
    }

}
