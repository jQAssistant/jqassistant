package com.buschmais.jqassistant.core.scanner.impl.resolver;

import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.ParentDescriptor;
import com.buschmais.jqassistant.core.store.api.Store;

public abstract class AbstractDescriptorResolver<P extends ParentDescriptor, T extends AbstractDescriptor> {

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

    public T resolve(P parent, String name) {
        StringBuffer fullQualifiedName = new StringBuffer(parent.getFullQualifiedName());
        fullQualifiedName.append(getSeparator());
        fullQualifiedName.append(name);
        return resolve(fullQualifiedName.toString());
    }

    public T resolve(String fullQualifiedName) {
        T descriptor = store.find(getType(), fullQualifiedName);
        if (descriptor == null) {
            P parent = null;
            int separatorIndex = fullQualifiedName.lastIndexOf(getSeparator());
            if (separatorIndex != -1) {
                String parentName = fullQualifiedName.substring(0, separatorIndex);
                parent = parentResolver.resolve(parentName);
            }
            descriptor = store.create(getType(), fullQualifiedName);
            if (parent != null) {
                parent.getContains().add(descriptor);
            }
        }
        return descriptor;
    }

    protected Store getStore() {
        return store;
    }

    protected abstract Class<T> getType();

    protected abstract char getSeparator();
}
