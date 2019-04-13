package com.buschmais.jqassistant.neo4j.backend.neo4jv3;

import org.neo4j.kernel.internal.GraphDatabaseAPI;

/**
 * Activator for custom libraries providing functions and procedures.
 */
public interface Neo4jLibraryActivator {

    /**
     * Return the human readable name of the library.
     *
     * @return The name.
     */
    String getName();

    /**
     * Register the functions and procedures using the provided
     * {@link GraphDatabaseAPI}.
     * 
     * @param graphDatabaseAPI
     *            The {@link GraphDatabaseAPI}.
     */
    void register(GraphDatabaseAPI graphDatabaseAPI);

}
