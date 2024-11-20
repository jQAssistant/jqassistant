package com.buschmais.jqassistant.core.test;

import com.buschmais.jqassistant.core.test.plugin.AbstractPluginIT;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

class ApocIT extends AbstractPluginIT {

    @Test
    void apocTest() {
        assumeThat(Runtime.version()
            .feature()).isGreaterThanOrEqualTo(17);
        store.beginTransaction();
        Query.Result<CompositeRowObject> rows = store.executeQuery("call apoc.help('apoc')");
        assertThat(rows).isNotEmpty();
        store.commitTransaction();
    }

}
