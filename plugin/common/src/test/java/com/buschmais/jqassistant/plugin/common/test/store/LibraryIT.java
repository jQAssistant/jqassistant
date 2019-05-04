package com.buschmais.jqassistant.plugin.common.test.store;

import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jConfiguration;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Verifies registration of libraries in the embedded store.
 */
public class LibraryIT extends AbstractPluginIT {

    @Override
    protected EmbeddedNeo4jConfiguration getEmbeddedNeo4jConfiguration() {
        return EmbeddedNeo4jConfiguration.builder().apocEnabled(true).graphAlgorithmsEnabled(true).build();
    }

    @Test
    public void apoc() {
        TestResult result = query("call apoc.help('search')");
        assertThat(result.getRows().isEmpty(), equalTo(false));
    }

    @Test
    public void graphAlgorithms() {
        TestResult result = query("call algo.list()");
        assertThat(result.getRows().isEmpty(), equalTo(false));
    }
}
