package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import org.junit.jupiter.api.Test;

class ExampleC2E13IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e13-in-literals-newlines-are-preserved.yaml";

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
