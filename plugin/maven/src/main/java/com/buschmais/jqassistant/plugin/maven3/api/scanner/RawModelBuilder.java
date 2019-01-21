package com.buschmais.jqassistant.plugin.maven3.api.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a POM model builder which does no resolving and returns the
 * raw model.
 */
public class RawModelBuilder implements PomModelBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawModelBuilder.class);

    private MavenXpp3Reader mavenXpp3Reader;

    public RawModelBuilder() {
        this.mavenXpp3Reader = new MavenXpp3Reader();
    }

    @Override
    public Model getModel(File pomFile) throws IOException {
        try (InputStream stream = new FileInputStream(pomFile)) {
            return mavenXpp3Reader.read(stream, false); // non-strict mode
        } catch (XmlPullParserException e) {
            LOGGER.debug("Cannot read POM descriptor from " + pomFile.getAbsolutePath() + ".", e);
            return null;
        }
    }
}
