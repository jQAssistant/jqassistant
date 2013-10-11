package com.buschmais.jqassistant.core.store.api;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.DescriptorMapper;

import java.util.List;
import java.util.Map;

/**
 * Defines the store for {@link com.buschmais.jqassistant.core.store.api.descriptor.AbstractDescriptor}s.
 */
public interface Store {

    /**
     * Start the store.
     * <p>
     * This method must be called before any other method of this interface can be used.
     * </p>
     *
     * @param mappers The mappers to use.
     */
    void start(List<DescriptorMapper<?>> mappers);

    /**
     * Stop the store.
     * <p>
     * After calling this method no other method defined within this interface can be called.
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
     * Commit a transaction.
     * <p>
     * This method must be called to permanently store the changes of executed write operations.
     * </p>
     */
    void commitTransaction();

    /**
     * Rollback a transaction.
     */
    void rollbackTransaction();


    /**
     * Creates a {@link Descriptor} of the given type.
     *
     * @param type              The type.
     * @return The {@link Descriptor}.
     */
    <T extends Descriptor> T create(Class<T> type);

    /**
     * Creates a {@link Descriptor} of the given type with a full qualified name
     *
     * @param type              The type.
     * @param fullQualifiedName The full qualified name of the descriptor.
     * @return The {@link com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor}.
     */
    <T extends FullQualifiedNameDescriptor> T create(Class<T> type, String fullQualifiedName);

    /**
     * Finds a {@link Descriptor}.
     *
     * @param type              The type.
     * @param fullQualifiedName The full qualified name.
     * @return The {@link Descriptor}.
     */
    <T extends Descriptor> T find(Class<T> type, String fullQualifiedName);

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