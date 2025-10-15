package com.buschmais.jqassistant.core.test;

import java.io.File;
import java.io.IOException;

import com.buschmais.jqassistant.core.shared.configuration.ConfigurationBuilder;
import com.buschmais.jqassistant.core.store.api.configuration.Embedded;
import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.neo4j.graphdb.QueryExecutionException;

import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assumptions.assumeThat;

class ApocIT {

    @BeforeEach
    void setUp() throws IOException {
        assumeThat(Runtime.version()
            .feature()).isGreaterThanOrEqualTo(17);
        deleteDirectory(new File("target/jqassistant/test-store/plugins"));
    }

    @Nested
    class WithAPOC extends AbstractPluginIT {

        @Override
        protected void configure(ConfigurationBuilder configurationBuilder) {
            configurationBuilder.with(Embedded.class, Embedded.APOC_ENABLED, "true");
        }

        @Test
        void apocEnabled() {
            store.beginTransaction();
            Query.Result<CompositeRowObject> rows = store.executeQuery("call apoc.help('apoc')");
            assertThat(rows).isNotEmpty();
            store.commitTransaction();
        }
    }

    @Nested
    class WithoutAPOC extends AbstractPluginIT {

        @Test
        void apocDisabled() {
            store.beginTransaction();
            assertThatExceptionOfType(QueryExecutionException.class)
                .isThrownBy(() -> store.executeQuery("call apoc.help('apoc')"));
            store.rollbackTransaction();
        }
    }

}
