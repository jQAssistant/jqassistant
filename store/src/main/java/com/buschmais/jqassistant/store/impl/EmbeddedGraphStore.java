package com.buschmais.jqassistant.store.impl;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class EmbeddedGraphStore extends AbstractInVMGraphStore {

	private final String databaseDirectory;

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

}
