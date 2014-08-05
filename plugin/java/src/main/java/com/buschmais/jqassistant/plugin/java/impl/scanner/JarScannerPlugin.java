package com.buschmais.jqassistant.plugin.java.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractArchiveScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;

public class JarScannerPlugin extends AbstractArchiveScannerPlugin {

    @Override
    protected String getExtension() {
        return ".jar";
    }

    @Override
    protected FileDescriptor afterArchive(FileDescriptor fileDescriptor) {
        return fileDescriptor;
    }

    @Override
    protected Scope createScope(Scope currentScope) {
        return JavaScope.CLASSPATH;
    }
}
