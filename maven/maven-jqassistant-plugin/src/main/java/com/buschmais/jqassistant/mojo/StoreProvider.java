package com.buschmais.jqassistant.mojo;

import java.io.File;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.impl.EmbeddedGraphStore;

/**
 */
public class StoreProvider {

	private Store store = null;

	public Store getStore(File databaseDirectory) {
		if (store == null) {
			store = new EmbeddedGraphStore(databaseDirectory.getAbsolutePath());
			store.start();
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					store.stop();
				}
			});

		}
		return store;
	}

}
