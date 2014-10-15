package com.buschmais.jqassistant.scm.cli.test;

import java.io.IOException;
import java.util.Properties;

/**
 * Abstract base implementation for CLI tests.
 */
public abstract class AbstractCLIIT {

    private Properties properties = new Properties();

    protected AbstractCLIIT() {
        try {
            properties.load(AbstractCLIIT.class.getResourceAsStream("/cli-test.properties"));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read cli-test.properties.", e);
        }
    }

}
