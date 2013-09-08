package com.buschmais.jqassistant.plugin.java.impl.store.resolver;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;

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