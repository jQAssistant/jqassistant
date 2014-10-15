package com.buschmais.jqassistant.scm.cli.test;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import org.junit.Before;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.cli.JQATask;

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

    /**
     * Reset the default store.
     */
    @Before
    public void before() {
        EmbeddedGraphStore store = new EmbeddedGraphStore(JQATask.DEFAULT_STORE_DIRECTORY);
        store.start(Collections.<Class<?>> emptyList());
        store.reset();
        store.stop();
    }

}
