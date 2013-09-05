package com.buschmais.jqassistant.rules.javaee.impl.scanner;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.rules.javaee.impl.store.PersistenceUnitDescriptor;

import java.io.IOException;

/**
 * A scanner for JPA persistence units.
 */
public class PersistenceUnitScanner implements FileScannerPlugin<PersistenceUnitDescriptor> {

    @Override
    public boolean matches(String file, boolean isDirectory) {
        return "META-INF/persistence.xml".equals(file) || "WEB-INF/persistence.xml".equals(file);
    }

    @Override
    public PersistenceUnitDescriptor scanFile(Store store, InputStreamSource streamSource) throws IOException {
        return null;
    }

    @Override
    public PersistenceUnitDescriptor scanDirectory(Store store, String name) throws IOException {
        return null;
    }
}
