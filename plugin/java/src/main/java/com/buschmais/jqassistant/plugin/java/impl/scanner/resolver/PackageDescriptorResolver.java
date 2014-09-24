package com.buschmais.jqassistant.plugin.java.impl.scanner.resolver;

import com.buschmais.jqassistant.plugin.java.api.model.PackageDescriptor;

public class PackageDescriptorResolver extends AbstractPackageMemberDescriptorResolver<PackageDescriptor, PackageDescriptor> {

    @Override
    protected Class<PackageDescriptor> getBaseType() {
        return PackageDescriptor.class;
    }

    @Override
    protected char getSeparator() {
        return '.';
    }

}
