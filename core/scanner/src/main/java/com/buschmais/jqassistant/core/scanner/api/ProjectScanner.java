package com.buschmais.jqassistant.core.scanner.api;

import java.io.IOException;

/**
 * Defines the interface for a project scanner.
 */
public interface ProjectScanner {

    void scan() throws IOException;

}
