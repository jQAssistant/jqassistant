package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

/**
 * Defines the interface for type resolvers.
 */
public interface TypeResolver {

    /**
     * Resolve the descriptor of for Java type name.
     * 
     * @param fullQualifiedName
     *            The fully qualified type name, e.g. "java.lang.Object".
     * @param expectedType
     *            The expected type of the descriptor.
     * @param scannerContext
     *            The scanner context.
     * @param <T>
     *            The expected type of the descriptor.
     * @return The type descriptor.
     */
    <T extends TypeDescriptor> TypeCache.CachedType<T> create(String fullQualifiedName, Class<T> expectedType, ScannerContext scannerContext);

    /**
     * Resolve the descriptor of for Java type name.
     * 
     * @param fullQualifiedName
     *            The fully qualified type name, e.g. "java.lang.Object".
     * @param context
     *            The scanner context.
     */
    TypeCache.CachedType<TypeDescriptor> resolve(String fullQualifiedName, ScannerContext context);

}
