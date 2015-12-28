package com.buschmais.jqassistant.plugin.java.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractZipArchiveScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.model.JarArchiveDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.ArtifactScopedTypeResolver;
import com.buschmais.jqassistant.plugin.java.api.scanner.JavaScope;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;

public class JarScannerPlugin extends AbstractZipArchiveScannerPlugin<JarArchiveDescriptor, JarScannerPlugin> {
    @Override
    protected JarScannerPlugin getThis() {
        return this;
    }

    @Override
    protected String getExtension() {
        return ".jar";
    }

    @Override
    protected Scope createScope(Scope currentScope, JarArchiveDescriptor archiveDescriptor, ScannerContext scannerContext) {
        scannerContext.push(TypeResolver.class, new ArtifactScopedTypeResolver(archiveDescriptor));
        return JavaScope.CLASSPATH;
    }

    @Override
    protected void destroyScope(ScannerContext scannerContext) {
        scannerContext.pop(TypeResolver.class);
    }

}
