package com.buschmais.jqassistant.plugin.java.impl.scanner.resolver;

public class DescriptorResolverFactory {

    private PackageDescriptorResolver packageDescriptorResolver;

    private TypeDescriptorResolver typeDescriptorResolver;

    public DescriptorResolverFactory() {
        packageDescriptorResolver = new PackageDescriptorResolver();
        typeDescriptorResolver = new TypeDescriptorResolver(packageDescriptorResolver);
    }

    public TypeDescriptorResolver getTypeDescriptorResolver() {
        return typeDescriptorResolver;
    }
}
