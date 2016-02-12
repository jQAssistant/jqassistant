package com.buschmais.jqassistant.plugin.common.api.scanner.filesystem;

import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * Represents a ZIP file resource.
 */
public class ZipFileResource implements Closeable {
    private final ZipFile zipFile;
    private final String path;

    public ZipFileResource(File file) throws IOException {
        path = file.getPath();
        this.zipFile = new ZipFile(file);
    }

    @Override
    public String toString() {
        return getPath();
    }

    public String getPath() {
        return path;
    }

    @Override
    public void close() throws IOException {
        zipFile.close();
    }

    /**
     * Returns the underlying {@link ZipFile ZIP file}.
     *
     * @return Returns the underlaying ZIP file, never `null`.
     */
    public ZipFile getZipFile() {
        return zipFile;
    }
}
