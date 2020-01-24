package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDocumentDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLFileDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLMapDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLSequenceDescriptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static com.buschmais.jqassistant.plugin.yaml2.helper.TestHelper.*;
import static com.buschmais.jqassistant.plugin.yaml2.helper.YMLPluginAssertions.assertThat;

@EnabledIfSystemProperty(named = "jqassistant.yaml2.activate", matches = "^true$")
class ExampleC2E12IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e12-compact-nested-mapping.yaml";

    @Override
    protected String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }

    @Test
    void documentContainsASequence() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        YMLDocumentDescriptor document = getDocuments(ymlFileDescriptor).getDocument(0);

        assertThat(document).hasSequences(1);
        YMLSequenceDescriptor sequence = getSequences(document).getSequence(0);
        YMLMapDescriptor map0 = getMaps(sequence).getMap(0);
        YMLMapDescriptor map2 = getMaps(sequence).getMap(2);

        assertThat(map0).isNotEqualTo(map2);

        assertThat(map0).containsSimpleKeyWithName("item")
                        .containsSimpleKeyWithName("quantity");

        assertThat(map2).containsSimpleKeyWithName("item")
                        .containsSimpleKeyWithName("quantity");
    }
}
