package com.buschmais.jqassistant.core.store.api;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;

import java.util.Collection;
import java.util.Map;

import static com.buschmais.cdo.api.Query.Result;
import static com.buschmais.cdo.api.Query.Result.CompositeRowObject;

/**
 * Defines the store for
 * {@link Descriptor}s.
 */
public abstract class Store {

    /**
     * Start the store.
     * <p>
     * This method must be called before any other method of this interface can
     * be used.
     * </p>
     *
     * @param types The types to use.
     */
    public abstract void start(Collection<Class<?>> types);

    /**
     * Stop the store.
     * <p>
     * After calling this method no other method defined within this interface
     * can be called.
     * </p>
     */
    public abstract void stop();

    /**
     * Clear the content of the store, i.e. delete all nodes and relationships.
     */
    public abstract void reset();

    /**
     * Begin a transaction.
     * <p>
     * This method must be called before any write operation is performed.
     * </p>
     */
    public abstract void beginTransaction();

    /**
     * Commit a transaction.
     * <p>
     * This method must be called to permanently store the changes of executed
     * write operations.
     * </p>
     */
    public abstract void commitTransaction();

    /**
     * Rollback a transaction.
     */
    public abstract void rollbackTransaction();

    /**
     * Creates a {@link Descriptor} of the given type.
     *
     * @param type The type.
     * @return The {@link Descriptor}.
     */
    public abstract <T extends Descriptor> T create(Class<T> type);

    /**
     * Migrates the descriptor instance to the given sub-type.
     *
     * @param descriptor   The descriptor.
     * @param concreteType The concrete type.
     * @param <T>          The descriptor type.
     * @param <C>          The concrete type.
     */
    public abstract <T extends Descriptor, C extends T> C migrate(T descriptor, Class<C> concreteType);

    /**
     * Creates a {@link Descriptor} of the given type with a full qualified name
     *
     * @param type              The type.
     * @param fullQualifiedName The full qualified name of the descriptor.
     * @return The
     *         {@link com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor}
     *         .
     */
    public abstract <T extends FullQualifiedNameDescriptor> T create(Class<T> type, String fullQualifiedName);

    /**
     * Finds a {@link Descriptor}.
     *
     * @param type              The type.
     * @param fullQualifiedName The full qualified name.
     * @return The {@link Descriptor}.
     */
    public abstract <T extends Descriptor> T find(Class<T> type, String fullQualifiedName);

    /**
     * Executes a CYPHER query.
     * <p>
     * This method executes a CYPHER query.
     * </p>
     *
     *
     *
     * @param query      The CYPHER query.
     * @param parameters The {@link java.util.Map} of parameters for the given query.
     * @return The {@link Result}.
     */
    public abstract Result<CompositeRowObject> executeQuery(String query, Map<String, Object> parameters);
}
