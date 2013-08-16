package com.buschmais.jqassistant.core.scanner.impl.resolver;

import com.buschmais.jqassistant.core.store.api.Store;

public class DescriptorResolverFactory {

    private final Store store;

    private final PackageDescriptorResolver packageDescriptorResolver;

    private final TypeDescriptorResolver typeDescriptorResolver;

    public DescriptorResolverFactory(Store store) {
        this.store = store;
        packageDescriptorResolver = new PackageDescriptorResolver(store);
        typeDescriptorResolver = new TypeDescriptorResolver(store, packageDescriptorResolver);
    }

    public Store getStore() {
        return store;
    }

    public PackageDescriptorResolver getPackageDescriptorResolver() {
        return packageDescriptorResolver;
    }

    public TypeDescriptorResolver getTypeDescriptorResolver() {
        return typeDescriptorResolver;
    }

}
