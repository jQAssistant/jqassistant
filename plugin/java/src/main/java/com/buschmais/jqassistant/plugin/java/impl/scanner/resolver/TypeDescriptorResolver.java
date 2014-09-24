package com.buschmais.jqassistant.plugin.java.impl.scanner.resolver;

import com.buschmais.jqassistant.plugin.java.api.model.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

public class TypeDescriptorResolver extends AbstractPackageMemberDescriptorResolver<PackageDescriptor, TypeDescriptor> {

    public TypeDescriptorResolver(PackageDescriptorResolver parentResolver) {
        super(parentResolver);
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
