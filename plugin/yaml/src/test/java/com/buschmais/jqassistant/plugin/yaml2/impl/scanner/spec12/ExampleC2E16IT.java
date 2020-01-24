package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "jqassistant.yaml2.activate", matches = "^true$")
class ExampleC2E16IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e16-indentation-determines-scope.yaml";

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
