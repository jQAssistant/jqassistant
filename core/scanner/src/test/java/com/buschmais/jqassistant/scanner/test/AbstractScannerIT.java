package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.scanner.impl.ClassScannerImpl;
import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.impl.EmbeddedGraphStore;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;

public abstract class AbstractScannerIT {

    protected Store store;

    protected ClassScannerImpl scanner;

    @Before
    public void startStore() {
        store = new EmbeddedGraphStore("target/jqassistant/" + this.getClass().getSimpleName());
        scanner = new ClassScannerImpl(store, getScanListener());
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
     * Return the {@link com.buschmais.jqassistant.scanner.impl.ClassScannerImpl.ScanListener} to be used for scanning.
     * <p>The default implementation returns a listener without any functionality, a class may override this method to return a listener implementing specific behavior.</p>
     *
     * @return The {@link com.buschmais.jqassistant.scanner.impl.ClassScannerImpl.ScanListener}.
     */
    protected ClassScannerImpl.ScanListener getScanListener() {
        return new ClassScannerImpl.ScanListener() {
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
