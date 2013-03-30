package com.buschmais.jqassistant.scanner.test;

import org.junit.After;
import org.junit.Before;

import com.buschmais.jqassistant.scanner.ClassScanner;
import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.impl.EmbeddedGraphStore;

public abstract class AbstractScannerIT {

	private Store store;

	protected ClassScanner scanner;

	@Before
	public void startStore() {
		store = new EmbeddedGraphStore("target/jqassistant/"
				+ this.getClass().getSimpleName());
		scanner = new ClassScanner(store);
		store.start();
		store.reset();
	}

	@After
	public void stopStore() {
		store.stop();
	}

}
