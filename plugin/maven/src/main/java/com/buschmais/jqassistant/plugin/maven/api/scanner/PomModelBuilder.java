package com.buschmais.jqassistant.plugin.maven.api.scanner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.model.Model;

/**
 * Defines the interface for a POM model builder. If available on the scanner
 * context it will be used for building the POM model.
 */
public interface PomModelBuilder {

    /**
     * Build the Maven model from the given POM file.
     *
     * @param pomFile
     *     The pom file.
     * @return The Maven model.
     * @throws IOException
     *     If model building fails.
     */
    Model getModel(File pomFile) throws IOException;

    Model getModel(InputStream stream, String path);
}
