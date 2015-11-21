package com.buschmais.jqassistant.plugin.javaee6.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractZipArchiveScannerPlugin;
import com.buschmais.jqassistant.plugin.java.api.scanner.ArtifactScopedTypeResolver;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeResolver;
import com.buschmais.jqassistant.plugin.javaee6.api.model.WebApplicationArchiveDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.model.WebApplicationDescriptor;
import com.buschmais.jqassistant.plugin.javaee6.api.scanner.WebApplicationScope;

public class WebApplicationArchiveScannerPlugin extends AbstractZipArchiveScannerPlugin<WebApplicationArchiveDescriptor> {

    @Override
    protected String getExtension() {
        return ".war";
    }

    @Override
    protected Scope createScope(Scope currentScope, WebApplicationArchiveDescriptor archiveDescriptor, ScannerContext scannerContext) {
        TypeResolver typeResolver = new ArtifactScopedTypeResolver(archiveDescriptor);
        scannerContext.push(TypeResolver.class, typeResolver);
        scannerContext.push(WebApplicationDescriptor.class, archiveDescriptor);
        return WebApplicationScope.WAR;
    }

    @Override
    protected void destroyScope(ScannerContext scannerContext) {
        scannerContext.pop(WebApplicationDescriptor.class);
        scannerContext.pop(TypeResolver.class);
    }

}
