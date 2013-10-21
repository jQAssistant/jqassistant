package com.buschmais.jqassistant.core.store.impl;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import com.buschmais.jqassistant.core.store.api.Store;

/**
 * {@link Store} implementation using an embedded Neo4j instance.
 */
public class EmbeddedGraphStore extends AbstractGraphStore {

	/**
	 * The directory of the database.
	 */
	private final String databaseDirectory;

	/**
	 * The current {@link Transaction}.
	 */
	private Transaction transaction = null;

	/**
	 * Constructor.
	 * 
	 * @param databaseDirectory
	 *            The directory of the database.
	 */
	public EmbeddedGraphStore(String databaseDirectory) {
		this.databaseDirectory = databaseDirectory;
	}

	@Override
	protected GraphDatabaseService startDatabase() {
		return new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory);
	}

	@Override
	protected void stopDatabase(GraphDatabaseService database) {
		if (database != null) {
			database.shutdown();
		}
	}

	@Override
	public void beginTransaction() {
		if (transaction != null) {
			throw new IllegalStateException("There is already an existing transaction.");
		}
		transaction = database.beginTx();
	}

	@Override
	public void commitTransaction() {
		if (transaction == null) {
			throw new IllegalStateException("There is no existing transaction.");
		}
		flush();
		transaction.success();
		transaction.close();
		transaction = null;
	}

	@Override
	public void rollbackTransaction() {
		if (transaction == null) {
			throw new IllegalStateException("There is no existing transaction.");
		}
		transaction.failure();
		transaction.close();
		transaction = null;
	}

}
