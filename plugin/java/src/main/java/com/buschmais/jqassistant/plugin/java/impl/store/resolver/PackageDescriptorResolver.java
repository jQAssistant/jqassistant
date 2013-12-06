package com.buschmais.jqassistant.plugin.java.impl.store.resolver;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PackageDescriptor;

public class PackageDescriptorResolver extends AbstractPackageMemberDescriptorResolver<PackageDescriptor, PackageDescriptor> {

    public PackageDescriptorResolver(Store store) {
        super(store);
    }

    @Override
    protected Class<PackageDescriptor> getBaseType() {
        return PackageDescriptor.class;
    }

    @Override
    protected char getSeparator() {
        return '.';
    }

}
