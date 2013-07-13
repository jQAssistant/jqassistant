package com.buschmais.jqassistant.store.api;

import com.buschmais.jqassistant.store.api.model.*;

import java.util.Map;

/**
 * Defines the store for {@link AbstractDescriptor}s.
 */
public interface Store {

    /**
     * Start the store.
     * <p>
     * This method must be called before any other method of this interface can
     * be used.
     * </p>
     */
    void start();

    /**
     * Stop the store.
     * <p>
     * After calling this method no other method defined within this interface
     * can be called.
     * </p>
     */
    void stop();

    /**
     * Clear the content of the store, i.e. delete all nodes and relationships.
     */
    void reset();

    /**
     * Begin a transaction.
     * <p>
     * This method must be called before any write operation is performed.
     * </p>
     */
    void beginTransaction();

    /**
     * Flush all pending write operations.
     * <p>Must be called within a transaction.</p>
     */
    void flush();

    /**
     * End a transaction.
     * <p>
     * This method must be called to permanently store the changes of executed
     * write operations.
     * </p>
     */
    void endTransaction();

    /**
     * Resolves a {@link PackageDescriptor}.
     * <p>
     * The {@link PackageDescriptor} is resolved using a parent
     * {@link PackageDescriptor} and the name of the contained package. If no
     * matching {@link PackageDescriptor} can be found in the store a new
     * {@link PackageDescriptor} will be created.
     * </p>
     *
     * @param parentPackageDescriptor The {@link PackageDescriptor} containing the package.
     * @param packageName             The name of the package.
     * @return The resolved {@link PackageDescriptor}.
     */
    PackageDescriptor resolvePackageDescriptor(PackageDescriptor parentPackageDescriptor, String packageName);

    /**
     * Resolves a {@link ClassDescriptor}.
     * <p>
     * The {@link ClassDescriptor} is resolved using a parent
     * {@link PackageDescriptor} and the name of the contained class. If no
     * matching {@link ClassDescriptor} can be found in the store a new
     * {@link ClassDescriptor} will be created.
     * </p>
     *
     * @param packageDescriptor The {@link PackageDescriptor} containing the package.
     * @param className         The name of the class.
     * @return The resolved {@link ClassDescriptor}.
     */
    ClassDescriptor resolveClassDescriptor(PackageDescriptor packageDescriptor, String className);

    /**
     * Resolves a {@link MethodDescriptor}.
     * <p>
     * The {@link MethodDescriptor} is resolved using a parent
     * {@link ClassDescriptor} and the name of the contained method. If no
     * matching {@link MethodDescriptor} can be found in the store a new
     * {@link MethodDescriptor} will be created.
     * </p>
     *
     * @param classDescriptor The {@link ClassDescriptor} containing the method.
     * @param methodName      The name of the method.
     * @return The resolved {@link ClassDescriptor}.
     */
    MethodDescriptor resolveMethodDescriptor(ClassDescriptor classDescriptor, String methodName);

    /**
     * Resolves a {@link FieldDescriptor}.
     * <p>
     * The {@link FieldDescriptor} is resolved using a parent
     * {@link ClassDescriptor} and the name of the contained field. If no
     * matching {@link FieldDescriptor} can be found in the store a new
     * {@link FieldDescriptor} will be created.
     * </p>
     *
     * @param classDescriptor The {@link ClassDescriptor} containing the method.
     * @param fieldName       The name of the field.
     * @return The resolved {@link FieldDescriptor}.
     */
    FieldDescriptor resolveFieldDescriptor(ClassDescriptor classDescriptor, String fieldName);

    /**
     * Executes a CYPHER query.
     * <p>
     * This method delegates to {@link DescriptorDAO#executeQuery(String, Map)}.
     * </p>
     *
     * @param query      The CYPHER query.
     * @param parameters The {@link Map} of parameters for the given query.
     * @return The {@link QueryResult}.
     */
    QueryResult executeQuery(String query, Map<String, Object> parameters);
}