package com.buschmais.jqassistant.plugin.javaee6.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin.Requires;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractResourceScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.Resource;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.WebApplicationScope;

@Requires(FileDescriptor.class)
public class WarClassesResourceScannerPlugin extends AbstractResourceScannerPlugin<Resource, FileDescriptor> {

    public static final String PREFIX = "/WEB-INF/classes";

    @Override
    public boolean accepts(Resource item, String path, Scope scope) throws IOException {
        return WebApplicationScope.WAR.equals(scope) && path.startsWith(PREFIX + "/");
    }

    @Override
    public FileDescriptor scan(Resource item, String path, Scope scope, Scanner scanner) throws IOException {
        String resourcePath = path.substring(PREFIX.length());
        FileDescriptor fileDescriptor = scanner.scan(item, resourcePath, JavaScope.CLASSPATH);
        return fileDescriptor != null ? fileDescriptor : scanner.getContext().peek(FileDescriptor.class);
    }
}
