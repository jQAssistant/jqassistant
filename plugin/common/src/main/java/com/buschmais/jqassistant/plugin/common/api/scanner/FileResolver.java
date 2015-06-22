package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.util.HashMap;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;

/**
 * A file resolver.
 */
public final class FileResolver {

    /**
     * The registered file resolver instances identified by their type.
     */
    private HashMap<Class<? extends FileResolverStrategy>, FileResolverStrategy> resolverStrategies = new HashMap<>();

    /**
     * Add a file resolver.
     * 
     * @param fileResolverStrategy
     *            A file resolver.
     */
    public void addStrategy(FileResolverStrategy fileResolverStrategy) {
        resolverStrategies.put(fileResolverStrategy.getClass(), fileResolverStrategy);
    }

    /**
     * Remove a file resolver.
     * 
     * @param fileResolverStrategy
     *            The file resolver.
     */
    public void removeStrategy(FileResolverStrategy fileResolverStrategy) {
        removeStrategy(fileResolverStrategy.getClass());
    }

    /**
     * Remove a file resolver.
     * 
     * @param fileResolverType
     *            The file resolver type.
     */
    public void removeStrategy(Class<? extends FileResolverStrategy> fileResolverType) {
        resolverStrategies.remove(fileResolverType);
    }

    /**
     * Resolve the given resource.
     * 
     * @param path
     *            The path.
     * @param context
     *            The scanner context.
     * @return The resolved {@link Descriptor} or <code>null</code>.
     */
    public Descriptor resolve(String path, ScannerContext context) {
        for (FileResolverStrategy fileResolverStrategy : resolverStrategies.values()) {
            Descriptor resolvedDescriptor = fileResolverStrategy.resolve(path, context);
            if (resolvedDescriptor != null) {
                return resolvedDescriptor;
            }
        }
        return null;
    }
}
