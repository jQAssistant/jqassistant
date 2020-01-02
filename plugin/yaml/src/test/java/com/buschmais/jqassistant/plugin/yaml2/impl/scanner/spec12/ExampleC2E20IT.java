package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import org.junit.jupiter.api.Test;

class ExampleC2E20IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e20-floating point.yaml";

    @Override
    protected String getSourceYAMLFile() {
        return YAML_FILE;
    }


    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }

    /* Todo Write more specific tests */
}
