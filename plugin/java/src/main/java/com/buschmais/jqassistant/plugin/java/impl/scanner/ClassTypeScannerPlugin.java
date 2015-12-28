package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;

import java.io.IOException;
import java.io.InputStream;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.AbstractFileResource;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;

public class ClassTypeScannerPlugin extends AbstractScannerPlugin<Class<?>, ClassFileDescriptor, ClassTypeScannerPlugin> {

    @Override
    protected ClassTypeScannerPlugin getThis() {
        return this;
    }

    @Override
    protected boolean doAccepts(Class<?> item, String path, Scope scope) throws IOException {
        return CLASSPATH.equals(scope);
    }

    @Override
    public ClassFileDescriptor scan(final Class<?> item, String path, Scope scope, Scanner scanner) throws IOException {
        final String fileName = "/" + item.getName().replace('.', '/') + ".class";
        AbstractFileResource fileResource = new AbstractFileResource() {
            @Override
            public InputStream createStream() throws IOException {
                return item.getResourceAsStream(fileName);
            }
        };
        return scanner.scan(fileResource, fileName, scope);
    }
}
