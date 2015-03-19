package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

/**
 * Defines the interface for type resolvers.
 */
public interface TypeResolver {

    /**
     * Resolve or create the descriptor for a Java type name.
     * <p>
     * If a the descriptor already exists it will be used and migrated to the
     * given type.
     * </p>
     * 
     * @param fullQualifiedName
     *            The fully qualified type name, e.g. "java.lang.Object".
     * @param descriptorType
     *            The expected type of the descriptor.
     * @param scannerContext
     *            The scanner context.
     * @param <T>
     *            The expected type of the descriptor.
     * @return The type descriptor.
     */
    <T extends ClassFileDescriptor> TypeCache.CachedType<T> create(String fullQualifiedName, Class<T> descriptorType, ScannerContext scannerContext);

    /**
     * Resolve or create the descriptor for Java type name to be used as
     * dependency.
     * 
     * @param fullQualifiedName
     *            The fully qualified type name, e.g. "java.lang.Object".
     * @param context
     *            The scanner context.
     */
    TypeCache.CachedType<TypeDescriptor> resolve(String fullQualifiedName, ScannerContext context);

}
