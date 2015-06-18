package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.util.Collection;
import java.util.HashMap;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;

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
        FileResolverProvider fileResolverProvider = context.peek(FileResolverProvider.class);
        if (fileResolverProvider == null) {
            fileResolverProvider = new FileResolverProvider();
            context.push(FileResolverProvider.class, fileResolverProvider);
        }
        fileResolverProvider.resolverMap.put(fileResolver.getClass(), fileResolver);
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

    public Collection<FileResolver> get() {
        return resolverMap.values();
    }
}
