package com.buschmais.jqassistant.plugin.common.api.scanner;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a file resource which is created by a scanner.
 */
public interface FileResource {

    InputStream createStream() throws IOException;
}
