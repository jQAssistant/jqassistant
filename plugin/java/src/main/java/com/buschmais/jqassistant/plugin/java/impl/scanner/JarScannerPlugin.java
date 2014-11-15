package com.buschmais.jqassistant.plugin.java.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractArchiveScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import com.buschmais.jqassistant.plugin.java.api.model.JarArchiveDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolverBuilder;

public class JarScannerPlugin extends AbstractArchiveScannerPlugin<JarArchiveDescriptor> {

    @Override
    protected String getExtension() {
        return ".jar";
    }

    @Override
    protected Scope createScope(Scope currentScope, JarArchiveDescriptor archiveDescriptor, ScannerContext scannerContext) {
        scannerContext.push(TypeResolver.class, TypeResolverBuilder.createTypeResolver(scannerContext));
        return JavaScope.CLASSPATH;
    }

    @Override
    protected void destroyScope(ScannerContext scannerContext) {
        scannerContext.pop(TypeResolver.class);
    }

    @Override
    protected JarArchiveDescriptor createArchive(FileResource file, String path, ScannerContext scannerContext) {
        return scannerContext.getStore().create(JarArchiveDescriptor.class);
    }
}
