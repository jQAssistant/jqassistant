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
     * @param context
     *            The scanner context.
     */
    public static void add(FileResolverStrategy fileResolverStrategy, ScannerContext context) {
        FileResolver fileResolver = getFileResolverProvider(context);
        fileResolver.resolverStrategies.put(fileResolverStrategy.getClass(), fileResolverStrategy);
    }

    /**
     * Lookup the file resolver provider instance from the scanner context. If
     * no instance exists a new one will be created.
     * 
     * @param context
     *            The context
     * @return The provider.
     */
    private static FileResolver getFileResolverProvider(ScannerContext context) {
        FileResolver fileResolver = context.peek(FileResolver.class);
        if (fileResolver == null) {
            fileResolver = new FileResolver();
            context.push(FileResolver.class, fileResolver);
        }
        return fileResolver;
    }

    /**
     * Remove a file resolver.
     * 
     * @param fileResolverStrategy
     *            The file resolver.
     * @param context
     *            The scanner context.
     */
    public static void remove(FileResolverStrategy fileResolverStrategy, ScannerContext context) {
        remove(fileResolverStrategy.getClass(), context);
    }

    /**
     * Remove a file resolver.
     * 
     * @param fileResolverType
     *            The file resolver type.
     * @param context
     *            The scanner context.
     */
    public static void remove(Class<? extends FileResolverStrategy> fileResolverType, ScannerContext context) {
        FileResolver fileResolver = context.peek(FileResolver.class);
        fileResolver.resolverStrategies.remove(fileResolverType);
        if (fileResolver.resolverStrategies.isEmpty()) {
            context.pop(FileResolver.class);
        }
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
    public static Descriptor resolve(String path, ScannerContext context) {
        final FileResolver provider = getFileResolverProvider(context);
        for (FileResolverStrategy fileResolverStrategy : provider.resolverStrategies.values()) {
            Descriptor resolvedDescriptor = fileResolverStrategy.resolve(path, context);
            if (resolvedDescriptor != null) {
                return resolvedDescriptor;
            }
        }
        return null;
    }
}
