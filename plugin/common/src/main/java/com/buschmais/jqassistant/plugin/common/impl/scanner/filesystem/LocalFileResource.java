package com.buschmais.jqassistant.plugin.common.impl.scanner.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

/**
 * Represents a local file resource.
 */
public class LocalFileResource implements FileResource {

    private final File file;

    public LocalFileResource(File file) {
        this.file = file;
    }

    @Override
    public InputStream createStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return file.toString();
    }
}
