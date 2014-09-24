package com.buschmais.jqassistant.plugin.common.impl.scanner;

import java.io.File;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;

/**
 * Abstract base implementation of a {@link ScannerPlugin}.
 */
public abstract class AbstractScannerPlugin<I> implements ScannerPlugin<I> {

    private Map<String, Object> properties;

    @Override
    public void initialize(Map<String, Object> properties) {
        this.properties = properties;
        initialize();
    }

    /**
     * Initialize the plugin.
     */
    protected abstract void initialize();

    protected Map<String, Object> getProperties() {
        return properties;
    }

    protected String getDirectoryPath(File directory, File entry) {
        String relativePath;
        if (entry.equals(directory)) {
            relativePath = "/";
        } else {
            String filePath = entry.getAbsolutePath();
            String directoryPath = directory.getAbsolutePath();
            relativePath = filePath.substring(directoryPath.length()).replace(File.separator, "/");
        }
        return relativePath;
    }
}
