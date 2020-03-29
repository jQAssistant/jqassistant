package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import org.junit.jupiter.api.Test;

class C02E03IT extends AbstractYAMLPluginIT {

    private static String YAML_FILE = "/spec-examples/c2-e03-mapping-scalars-to-sequences.yaml";

    @Override
    protected String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }
}
