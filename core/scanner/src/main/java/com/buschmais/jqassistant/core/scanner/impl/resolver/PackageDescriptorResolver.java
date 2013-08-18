package com.buschmais.jqassistant.core.scanner.impl.resolver;

import com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.core.store.api.Store;

public class PackageDescriptorResolver extends AbstractDescriptorResolver<PackageDescriptor, PackageDescriptor> {

    public PackageDescriptorResolver(Store store) {
        super(store);
    }

    @Override
    protected Class<PackageDescriptor> getType() {
        return PackageDescriptor.class;
    }

    @Override
    protected char getSeparator() {
        return '.';
    }

}
