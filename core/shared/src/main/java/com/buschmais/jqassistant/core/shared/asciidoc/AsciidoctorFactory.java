package com.buschmais.jqassistant.core.shared.asciidoc;

import com.buschmais.jqassistant.core.shared.asciidoc.delegate.AsciidoctorDelegate;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.log.LogHandler;
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

    private static final String ASCIIDOCTOR_LOG_FORMAT = "{}";

    public static final LogHandler LOG_HANDLER = logRecord -> {

        String message = logRecord.getMessage();
        switch (logRecord.getSeverity()) {
        case FATAL:
        case ERROR:
            LOGGER.error(ASCIIDOCTOR_LOG_FORMAT, message);
            break;
        case WARN:
            LOGGER.warn(ASCIIDOCTOR_LOG_FORMAT, message);
            break;
        case DEBUG:
            LOGGER.debug(ASCIIDOCTOR_LOG_FORMAT, message);
            break;
        default:
            LOGGER.info(ASCIIDOCTOR_LOG_FORMAT, message);
            break;
        }
    };

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
        asciidoctor.registerLogHandler(LOG_HANDLER);

        LOGGER.debug("Loaded Asciidoctor {}");
        // The delegate is used to fix classloading issues if the CLI plugin classloader
        // is used for adding extensions. Simply passing the required CL to
        // Asciidoctor.Factory#create(ClassLoader) prevents IncludeProcessor to work.
        // Any better solution highly welcome...
        return new AsciidoctorDelegate(asciidoctor);
    }
}
