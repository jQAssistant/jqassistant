package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

class DefaultTypeResolver extends AbstractTypeResolver {

    @Override
    protected TypeDescriptor findType(String fullQualifiedName, ScannerContext context) {
        return context.getStore().find(TypeDescriptor.class, fullQualifiedName);
    }

    @Override
    protected <T extends TypeDescriptor> void removeRequiredType(T typeDescriptor) {
    }

    @Override
    protected void addRequiredType(TypeDescriptor typeDescriptor) {
    }
}
