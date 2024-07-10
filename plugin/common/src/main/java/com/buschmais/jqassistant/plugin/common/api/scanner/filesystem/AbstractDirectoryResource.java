package com.buschmais.jqassistant.plugin.common.api.scanner.filesystem;

/**
 * Abstract base implementation of a virtual directory.
 */
public class AbstractDirectoryResource implements DirectoryResource {
    private String path;

    public AbstractDirectoryResource(String entryPath) {
        path = entryPath;
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return path;
    }
}
