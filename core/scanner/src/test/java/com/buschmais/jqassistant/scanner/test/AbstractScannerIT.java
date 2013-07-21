package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.scanner.ClassScanner;
import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.impl.EmbeddedGraphStore;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractScannerIT {

    protected Store store;

    protected ClassScanner scanner;

    @Before
    public void startStore() {
        store = new EmbeddedGraphStore("target/jqassistant/" + this.getClass().getSimpleName());
        scanner = new ClassScanner(store, getScanListener());
        store.start();
        store.beginTransaction();
        store.reset();
        store.endTransaction();
    }

    @After
    public void stopStore() {
        store.stop();
    }

    protected ClassScanner.ScanListener getScanListener() {
        return new ClassScanner.ScanListener() {};
    }

}
