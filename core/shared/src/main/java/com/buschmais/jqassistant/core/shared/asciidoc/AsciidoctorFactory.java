package com.buschmais.jqassistant.core.shared.asciidoc;

import com.buschmais.jqassistant.core.shared.asciidoc.delegate.AsciidoctorDelegate;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.log.LogHandler;
import org.asciidoctor.log.LogRecord;
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
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        asciidoctor.requireLibrary(ASCIIDOCTOR_DIAGRAM);
        asciidoctor.registerLogHandler(new LogHandler() {
            @Override
            public void log(LogRecord logRecord) {
                System.out.println(logRecord.getCursor() + " " + logRecord.getSeverity() + " " + logRecord.getMessage() + " " + logRecord.getSourceFileName());
            }
        });
        LOGGER.debug("Loaded Asciidoctor {}");
        // The delegate is used to fix classloading issues if the CLI plugin classloader
        // is used for adding extensions. Simply passing the required CL to
        // Asciidoctor.Factory#create(ClassLoader) prevents IncludeProcessor to work.
        // Any better solution highly welcome...
        return new AsciidoctorDelegate(asciidoctor);
    }
}
