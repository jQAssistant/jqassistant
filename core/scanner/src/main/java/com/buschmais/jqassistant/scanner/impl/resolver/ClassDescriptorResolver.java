package com.buschmais.jqassistant.scanner.impl.resolver;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.descriptor.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.descriptor.PackageDescriptor;

public class ClassDescriptorResolver extends AbstractDescriptorResolver<PackageDescriptor, ClassDescriptor> {

    public ClassDescriptorResolver(Store store, PackageDescriptorResolver parentResolver) {
        super(store, parentResolver);
    }

    @Override
    public ClassDescriptor create(PackageDescriptor parent, String name) {
        ClassDescriptor classDescriptor = getStore().createClassDescriptor(parent, name);
        if (parent != null) {
            parent.getContains().add(classDescriptor);
        }
        return classDescriptor;
    }

    @Override
    protected ClassDescriptor find(String fullQualifiedName) {
        return getStore().findClassDescriptor(fullQualifiedName);
    }

    @Override
    protected char getSeparator() {
        return '.';
    }
}