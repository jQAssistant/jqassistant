package com.buschmais.jqassistant.plugin.maven.api.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Optional;

import javax.xml.stream.XMLStreamException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.v4.MavenStaxReader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Implementation of a POM model builder which does no resolving and returns the
 * raw model.
 */
public class RawModelBuilder implements PomModelBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawModelBuilder.class);

    @Override
    public Model getModel(File pomFile) throws IOException {
        try (InputStream stream = new FileInputStream(pomFile)) {
            return getModel(stream, pomFile.getAbsolutePath());
        }
    }

    @Override
    public Model getModel(InputStream stream, String path) {
        // execution context may provide Maven 3 or Maven 4 classes
        // Trying with Maven 4 first, falling back to Maven 3.
        return readModelV4(stream, path).orElseGet(() -> readModelV3(stream, path));
    }

    private static Optional<Model> readModelV4(InputStream stream, String path) {
        Constructor<Model> delegateConstructor;
        try {
            delegateConstructor = Model.class.getDeclaredConstructor(org.apache.maven.api.model.Model.class);
        } catch (NoSuchMethodException e) {
            LOGGER.warn("Delegate constructor for Maven 4 model not found.", e);
            return empty();
        }
        MavenStaxReader mavenStaxReader = new MavenStaxReader();
        try {
            return of(delegateConstructor.newInstance(mavenStaxReader.read(stream)));
        } catch (XMLStreamException e) {
            LOGGER.warn("Cannot read POM descriptor from {}.", path, e);
            return empty();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot create instance for " + Model.class, e);
        }
    }

    private static @Nullable Model readModelV3(InputStream stream, String path) {
        MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
        try {
            return mavenXpp3Reader.read(stream);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read POM model from stream.", e);
        } catch (XmlPullParserException e) {
            LOGGER.warn("Cannot read POM descriptor from {}.", path, e);
        }
        return null;
    }

}
