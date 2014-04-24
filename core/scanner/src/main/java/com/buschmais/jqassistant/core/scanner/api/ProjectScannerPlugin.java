package com.buschmais.jqassistant.core.scanner.api;

import java.io.IOException;

/**
 * Defines the interface for plugins for scanning project.
 * 
 */
public interface ProjectScannerPlugin extends ScannerPlugin {

    /**
     * Perform scanning of a project.
     * 
     * @return The descriptor representing the file.
     * @throws IOException
     *             If scanning fails.
     */
    void scan(FileScanner fileScanner) throws IOException;
}
