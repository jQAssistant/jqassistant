package com.buschmais.jqassistant.core.scanner.impl.resolver;

import com.buschmais.jqassistant.core.model.api.descriptor.MethodDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.store.api.Store;

public class MethodDescriptorResolver extends AbstractDescriptorResolver<TypeDescriptor, MethodDescriptor> {

    public MethodDescriptorResolver(Store store, TypeDescriptorResolver parentResolver) {
        super(store, parentResolver);
    }

    @Override
    protected Class<MethodDescriptor> getType() {
        return MethodDescriptor.class;
    }

    @Override
    protected char getSeparator() {
        return '#';
    }
}