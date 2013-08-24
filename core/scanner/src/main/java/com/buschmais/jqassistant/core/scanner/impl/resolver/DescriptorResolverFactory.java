package com.buschmais.jqassistant.core.scanner.impl.resolver;

import com.buschmais.jqassistant.core.store.api.Store;

public class DescriptorResolverFactory {

    private PackageDescriptorResolver packageDescriptorResolver;

    private TypeDescriptorResolver typeDescriptorResolver;

    private MethodDescriptorResolver methodDescriptorResolver;

    private FieldDescriptorResolver fieldDescriptorResolver;

    public DescriptorResolverFactory(Store store) {
        packageDescriptorResolver = new PackageDescriptorResolver(store);
        typeDescriptorResolver = new TypeDescriptorResolver(store, packageDescriptorResolver);
        methodDescriptorResolver = new MethodDescriptorResolver(store, typeDescriptorResolver);
        fieldDescriptorResolver = new FieldDescriptorResolver(store, typeDescriptorResolver);
    }

    public TypeDescriptorResolver getTypeDescriptorResolver() {
        return typeDescriptorResolver;
    }

    public MethodDescriptorResolver getMethodDescriptorResolver() {
        return methodDescriptorResolver;
    }

    public FieldDescriptorResolver getFieldDescriptorResolver() {
        return fieldDescriptorResolver;
    }
}
