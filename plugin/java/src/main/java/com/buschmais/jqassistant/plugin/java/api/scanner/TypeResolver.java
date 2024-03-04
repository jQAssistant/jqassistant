package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ModuleDescriptor;
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
     * @param <T>
     *     The expected type of the descriptor.
     * @param fullQualifiedName
     *     The fully qualified type name, e.g. "java.lang.Object".
     * @param fileDescriptor
     *     The file descriptor.
     * @param descriptorType
     *     The expected type of the descriptor.
     * @param scannerContext
     *     The scanner context. @return The type descriptor.
     */
    <T extends ClassFileDescriptor> TypeCache.CachedType<T> create(String fullQualifiedName, FileDescriptor fileDescriptor, Class<T> descriptorType,
        ScannerContext scannerContext);

    /**
     * Resolve or create the descriptor for Java type name to be used as
     * dependency.
     *
     * @param fullQualifiedName
     *     The fully qualified type name, e.g. "java.lang.Object".
     * @param context
     *     The scanner context.
     */
    TypeCache.CachedType<TypeDescriptor> resolve(String fullQualifiedName, ScannerContext context);

    /**
     * Resolves a {@link FileDescriptor} from the class path.
     *
     * @param requiredFileName
     *     The file name.
     * @param requiredFileType
     *     The required file type.
     * @param context
     *     The {@link ScannerContext}.
     * @param <T>
     *     The {@link FileDescriptor} type.
     * @return The resolved {@link FileDescriptor}.
     */
    <T extends FileDescriptor> T require(String requiredFileName, Class<T> requiredFileType, ScannerContext context);

    /**
     * Resolve the required {@link ModuleDescriptor.
     *
     * @param moduleName
     *     The module name.
     * @param version
     *     The module version.
     * @param context
     *     The {@link ScannerContext}
     * @return The resolved {@link ModuleDescriptor}.
     */
    ModuleDescriptor resolveModule(String moduleName, String version, ScannerContext context);
}
