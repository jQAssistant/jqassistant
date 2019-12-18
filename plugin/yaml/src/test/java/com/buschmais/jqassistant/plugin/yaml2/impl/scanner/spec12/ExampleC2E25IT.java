package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ExampleC2E25IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e25-unordered-sets.yaml";

    @Override
    String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Disabled("Test and scanner are not yet implemented.")
    @Test
    void scannerCanReadDocument() {
        throw new RuntimeException("Please implement me!");
    }
}
