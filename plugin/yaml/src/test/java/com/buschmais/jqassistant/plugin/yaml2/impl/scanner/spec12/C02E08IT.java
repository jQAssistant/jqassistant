package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLFileDescriptor;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.yaml2.helper.YMLPluginAssertions.assertThat;

class C02E08IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/spec-examples/c2-e08-play-by-play-feed-from-a-game.yaml";

    /* Note on the implemented tests
     * This test class tests only specific aspects of the document
     * under test as a lot of the basic features is already tested
     * by other tests.
     * Oliver Fischer // 2019-12-22
     */

    @Override
    protected String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }

    @Test
    void theFileContaintsTwoDocuments() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        assertThat(ymlFileDescriptor).hasDocuments(2);
    }
}
