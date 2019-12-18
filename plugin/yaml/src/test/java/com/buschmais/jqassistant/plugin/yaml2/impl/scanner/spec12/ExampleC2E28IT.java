package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ExampleC2E28IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e28-log-file.yaml";

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
