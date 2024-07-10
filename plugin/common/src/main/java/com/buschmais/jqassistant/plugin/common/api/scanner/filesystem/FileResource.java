package com.buschmais.jqassistant.plugin.common.api.scanner.filesystem;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a file resource.
 */
public interface FileResource extends Resource {

    InputStream createStream() throws IOException;

    java.io.File getFile() throws IOException;
}
