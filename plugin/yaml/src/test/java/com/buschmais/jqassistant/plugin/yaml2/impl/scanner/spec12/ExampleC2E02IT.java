package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


class ExampleC2E02IT extends AbstractPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e02-mapping-scalars-to-scalars.yaml";

    @BeforeEach
    void startTransaction() {
        store.beginTransaction();
    }

    @AfterEach
    void commitTransaction() {
        store.commitTransaction();
    }

    @Disabled("Test and scanner are not yet implemented.")
    @Test
    void scannerCanReadDocument() {
        throw new RuntimeException("Please implement me!");
    }
}
