package com.buschmais.jqassistant.plugin.maven.api.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import org.apache.maven.api.model.Model;
import org.apache.maven.model.v4.MavenStaxReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a POM model builder which does no resolving and returns the
 * raw model.
 */
public class RawModelBuilder implements PomModelBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawModelBuilder.class);

    private final MavenStaxReader mavenStaxReader;

    public RawModelBuilder() {
        this.mavenStaxReader = new MavenStaxReader();
    }

    @Override
    public Model getModel(File pomFile) throws IOException {
        try (InputStream stream = new FileInputStream(pomFile)) {
            return getModel(stream, pomFile.getAbsolutePath());
        }
    }

    @Override
    public Model getModel(InputStream stream, String path) {
        try {
            return mavenStaxReader.read(stream);
        } catch (XMLStreamException e) {
            LOGGER.warn("Cannot read POM descriptor from {}.", path, e);
            return null;
        }
    }
}
