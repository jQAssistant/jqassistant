package com.buschmais.jqassistant.store.impl;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.tooling.GlobalGraphOperations;

import com.buschmais.jqassistant.store.api.Store;

public abstract class AbstractGraphStore implements Store {

	protected GraphDatabaseService database;

	@Override
	public void start() {
		database = startDatabase();
	}

	@Override
	public void stop() {
		stopDatabase(database);
	}

	@Override
	public void reset() {
		beginTransaction();
		for (Relationship relationShip : GlobalGraphOperations.at(database)
				.getAllRelationships()) {
			relationShip.delete();
		}
		for (Node node : GlobalGraphOperations.at(database).getAllNodes()) {
			node.delete();
		}
		endTransaction();
	}

	public GraphDatabaseAPI getDatabaseAPI() {
		if (database == null) {
			throw new IllegalStateException("Store is not started!.");
		}
		return (GraphDatabaseAPI) database;
	}

	protected abstract GraphDatabaseService startDatabase();

	protected abstract void stopDatabase(GraphDatabaseService database);

}