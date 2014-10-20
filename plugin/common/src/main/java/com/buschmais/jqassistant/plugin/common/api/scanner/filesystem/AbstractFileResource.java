package com.buschmais.jqassistant.plugin.common.api.scanner.filesystem;

import java.io.*;

import org.apache.commons.io.IOUtils;

/**
 * Abstract base implementation of a file resource which uses a temporary file.
 */
public abstract class AbstractFileResource implements FileResource {

    public static final String TMP_FILE_PREFIX = "jqassistant-";
    public static final String TMP_FILE_SUFFIX = ".tmp";

    private File file = null;

    @Override
    public File getFile() throws IOException {
        this.file = File.createTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFFIX);
        try (InputStream input = createStream()) {
            try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
                IOUtils.copy(input, output);
            }
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
