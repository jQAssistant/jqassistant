package com.buschmais.jqassistant.plugin.java.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.ArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractArchiveScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;

public class JarScannerPlugin extends AbstractArchiveScannerPlugin {

    @Override
    protected String getExtension() {
        return ".jar";
    }

    @Override
    protected Scope createScope(Scope currentScope) {
        return JavaScope.CLASSPATH;
    }

    @Override
    protected ArchiveDescriptor beforeArchive(String path, Scope scope) {
        ArchiveDescriptor archiveDescriptor = getStore().create(ArchiveDescriptor.class);
        archiveDescriptor.setFileName(path);
        return archiveDescriptor;
    }

}
