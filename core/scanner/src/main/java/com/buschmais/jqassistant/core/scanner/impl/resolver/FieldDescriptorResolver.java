package com.buschmais.jqassistant.core.scanner.impl.resolver;

import com.buschmais.jqassistant.core.model.api.descriptor.FieldDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import com.buschmais.jqassistant.core.store.api.Store;

public class FieldDescriptorResolver extends AbstractDescriptorResolver<TypeDescriptor, FieldDescriptor> {

    public FieldDescriptorResolver(Store store, TypeDescriptorResolver parentResolver) {
        super(store, parentResolver);
    }

    @Override
    protected Class<FieldDescriptor> getType() {
        return FieldDescriptor.class;
    }

    @Override
    protected char getSeparator() {
        return '#';
    }
}