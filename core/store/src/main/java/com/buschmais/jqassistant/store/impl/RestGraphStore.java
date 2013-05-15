package com.buschmais.jqassistant.store.impl;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.rest.graphdb.RestGraphDatabase;

import com.buschmais.jqassistant.store.api.Store;

/**
 * Experimental {@link Store} implementation using an remote Neo4j instance via
 * REST.
 */
public class RestGraphStore extends AbstractGraphStore {

	@Override
	protected GraphDatabaseService startDatabase() {
		return new RestGraphDatabase("http://localhost:7474/db/data");
	}

	@Override
	protected void stopDatabase(GraphDatabaseService database) {
	}

	@Override
	public void reset() {
	}

}
