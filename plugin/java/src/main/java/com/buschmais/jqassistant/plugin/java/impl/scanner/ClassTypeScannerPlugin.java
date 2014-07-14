package com.buschmais.jqassistant.plugin.java.impl.scanner;

import static com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope.CLASSPATH;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.StreamFactory;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractScannerPlugin;

public class ClassTypeScannerPlugin extends AbstractScannerPlugin<Class<?>> {
    @Override
    protected void initialize() {
    }

    @Override
    public Class<? super Class<?>> getType() {
        return Class.class;
    }

    @Override
    public boolean accepts(Class<?> item, String path, Scope scope) throws IOException {
        return CLASSPATH.equals(scope);
    }

    @Override
    public Iterable<? extends FileDescriptor> scan(final Class<?> item, String path, Scope scope, Scanner scanner) throws IOException {
        final String resource = "/" + item.getName().replace('.', '/') + ".class";
        return scanner.scan(new StreamFactory() {
            @Override
            public InputStream createStream() throws IOException {
                return new BufferedInputStream(item.getResourceAsStream(resource));
            }
        }, resource, scope);
    }
}
