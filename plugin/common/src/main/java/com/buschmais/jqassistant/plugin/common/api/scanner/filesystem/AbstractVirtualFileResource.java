package com.buschmais.jqassistant.plugin.common.api.scanner.filesystem;

import java.io.*;
import java.nio.file.Files;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Abstract base implementation of a file resource which uses a temporary file.
 *
 * The implementation preserves the original file name including preceding directory structures for plugins which rely on path names.
 */
@Slf4j
public abstract class AbstractVirtualFileResource implements FileResource {

    public static final String TMP_DIR_PREFIX = "jqassistant";

    private File directory;

    private File file;

    @Override
    public final File getFile() throws IOException {
        if (this.file == null) {
            // create a temp directory which will contain the extracted file (structure)
            this.directory = Files.createTempDirectory(TMP_DIR_PREFIX).toFile();
            this.file = new File(this.directory, getName());
            // The file name itself may contain a directory structure, create this structure within the temp dir
            File parentDirectory = this.file.getParentFile();
            if (!parentDirectory.mkdirs()) {
                throw new IllegalStateException("Cannot create directory " + parentDirectory);
            }
            try (InputStream input = createStream(); BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
                IOUtils.copy(input, output);
            }
        }
        return file;
    }

    protected abstract String getName();

    @Override
    public final void close() {
        if (this.directory != null) {
            try {
                FileUtils.deleteDirectory(this.directory);
            } catch (IOException e) {
                log.warn("Cannot delete file resource directory " + this.directory, e);
            }
        }
    }
}
