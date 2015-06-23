package com.buschmais.jqassistant.plugin.javaee6.impl.scanner;

import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.JavaClassesDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.WebApplicationScope;

public class WarClassesScannerPlugin extends AbstractWarResourceScannerPlugin<FileResource, FileDescriptor> {

    public static final String PREFIX = "/WEB-INF/classes";

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) throws IOException {
        return WebApplicationScope.WAR.equals(scope) && path.startsWith(PREFIX);
    }

    @Override
    protected FileDescriptor scan(FileResource item, String path, JavaClassesDirectoryDescriptor classesDirectory, Scanner scanner) throws IOException {
        if (PREFIX.equals(path)) {
            return classesDirectory;
        }
        String resourcePath = path.substring(PREFIX.length());
        FileDescriptor fileDescriptor = scanner.scan(item, resourcePath, JavaScope.CLASSPATH);
        classesDirectory.getContains().add(toFileDescriptor(item, fileDescriptor, resourcePath, scanner.getContext()));
        return fileDescriptor;
    }
}
