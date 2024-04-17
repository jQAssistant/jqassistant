package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.IOException;
import java.io.InputStream;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractVirtualFileResource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;

public class ClassTypeScannerPlugin extends AbstractScannerPlugin<Class<?>, ClassFileDescriptor> {

    @Override
    public boolean accepts(Class<?> item, String path, Scope scope) throws IOException {
        return CLASSPATH.equals(scope);
    }

    @Override
    public ClassFileDescriptor scan(final Class<?> item, String path, Scope scope, Scanner scanner) throws IOException {
        final String fileName = "/" + item.getName()
            .replace('.', '/') + ".class";
        FileResource fileResource = new AbstractVirtualFileResource() {
            @Override
            public InputStream createStream() {
                return item.getResourceAsStream(fileName);
            }

            @Override
            protected String getRelativePath() {
                return path;
            }
        };
        return scanner.scan(fileResource, fileName, scope);
    }
}
