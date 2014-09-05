package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.core.store.api.type.FileContainerDescriptor;
import com.buschmais.jqassistant.plugin.common.impl.scanner.AbstractDirectoryScannerPlugin;

public class CustomDirectoryScannerPlugin extends AbstractDirectoryScannerPlugin<CustomDirectory> {

    @Override
    protected void initialize() {
    }

    @Override
    protected File getDirectory(CustomDirectory item) {
        return item.getDirectory();
    }

    @Override
    public Class<? super CustomDirectory> getType() {
        return CustomDirectory.class;
    }

    @Override
    public boolean accepts(CustomDirectory item, String path, Scope scope) throws IOException {
        return true;
    }

    @Override
    protected FileContainerDescriptor getContainerDescriptor(CustomDirectory container) {
        return getStore().create(CustomDirectoryDescriptor.class);
    }

    @Override
    protected Scope createScope(Scope currentScope) {
        return currentScope;
    }
}
