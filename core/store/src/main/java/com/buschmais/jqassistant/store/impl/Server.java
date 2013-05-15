package com.buschmais.jqassistant.store.impl;

import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;

/**
 * Web server implementation wrapping an {@link EmbeddedGraphStore}.
 */
public class Server {

	/**
	 * The {@link EmbeddedGraphStore}.
	 */
	private final EmbeddedGraphStore graphStore;

	/**
	 * The {@link WrappingNeoServerBootstrapper}.
	 */
	private WrappingNeoServerBootstrapper server;

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
		GraphDatabaseAPI databaseAPI = graphStore.getDatabaseAPI();
		this.server = new WrappingNeoServerBootstrapper(databaseAPI);
		this.server.start();
	}

	/**
	 * Stop the web server.
	 */
	public void stop() {
		this.server.stop();
	}

}
