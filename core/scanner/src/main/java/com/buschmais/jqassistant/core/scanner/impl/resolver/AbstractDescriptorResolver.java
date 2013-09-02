package com.buschmais.jqassistant.core.scanner.impl.resolver;

import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.ParentDescriptor;
import com.buschmais.jqassistant.core.store.api.Store;
import org.apache.commons.lang.StringUtils;

/**
 * Abstract resolver providing functionality to resolve a descriptor hierarchy from full qualified name.
 *
 * @param <P> The type of the parent descriptor.
 * @param <T> The type of the descriptor to be resolved.
 */
public abstract class AbstractDescriptorResolver<P extends ParentDescriptor, T extends AbstractDescriptor> {

    public static final String EMPTY_NAME = "";
    private final Store store;

    private final AbstractDescriptorResolver<?, P> parentResolver;

    /**
     * Constructor.
     *
     * @param store          The store.
     * @param parentResolver The parent resolver instance.
     */
    protected AbstractDescriptorResolver(Store store, AbstractDescriptorResolver<?, P> parentResolver) {
        this.store = store;
        this.parentResolver = parentResolver;
    }

    /**
     * Constructor.
     *
     * @param store The store.
     */
    @SuppressWarnings("unchecked")
    protected AbstractDescriptorResolver(Store store) {
        this.store = store;
        this.parentResolver = (AbstractDescriptorResolver<?, P>) this;
    }

    /**
     * Resolve a descriptor from a given parent descriptor and a local name.
     *
     * @param parent The parent descriptor.
     * @param name   The name.
     * @return The descriptor.
     */
    public T resolve(P parent, String name) {
        StringBuffer fullQualifiedName = new StringBuffer(parent.getFullQualifiedName());
        fullQualifiedName.append(getSeparator());
        fullQualifiedName.append(name);
        return resolve(fullQualifiedName.toString());
    }

    /**
     * Resolve a descriptor from a given full qualified name.
     *
     * @param fullQualifiedName The full qualified name.
     * @return The descriptor.
     */
    public T resolve(String fullQualifiedName) {
        T descriptor = store.find(getType(), fullQualifiedName);
        if (descriptor == null) {
            P parent = null;
            if (!EMPTY_NAME.equals(fullQualifiedName)) {
                int separatorIndex = fullQualifiedName.lastIndexOf(getSeparator());
                String parentName;
                if (separatorIndex != -1) {
                    parentName = fullQualifiedName.substring(0, separatorIndex);
                } else {
                    parentName = EMPTY_NAME;
                }
                parent = parentResolver.resolve(parentName);
            }
            descriptor = store.create(getType(), fullQualifiedName);
            if (parent != null) {
                parent.getContains().add(descriptor);
            }
        }
        return descriptor;
    }

    /**
     * Return the descriptor type which is resolved by this instance.
     */
    protected abstract Class<T> getType();

    protected abstract char getSeparator();
}
