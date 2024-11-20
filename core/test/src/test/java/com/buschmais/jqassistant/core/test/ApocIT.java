package com.buschmais.jqassistant.core.test;

import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

class ApocIT extends AbstractPluginIT {

    @BeforeAll
    static void setUp() {
        assumeThat(Runtime.version()
            .feature()).isGreaterThanOrEqualTo(17);
    }

    @Test
    void apocTest() {
        store.beginTransaction();
        Query.Result<CompositeRowObject> rows = store.executeQuery("call apoc.help('apoc')");
        assertThat(rows).isNotEmpty();
        store.commitTransaction();
    }

}
