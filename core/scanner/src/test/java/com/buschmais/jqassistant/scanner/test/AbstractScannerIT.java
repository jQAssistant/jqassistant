package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.scanner.ClassScanner;
import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.impl.EmbeddedGraphStore;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;

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

    /**
     * Return the {@link com.buschmais.jqassistant.scanner.ClassScanner.ScanListener} to be used for scanning.
     * <p>The default implementation returns a listener without any functionality, a class may override this method to return a listener implementing specific behavior.</p>
     *
     * @return The {@link com.buschmais.jqassistant.scanner.ClassScanner.ScanListener}.
     */
    protected ClassScanner.ScanListener getScanListener() {
        return new ClassScanner.ScanListener() {
        };
    }

    /**
     * Scans the given classes.
     *
     * @param classes The classes.
     * @throws IOException If scanning fails.
     */
    protected void scanClasses(Class<?>... classes) throws IOException {
        store.beginTransaction();
        scanner.scanClasses(classes);
        store.endTransaction();
    }

}
