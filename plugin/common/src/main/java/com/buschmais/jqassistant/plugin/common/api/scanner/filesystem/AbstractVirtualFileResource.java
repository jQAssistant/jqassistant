package com.buschmais.jqassistant.plugin.common.api.scanner.filesystem;

import java.io.*;
import java.nio.file.Files;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Abstract base implementation of a file resource which uses a temporary file.
 * <p>
 * The implementation preserves the original file name including preceding directory structures for plugins which rely on path names.
 */
@Slf4j
public abstract class AbstractVirtualFileResource implements FileResource {

    public static final String TMP_DIR_PREFIX = "jqassistant";

    private File file;

    @Override
    public final File getFile() throws IOException {
        if (this.file == null) {
            this.file = Files.createTempFile(TMP_DIR_PREFIX, null).toFile();
            try (InputStream input = createStream(); BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
                IOUtils.copy(input, output);
            }
        }
        return file;
    }

    protected abstract String getRelativePath() throws IOException;

    @Override
    public final void close() {
        if (this.file != null) {
            try {
                FileUtils.delete(this.file);
            } catch (IOException e) {
                log.warn("Cannot delete file {}", this.file, e);
            }
        }
    }
}
