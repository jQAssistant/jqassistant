package com.buschmais.jqassistant.core.scanner.impl.resolver;

import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.core.store.api.Store;

public class TypeDescriptorResolver extends AbstractDescriptorResolver<PackageDescriptor, TypeDescriptor> {

    public TypeDescriptorResolver(Store store, PackageDescriptorResolver parentResolver) {
        super(store, parentResolver);
    }

    @Override
    protected Class<TypeDescriptor> getType() {
        return TypeDescriptor.class;
    }

    @Override
    protected char getSeparator() {
        return '.';
    }
}