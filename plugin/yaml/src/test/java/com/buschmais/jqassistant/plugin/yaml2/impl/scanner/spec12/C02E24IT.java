package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import org.junit.jupiter.api.Test;

class C02E24IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/spec-examples/example-c2-e24-cglobal-tags.yaml";

    @Override
    protected String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }
}
