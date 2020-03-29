package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import org.junit.jupiter.api.Test;

class C02E19IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/spec-examples/example-c2-e19-integers.yaml";

    @Override
    protected String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }

}
