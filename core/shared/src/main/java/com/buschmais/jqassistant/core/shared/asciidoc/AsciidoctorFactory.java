package com.buschmais.jqassistant.core.shared.asciidoc;

import org.asciidoctor.Asciidoctor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a {@link Asciidoctor} instance which is pre-configured with
 * Asciidoctor Diagram including PlantUML support.
 */
public class AsciidoctorFactory {

    private static final String ASCIIDOCTOR_DIAGRAM = "asciidoctor-diagram";

    private static final Logger LOGGER = LoggerFactory.getLogger(AsciidoctorFactory.class);

    private static AsciidoctorFactory INSTANCE = new AsciidoctorFactory();

    private Asciidoctor asciidoctor = null;

    /**
     * Private constructor.
     */
    private AsciidoctorFactory() {
    }

    /**
     * Returns the {@link Asciidoctor} instance which is created on demand (due to
     * JRuby/Asciidoctor loading time).
     *
     * @return The Asciidoctor.
     */
    private synchronized Asciidoctor getOrCreateAsciidoctor() {
        if (asciidoctor == null) {
            asciidoctor = Asciidoctor.Factory.create();
            asciidoctor.requireLibrary(ASCIIDOCTOR_DIAGRAM);
            LOGGER.info("Loaded Asciidoctor " + asciidoctor.asciidoctorVersion());
        }
        return asciidoctor;
    }

    /**
     * Return the pre-configured {@link Asciidoctor} instance.
     *
     * @return The {@link Asciidoctor} instance.
     */
    public synchronized static Asciidoctor getAsciidoctor() {
        return INSTANCE.getOrCreateAsciidoctor();
    }

}
