package com.buschmais.jqassistant.scanner.resolver;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.PackageDescriptor;

public class PackageDescriptorResolver extends AbstractDescriptorResolver<PackageDescriptor, PackageDescriptor> {

    public PackageDescriptorResolver(Store store) {
        super(store);
    }

    @Override
    public PackageDescriptor create(PackageDescriptor parent, String name) {
        PackageDescriptor packageDescriptor = getStore().createPackageDescriptor(parent, name);
        if (parent != null) {
            parent.getContains().add(packageDescriptor);
        }
        return packageDescriptor;
    }

    @Override
    protected PackageDescriptor find(String fullQualifiedName) {
        return getStore().findPackageDescriptor(fullQualifiedName);
    }

    @Override
    protected char getSeparator() {
        return '.';
    }

}
