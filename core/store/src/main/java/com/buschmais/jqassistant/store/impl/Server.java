package com.buschmais.jqassistant.store.impl;

import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;

public class Server {

	private AbstractGraphStore graphStore;

	private WrappingNeoServerBootstrapper server;

	public Server(AbstractGraphStore graphStore) {
		this.graphStore = graphStore;
	}

	public void start() {
		GraphDatabaseAPI databaseAPI = graphStore.getDatabaseAPI();
		this.server = new WrappingNeoServerBootstrapper(databaseAPI);
		this.server.start();
	}

	public void stop() {
		this.server.stop();
	}

}
