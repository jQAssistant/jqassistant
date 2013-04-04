package com.buschmais.jqassistant.store.impl;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;

public class EmbeddedGraphStore extends AbstractGraphStore {

	private final String databaseDirectory;

	private Transaction transaction = null;

	public EmbeddedGraphStore(String databaseDirectory) {
		this.databaseDirectory = databaseDirectory;
	}

	@Override
	protected GraphDatabaseService startDatabase() {
		return new GraphDatabaseFactory()
				.newEmbeddedDatabase(databaseDirectory);
	}

	@Override
	protected void stopDatabase(GraphDatabaseService database) {
		database.shutdown();
	}

	@Override
	public void beginTransaction() {
		if (transaction != null) {
			throw new IllegalStateException(
					"There is already an existing transaction.");
		}
		transaction = database.beginTx();
	}

	@Override
	public void endTransaction() {
		if (transaction == null) {
			throw new IllegalStateException("There is no existing transaction.");
		}
		transaction.success();
		transaction.finish();
		transaction = null;
	}

	@Override
	public void reset() {
		for (Relationship relationShip : GlobalGraphOperations.at(database)
				.getAllRelationships()) {
			relationShip.delete();
		}
		for (Node node : GlobalGraphOperations.at(database).getAllNodes()) {
			node.delete();
		}
	}
}
