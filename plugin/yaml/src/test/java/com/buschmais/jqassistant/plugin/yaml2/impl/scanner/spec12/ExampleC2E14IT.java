package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import org.junit.jupiter.api.Test;

class ExampleC2E14IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e14-in-the-folded-scalars-newlines-become spaces.yaml";

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
