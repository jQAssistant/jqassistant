package com.buschmais.jqassistant.store.impl;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.rest.graphdb.RestGraphDatabase;

public class RestGraphStore extends AbstractGraphStore {

	@Override
	public void beginTransaction() {
	}

	@Override
	public void endTransaction() {
	}

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
