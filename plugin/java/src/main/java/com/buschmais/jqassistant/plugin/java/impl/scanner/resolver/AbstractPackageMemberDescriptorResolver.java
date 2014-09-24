package com.buschmais.jqassistant.plugin.java.impl.scanner.resolver;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.api.model.PackageDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.PackageMemberDescriptor;

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
    /**
     * The parent resovler.
     */
    private final AbstractPackageMemberDescriptorResolver<?, P> parentResolver;

    /**
     * Constructor.
     * 
     * @param parentResolver
     *            The parent resolver instance.
     */
    protected AbstractPackageMemberDescriptorResolver(AbstractPackageMemberDescriptorResolver<?, P> parentResolver) {
        this.parentResolver = parentResolver;
    }

    /**
     * Constructor.
     */
    @SuppressWarnings("unchecked")
    protected AbstractPackageMemberDescriptorResolver() {
        this.parentResolver = (AbstractPackageMemberDescriptorResolver<?, P>) this;
    }

    /**
     * Resolve a descriptor from a given full qualified name.
     * 
     * @param fullQualifiedName
     *            The full qualified name.
     * @param scannerContext
     *            the scanner context.
     * @return The descriptor.
     */
    public T resolve(String fullQualifiedName, ScannerContext scannerContext) {
        return resolve(fullQualifiedName, getBaseType(), scannerContext);
    }

    /**
     * Resolve a descriptor from a given full qualified name.
     * 
     * @param fullQualifiedName
     *            The full qualified name.
     * @param concreteType
     *            The concrete type to use if an instance is created.
     * @param scannerContext
     *            the scanner context.
     * @return The descriptor.
     */
    public <R extends T> R resolve(String fullQualifiedName, Class<R> concreteType, ScannerContext scannerContext) {
        Store store = scannerContext.getStore();
        T descriptor = store.find(getBaseType(), fullQualifiedName);
        if (descriptor != null) {
            if (getBaseType().equals(concreteType)) {
                return concreteType.cast(descriptor);
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
                parent = parentResolver.resolve(parentName, scannerContext);
            } else {
                name = EMPTY_NAME;
            }
            descriptor = store.create(concreteType, fullQualifiedName);
            descriptor.setName(name);
            if (parent != null) {
                parent.addContains(descriptor);
            }
            return concreteType.cast(descriptor);
        }
    }

    /**
     * Return the descriptor type which is resolved by this instance.
     */
    protected abstract Class<T> getBaseType();

    protected abstract char getSeparator();
}
