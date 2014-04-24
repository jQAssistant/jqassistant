package com.buschmais.jqassistant.plugin.java.impl.store.resolver;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.PackageMemberDescriptor;

/**
 * Abstract resolver providing functionality to resolve a descriptor hierarchy
 * from a full qualified name.
 * <p>
 * A full qualified name consists of the full qualified name of the parent
 * descriptor and the signature of the descriptor.
 * </p>
 * 
 * @param <P>
 *            The type of the parent descriptor.
 * @param <T>
 *            The type of the descriptor to be resolved.
 */
public abstract class AbstractPackageMemberDescriptorResolver<P extends PackageDescriptor, T extends PackageMemberDescriptor> {

    public static final String EMPTY_NAME = "";
    private final Store store;
    /**
     * The parent resovler.
     */
    private final AbstractPackageMemberDescriptorResolver<?, P> parentResolver;

    /**
     * Constructor.
     * 
     * @param store
     *            The store.
     * @param parentResolver
     *            The parent resolver instance.
     */
    protected AbstractPackageMemberDescriptorResolver(Store store, AbstractPackageMemberDescriptorResolver<?, P> parentResolver) {
        this.store = store;
        this.parentResolver = parentResolver;
    }

    /**
     * Constructor.
     * 
     * @param store
     *            The store.
     */
    @SuppressWarnings("unchecked")
    protected AbstractPackageMemberDescriptorResolver(Store store) {
        this.store = store;
        this.parentResolver = (AbstractPackageMemberDescriptorResolver<?, P>) this;
    }

    /**
     * Resolve a descriptor from a given full qualified name.
     * 
     * @param fullQualifiedName
     *            The full qualified name.
     * @return The descriptor.
     */
    public T resolve(String fullQualifiedName) {
        return resolve(fullQualifiedName, getBaseType());
    }

    /**
     * Resolve a descriptor from a given full qualified name.
     * 
     * @param fullQualifiedName
     *            The full qualified name.
     * @param concreteType
     *            The concrete type to use if an instance is created.
     * @return The descriptor.
     */
    public T resolve(String fullQualifiedName, Class<? extends T> concreteType) {
        T descriptor = store.find(getBaseType(), fullQualifiedName);
        if (descriptor != null) {
            if (getBaseType().equals(concreteType)) {
                return descriptor;
            } else {
                return store.migrate(descriptor, concreteType);
            }
        } else {
            P parent = null;
            String name;
            if (!EMPTY_NAME.equals(fullQualifiedName)) {
                int separatorIndex = fullQualifiedName.lastIndexOf(getSeparator());
                String parentName;
                if (separatorIndex != -1) {
                    name = fullQualifiedName.substring(separatorIndex + 1, fullQualifiedName.length());
                    parentName = fullQualifiedName.substring(0, separatorIndex);
                } else {
                    name = fullQualifiedName;
                    parentName = EMPTY_NAME;
                }
                parent = parentResolver.resolve(parentName);
            } else {
                name = EMPTY_NAME;
            }
            descriptor = store.create(concreteType, fullQualifiedName);
            descriptor.setName(name);
            if (parent != null) {
                parent.addContains(descriptor);
            }
            return descriptor;
        }
    }

    /**
     * Return the descriptor type which is resolved by this instance.
     */
    protected abstract Class<T> getBaseType();

    protected abstract char getSeparator();
}
