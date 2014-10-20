package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;

import java.io.IOException;
import java.io.InputStream;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractFileResource;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractResourceScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;

public class ClassTypeScannerPlugin extends AbstractResourceScannerPlugin<Class<?>, ClassFileDescriptor> {

    @Override
    public boolean accepts(Class<?> item, String path, Scope scope) throws IOException {
        return CLASSPATH.equals(scope);
    }

    @Override
    public ClassFileDescriptor scan(final Class<?> item, String path, Scope scope, Scanner scanner) throws IOException {
        final String resource = "/" + item.getName().replace('.', '/') + ".class";
        ClassFileDescriptor fileDescriptor = scanner.scan(new AbstractFileResource() {
            @Override
            public InputStream createStream() throws IOException {
                return item.getResourceAsStream(resource);
            }
        }, resource, scope);
        return toFileDescriptor(fileDescriptor, resource, scanner.getContext());
    }
}
