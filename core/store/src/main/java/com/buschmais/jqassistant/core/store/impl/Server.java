package com.buschmais.jqassistant.core.store.impl;

import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServer;

/**
 * Web server implementation wrapping an {@link EmbeddedGraphStore}.
 */
public class Server {

	/**
	 * The {@link EmbeddedGraphStore}.
	 */
	private final EmbeddedGraphStore graphStore;

	/**
	 * The {@link WrappingNeoServer}.
	 */
	private WrappingNeoServer server;

	/**
	 * Constructor.
	 *
	 * @param graphStore
	 *            The {@link EmbeddedGraphStore}.
	 */
	public Server(EmbeddedGraphStore graphStore) {
		this.graphStore = graphStore;
	}

	/**
	 * Start the web server.
	 */
	public void start() {
		GraphDatabaseAPI databaseAPI = graphStore.getDatabaseService();
		this.server = new WrappingNeoServer(databaseAPI);
		this.server.start();
	}

	/**
	 * Stop the web server.
	 */
	public void stop() {
		this.server.stop();
	}

}
