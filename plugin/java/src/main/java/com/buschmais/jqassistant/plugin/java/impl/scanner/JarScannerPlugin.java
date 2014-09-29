package com.buschmais.jqassistant.plugin.java.impl.scanner;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.ArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractArchiveScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.JarArchiveDescriptor;
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
    protected ArchiveDescriptor createArchive(File file, String path, ScannerContext scannerContext) {
        return scannerContext.getStore().create(JarArchiveDescriptor.class);
    }
}
