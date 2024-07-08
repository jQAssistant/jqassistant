package com.buschmais.jqassistant.neo4j.embedded;

import java.lang.instrument.Instrumentation;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Acts as launcher agent for capturing the provided {@link Instrumentation} instance for later use (e.g. adding JARs to the classloader).
 */
@Slf4j
public class InstrumentationProvider {

    public static final InstrumentationProvider INSTANCE = new InstrumentationProvider();

    private Optional<Instrumentation> instrumentation = empty();

    private InstrumentationProvider() {
    }

    public static void agentmain(String args, Instrumentation instrumentation) {
        initInstrumentation(instrumentation);
    }

    public static void premain(String args, Instrumentation instrumentation) {
        initInstrumentation(instrumentation);
    }

    private static void initInstrumentation(Instrumentation instrumentation) {
        if (INSTANCE.instrumentation.isEmpty()) {
            INSTANCE.instrumentation = of(instrumentation);
            log.debug("Runtime instrumentation is now available.");
        }
    }

    public Optional<Instrumentation> getInstrumentation() {
        return instrumentation;
    }
}
