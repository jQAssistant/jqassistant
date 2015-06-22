package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.util.HashMap;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

/**
 * A provider for file resolvers.
 */
public final class FileResolverProvider {

    /**
     * The registered file resolver instances identified by their type.
     */
    private HashMap<Class<? extends FileResolver>, FileResolver> resolverMap = new HashMap<>();

    /**
     * Add a file resolver.
     * 
     * @param fileResolver
     *            A file resolver.
     * @param context
     *            The scanner context.
     */
    public static void add(FileResolver fileResolver, ScannerContext context) {
        FileResolverProvider fileResolverProvider = getFileResolverProvider(context);
        fileResolverProvider.resolverMap.put(fileResolver.getClass(), fileResolver);
    }

    /**
     * Lookup the file resolver provider instance from the scanner context. If
     * no instance exists a new one will be created.
     * 
     * @param context
     *            The context
     * @return The provider.
     */
    private static FileResolverProvider getFileResolverProvider(ScannerContext context) {
        FileResolverProvider fileResolverProvider = context.peek(FileResolverProvider.class);
        if (fileResolverProvider == null) {
            fileResolverProvider = new FileResolverProvider();
            context.push(FileResolverProvider.class, fileResolverProvider);
        }
        return fileResolverProvider;
    }

    /**
     * Remove a file resolver.
     * 
     * @param fileResolver
     *            The file resolver.
     * @param context
     *            The scanner context.
     */
    public static void remove(FileResolver fileResolver, ScannerContext context) {
        remove(fileResolver.getClass(), context);
    }

    /**
     * Remove a file resolver.
     * 
     * @param fileResolverType
     *            The file resolver type.
     * @param context
     *            The scanner context.
     */
    public static void remove(Class<? extends FileResolver> fileResolverType, ScannerContext context) {
        FileResolverProvider fileResolverProvider = context.peek(FileResolverProvider.class);
        fileResolverProvider.resolverMap.remove(fileResolverType);
        if (fileResolverProvider.resolverMap.isEmpty()) {
            context.pop(FileResolverProvider.class);
        }
    }

    /**
     * Resolve the given resource.
     * 
     * @param fileResource
     *            The file resource.
     * @param path
     *            The path.
     * @param context
     *            The scanner context.
     * @return The resolved {@link Descriptor} or <code>null</code>.
     */
    public static Descriptor resolve(FileResource fileResource, String path, ScannerContext context) {
        final FileResolverProvider provider = getFileResolverProvider(context);
        for (FileResolver fileResolver : provider.resolverMap.values()) {
            Descriptor resolvedDescriptor = fileResolver.resolve(fileResource, path, context);
            if (resolvedDescriptor != null) {
                return resolvedDescriptor;
            }
        }
        return null;
    }
}
