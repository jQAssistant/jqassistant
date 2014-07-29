package com.buschmais.jqassistant.plugin.java.impl.scanner.resolver;

import com.buschmais.jqassistant.core.store.api.Store;

public class DescriptorResolverFactory {

    private ThreadLocal<PackageDescriptorResolver> packageDescriptorResolver = new ThreadLocal<PackageDescriptorResolver>() {
        @Override
        protected PackageDescriptorResolver initialValue() {
            return new PackageDescriptorResolver(store);
        }
    };

    private ThreadLocal<TypeDescriptorResolver> typeDescriptorResolver = new ThreadLocal<TypeDescriptorResolver>() {
        @Override
        protected TypeDescriptorResolver initialValue() {
            return new TypeDescriptorResolver(store, getPackageDescriptorResolver());
        }
    };
    
    private Store store;
    

    public DescriptorResolverFactory(Store store) {
        this.store = store;
    }

    public TypeDescriptorResolver getTypeDescriptorResolver() {
        return typeDescriptorResolver.get();
    }
    
    public PackageDescriptorResolver getPackageDescriptorResolver() {
        return packageDescriptorResolver.get();
    }
}
