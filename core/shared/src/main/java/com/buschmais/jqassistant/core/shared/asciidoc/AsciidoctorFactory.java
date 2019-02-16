package com.buschmais.jqassistant.core.shared.asciidoc;

import org.asciidoctor.Asciidoctor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a {@link Asciidoctor} instance which is pre-configured with
 * Asciidoctor Diagram including PlantUML support.
 */
public class AsciidoctorFactory {

    public static final String ATTRIBUTE_IMAGES_OUT_DIR = "imagesoutdir";

    private static final String ASCIIDOCTOR_DIAGRAM = "asciidoctor-diagram";

    private static final Logger LOGGER = LoggerFactory.getLogger(AsciidoctorFactory.class);

    /**
     * Private constructor.
     */
    private AsciidoctorFactory() {
    }

    /**
     * Return the pre-configured {@link Asciidoctor} instance.
     *
     * @return The {@link Asciidoctor} instance.
     */
    public static Asciidoctor getAsciidoctor() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        Asciidoctor asciidoctor = Asciidoctor.Factory.create(contextClassLoader);
        asciidoctor.requireLibrary(ASCIIDOCTOR_DIAGRAM);
        LOGGER.debug("Loaded Asciidoctor {} (using classloader {}).", asciidoctor.asciidoctorVersion(), contextClassLoader);
        return asciidoctor;
    }

}
