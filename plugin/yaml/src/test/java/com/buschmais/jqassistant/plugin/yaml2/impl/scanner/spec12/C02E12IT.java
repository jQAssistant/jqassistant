package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLDocumentDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLFileDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLMapDescriptor;
import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLSequenceDescriptor;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.yaml2.helper.TestHelper.*;
import static com.buschmais.jqassistant.plugin.yaml2.helper.YMLPluginAssertions.assertThat;

class C02E12IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/spec-examples/c2-e12-compact-nested-mapping.yaml";

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

        YMLDocumentDescriptor document = getDocuments(ymlFileDescriptor).getDocumentByParsePosition(0);

        assertThat(document).hasSequences(1);
        YMLSequenceDescriptor sequence = getSequences(document).getSequenceByParsePosition(0);
        YMLMapDescriptor map0 = getMaps(sequence).getMapByParsePosition(0);
        YMLMapDescriptor map2 = getMaps(sequence).getMapByParsePosition(2);

        assertThat(map0).isNotEqualTo(map2);

        assertThat(map0).containsSimpleKeyWithName("item")
                        .containsSimpleKeyWithName("quantity");

        assertThat(map2).containsSimpleKeyWithName("item")
                        .containsSimpleKeyWithName("quantity");
    }
}
