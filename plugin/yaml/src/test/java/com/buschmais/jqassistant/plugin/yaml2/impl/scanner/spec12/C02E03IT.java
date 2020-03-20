package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class C02E03IT extends AbstractYAMLPluginIT {

    private static String YAML_FILE = "/spec-examples/c2-e03-mapping-scalars-to-sequences.yaml";

    @Override
    protected String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Disabled("Test and scanner are not yet implemented.")
    @Test
    void scannerCanReadDocument() {
        throw new RuntimeException("Please implement me!");
    }
}
