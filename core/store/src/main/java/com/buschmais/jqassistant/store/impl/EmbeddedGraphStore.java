package com.buschmais.jqassistant.store.impl;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.tooling.GlobalGraphOperations;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;
import com.buschmais.jqassistant.store.impl.dao.mapper.DescriptorMapper;

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
		super.endTransaction();
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
			DescriptorMapper<AbstractDescriptor> adapter = getAdapterRegistry()
					.getDescriptorAdapter(node);
			if (adapter != null) {
				Index<Node> index = adapter.getIndex();
				if (index != null) {
					index.remove(node);
				}
			}
			node.delete();
		}
	}
}
