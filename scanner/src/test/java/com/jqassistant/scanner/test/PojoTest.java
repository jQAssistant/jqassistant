package com.jqassistant.scanner.test;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.buschmais.jqassistant.scanner.DependencyScanner;
import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.impl.EmbeddedGraphStore;
import com.jqassistant.scanner.test.sets.pojo.Pojo;

public class PojoTest {

	private static final String CLASSNAME = "/"
			+ Pojo.class.getName().replace('.', '/') + ".class";

	private final Store store = new EmbeddedGraphStore();

	@Before
	public void startStore() {
		store.start();
	}

	@After
	public void stopStore() {
		store.stop();
	}

	@Test
	public void attributes() throws IOException {
		DependencyScanner scanner = new DependencyScanner(store);
		store.beginTransaction();
		scanner.scanInputStream(PojoTest.class.getResourceAsStream(CLASSNAME));
		store.endTransaction();
		System.out.println("test");
	}
}
