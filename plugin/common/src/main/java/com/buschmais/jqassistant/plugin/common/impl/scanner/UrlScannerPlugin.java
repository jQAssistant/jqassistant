package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;

public class UrlScannerPlugin extends AbstractScannerPlugin<URL> {
    @Override
    protected void initialize() {
    }

    @Override
    public Class<? super URL> getType() {
        return URL.class;
    }

    @Override
    public boolean accepts(URL item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    public Iterable<? extends FileDescriptor> scan(URL item, String path, Scope scope, Scanner scanner) throws IOException {
        return scanner.scan(new BufferedInputStream(item.openStream()), item.getPath(), scope);
    }
}
