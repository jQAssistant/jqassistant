package com.buschmais.jqassistant.core.scanner.impl.resolver;

import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.core.store.api.Store;

public class TypeDescriptorResolver extends AbstractDescriptorResolver<PackageDescriptor, TypeDescriptor> {

    public TypeDescriptorResolver(Store store, PackageDescriptorResolver parentResolver) {
        super(store, parentResolver);
    }

    @Override
    public TypeDescriptor create(PackageDescriptor parent, String name) {
        TypeDescriptor typeDescriptor = getStore().createClassDescriptor(parent, name);
        if (parent != null) {
            parent.getContains().add(typeDescriptor);
        }
        return typeDescriptor;
    }

    @Override
    protected TypeDescriptor find(String fullQualifiedName) {
        return getStore().findClassDescriptor(fullQualifiedName);
    }

    @Override
    protected char getSeparator() {
        return '.';
    }
}