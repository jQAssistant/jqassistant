package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

/**
 * Default type resolver, does not consider any scopes, i.e. provides a global
 * resolution.
 */
class DefaultTypeResolver extends AbstractTypeResolver {

    @Override
    protected TypeDescriptor findInArtifact(String fullQualifiedName, ScannerContext context) {
        return context.getStore().find(TypeDescriptor.class, fullQualifiedName);
    }

    @Override
    protected TypeDescriptor findInDependencies(String fullQualifiedName, ScannerContext context) {
        return context.getStore().find(TypeDescriptor.class, fullQualifiedName);
    }

    @Override
    protected <T extends TypeDescriptor> void removeRequiredType(String fqn, T typeDescriptor) {
    }

    @Override
    protected void addRequiredType(String fqn, TypeDescriptor typeDescriptor) {
    }

    @Override
    protected void addContainedType(String fqn, TypeDescriptor typeDescriptor) {
    }
}
