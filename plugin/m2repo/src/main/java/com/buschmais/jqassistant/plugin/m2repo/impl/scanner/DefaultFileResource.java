package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;

/**
 * Simple FileRessource for {@link File}s.
 * 
 * @author pherklotz
 */
public class DefaultFileResource implements FileResource {

    private final File file;
    private FileInputStream fis;

    /**
     * Constructs a new object.
     * 
     * @param file
     *            the file
     */
    public DefaultFileResource(File file) {
        this.file = file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        if (fis != null) {
            fis.close();
            fis = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream createStream() throws IOException {
        if (fis != null) {
            close();
        }
        fis = new FileInputStream(getFile());

        return fis;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultFileResource other = (DefaultFileResource) obj;
        if (file == null) {
            if (other.file != null)
                return false;
        } else if (!file.equals(other.file))
            return false;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getFile() throws IOException {
        return this.file;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((file == null) ? 0 : file.hashCode());
        return result;
    }

}
