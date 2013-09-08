package com.buschmais.jqassistant.plugin.java.impl.store.resolver;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;

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