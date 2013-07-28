package com.buschmais.jqassistant.scanner.impl.resolver;

import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.store.api.Store;

public abstract class AbstractDescriptorResolver<P extends AbstractDescriptor, T extends AbstractDescriptor> {

    private final Store store;

    private final AbstractDescriptorResolver<?, P> parentResolver;

    protected AbstractDescriptorResolver(Store store, AbstractDescriptorResolver<?, P> parentResolver) {
        this.store = store;
        this.parentResolver = parentResolver;
    }

    @SuppressWarnings("unchecked")
    public AbstractDescriptorResolver(Store store) {
        this.store = store;
        this.parentResolver = (AbstractDescriptorResolver<?, P>) this;
    }

    public T resolve(String fullQualifiedName) {
        T descriptor = find(fullQualifiedName);
        if (descriptor == null) {
            String name;
            P parent = null;
            int separatorIndex = fullQualifiedName.lastIndexOf(getSeparator());
            if (separatorIndex == -1) {
                name = fullQualifiedName;
            } else {
                String parentName = fullQualifiedName.substring(0, separatorIndex);
                name = fullQualifiedName.substring(separatorIndex + 1, fullQualifiedName.length());
                parent = parentResolver.resolve(parentName);
            }
            descriptor = this.create(parent, name);
        }
        return descriptor;
    }

    protected Store getStore() {
        return store;
    }

    protected abstract char getSeparator();

    protected abstract T create(P parent, String name);

    protected abstract T find(String fullQualifiedName);
}
