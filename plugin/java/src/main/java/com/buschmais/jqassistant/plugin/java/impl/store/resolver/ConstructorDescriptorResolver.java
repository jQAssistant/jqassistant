package com.buschmais.jqassistant.plugin.java.impl.store.resolver;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ConstructorDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;

public class ConstructorDescriptorResolver extends AbstractDescriptorResolver<TypeDescriptor, ConstructorDescriptor> {

    public ConstructorDescriptorResolver(Store store, TypeDescriptorResolver parentResolver) {
        super(store, parentResolver);
    }

    @Override
    protected Class<ConstructorDescriptor> getBaseType() {
        return ConstructorDescriptor.class;
    }

    @Override
    protected char getSeparator() {
        return '#';
    }
}
