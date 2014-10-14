package com.buschmais.jqassistant.plugin.common.api.scanner.filesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

/**
 * Abstract base implementation of a virtual file.
 */
public abstract class AbstractVirtualFile implements VirtualFile {

    private File file = null;

    @Override
    public File getFile() {
        try {
            this.file = File.createTempFile("jqassistant-", ".tmp");
            IOUtils.copy(createStream(), new FileOutputStream(file));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read stream", e);
        }
        return file;
    }

    @Override
    public void close() {
        if (file != null) {
            file.delete();
        }
    }
}
