package com.buschmais.jqassistant.plugin.java.impl.scanner.resolver;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.api.model.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

public class TypeDescriptorResolver extends AbstractPackageMemberDescriptorResolver<PackageDescriptor, TypeDescriptor> {

    public TypeDescriptorResolver(Store store, PackageDescriptorResolver parentResolver) {
        super(store, parentResolver);
    }

    @Override
    protected Class<TypeDescriptor> getBaseType() {
        return TypeDescriptor.class;
    }

    @Override
    protected char getSeparator() {
        return '.';
    }
}
