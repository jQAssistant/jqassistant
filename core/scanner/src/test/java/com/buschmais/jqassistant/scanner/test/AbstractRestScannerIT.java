package com.buschmais.jqassistant.scanner.test;

import org.junit.After;
import org.junit.Before;

import com.buschmais.jqassistant.scanner.ClassScanner;
import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.store.impl.RestGraphStore;
import com.buschmais.jqassistant.store.impl.Server;

public abstract class AbstractRestScannerIT {

	protected Store store;

	protected ClassScanner scanner;

	private Server server;

	private EmbeddedGraphStore embeddedStore;

	@Before
	public void startStore() {
		embeddedStore = new EmbeddedGraphStore("target/jqassistant/"
				+ this.getClass().getSimpleName());
		embeddedStore.start();
		server = new Server(embeddedStore);
		server.start();
		store = new RestGraphStore();
		store.start();
		scanner = new ClassScanner(store);
	}

	@After
	public void stopStore() {
		server.stop();
		embeddedStore.stop();
		store.stop();
	}

}
