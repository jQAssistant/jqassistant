package com.buschmais.jqassistant.store.api;

import com.buschmais.jqassistant.core.model.api.descriptor.*;

import java.util.Map;

/**
 * Defines the store for {@link com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor}s.
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
     * Resolves a
     * {@link com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor}
     * .
     * <p>
     * The
     * {@link com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor}
     * is resolved using artifact information (groupId, artifactId, version).
     * </p>
     *
     * @param fullQualifiedName The identifier of the artifact. Pattern
     *                          "<group>:<artifact>:<version>"
     * @return The resolved
     *         {@link com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor}
     *         .
     */
    ArtifactDescriptor createArtifactDescriptor(String fullQualifiedName);

    /**
     * Finds a
     * {@link com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor}
     * by it's full qualified name.
     *
     * @param fullQualifiedName The full qualified name.
     * @return The
     *         {@link com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor}
     *         or <code>null</code> if it does not exist.
     */
    ArtifactDescriptor findArtifactDescriptor(String fullQualifiedName);

    /**
     * Resolves a
     * {@link com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor}
     * .
     * <p>
     * The
     * {@link com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor}
     * is resolved using a parent
     * {@link com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor}
     * and the name of the contained package.
     * </p>
     *
     * @param parentArtifactDescriptor The
     *                                 {@link com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor}
     *                                 containing the package.
     * @param packageName              The name of the package.
     * @return The resolved
     *         {@link com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor}
     */
    PackageDescriptor createPackageDescriptor(final ArtifactDescriptor parentArtifactDescriptor, final String packageName);

    /**
     * Resolves a
     * {@link com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor}
     * .
     * <p>
     * The
     * {@link com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor}
     * is resolved using a parent
     * {@link com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor}
     * and the name of the contained package.
     * </p>
     *
     * @param parentPackageDescriptor The
     *                                {@link com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor}
     *                                containing the package.
     * @param packageName             The name of the package.
     * @return The resolved
     *         {@link com.buschmais.jqassistant.core.model.api.descriptor.PackageDescriptor}
     */
    PackageDescriptor createPackageDescriptor(PackageDescriptor parentPackageDescriptor, String packageName);


    /**
     * Finds a {@link com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor} by it's full qualified name.
     *
     * @param fullQualifiedName The full qualified name.
     * @return The {@link com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor} or <code>null</code> if it does not exist.
     */
    PackageDescriptor findPackageDescriptor(String fullQualifiedName);

    /**
     * Resolves a
     * {@link com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor}
     * .
     * <p>
     * The
     * {@link com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor}
     * is resolved using a parent {@link PackageDescriptor} and the name of the
     * contained class.
     * </p>
     *
     * @param packageDescriptor The {@link PackageDescriptor} containing the package.
     * @param className         The name of the class.
     * @return The resolved
     *         {@link com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor}
     *         .
     */
    TypeDescriptor createClassDescriptor(PackageDescriptor packageDescriptor, String className);

    /**
     * Finds a {@link com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor} by it's full qualified name.
     *
     * @param fullQualifiedName The full qualified name.
     * @return The {@link com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor} or <code>null</code> if it does not exist.
     */
    TypeDescriptor findClassDescriptor(String fullQualifiedName);

    /**
     * Resolves a
     * {@link com.buschmais.jqassistant.core.model.api.descriptor.MethodDescriptor}
     * .
     * <p>
     * The
     * {@link com.buschmais.jqassistant.core.model.api.descriptor.MethodDescriptor}
     * is resolved using a parent {@link com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor} and the name of the
     * contained method.
     * </p>
     *
     * @param typeDescriptor The {@link com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor} containing the method.
     * @param methodName      The name of the method.
     * @return The resolved {@link com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor}.
     */
    MethodDescriptor createMethodDescriptor(TypeDescriptor typeDescriptor, String methodName);

    /**
     * Resolves a
     * {@link com.buschmais.jqassistant.core.model.api.descriptor.FieldDescriptor}
     * .
     * <p>
     * The
     * {@link com.buschmais.jqassistant.core.model.api.descriptor.FieldDescriptor}
     * is resolved using a parent {@link com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor} and the name of the
     * contained field.
     * </p>
     *
     * @param typeDescriptor The {@link com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor} containing the method.
     * @param fieldName       The name of the field.
     * @return The resolved
     *         {@link com.buschmais.jqassistant.core.model.api.descriptor.FieldDescriptor}
     *         .
     */
    FieldDescriptor createFieldDescriptor(TypeDescriptor typeDescriptor, String fieldName);

    /**
     * Executes a CYPHER query.
     * <p>
     * This method delegates to {@link DescriptorDAO#executeQuery(String, Map)} using Collections#emptyMap as parameters.
     * </p>
     *
     * @param query The CYPHER query.
     * @return The {@link QueryResult}.
     */
    QueryResult executeQuery(String query);

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