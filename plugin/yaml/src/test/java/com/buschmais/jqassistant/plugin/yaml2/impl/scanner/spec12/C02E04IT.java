package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import java.util.List;
import java.util.Optional;

import com.buschmais.jqassistant.plugin.yaml2.api.model.*;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.yaml2.helper.TestHelper.*;
import static com.buschmais.jqassistant.plugin.yaml2.helper.YMLPluginAssertions.assertThat;

class C02E04IT extends AbstractYAMLPluginIT {

    private static String YAML_FILE = "/spec-examples/c2-e04-sequence-of-mappings.yaml";

    @Override
    protected String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }

    @Test
    void theDocumentContainsOnlyASequence() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        YMLDocumentDescriptor documentDescriptor = getDocuments(ymlFileDescriptor).getDocumentByParsePosition(0);

        assertThat(documentDescriptor).hasSequences().hasSequences(1);
        assertThat(documentDescriptor).hasNoMaps();
    }

    @Test
    void theSecondListItemIsAMap() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        YMLDocumentDescriptor documentDescriptor = getDocuments(ymlFileDescriptor).getDocumentByParsePosition(0);
        YMLSequenceDescriptor sequenceDescriptor = getSequences(documentDescriptor).getSequenceByParsePosition(0);
        Optional<YMLMapDescriptor> mapDescriptor = sequenceDescriptor.getMaps().stream().filter(m -> m.getIndex() == 1).findFirst();

        assertThat(mapDescriptor).hasValueSatisfying(m -> {
            assertThat(m).isInstanceOf(YMLMapDescriptor.class);
        });
    }

    @Test
    void theSecondMapHasThreeKeys() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        YMLDocumentDescriptor documentDescriptor = getDocuments(ymlFileDescriptor).getDocumentByParsePosition(0);
        YMLSequenceDescriptor sequenceDescriptor = getSequences(documentDescriptor).getSequenceByParsePosition(0);

        Optional<YMLMapDescriptor> firstMap = sequenceDescriptor.getMaps().stream()
                                                                .filter(m -> m.getIndex() == 1)
                                                                .findFirst();

        assertThat(firstMap).hasValueSatisfying(map -> {
            assertThat(map.getKeys()).hasSize(3);
        });
    }

    @Test
    void theSecondMapHasACorrectMappingForAvg() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        YMLDocumentDescriptor documentDescriptor = getDocuments(ymlFileDescriptor).getDocumentByParsePosition(0);
        YMLSequenceDescriptor sequenceDescriptor = getSequences(documentDescriptor).getSequenceByParsePosition(0);

        YMLMapDescriptor mapDescriptor = getMaps(sequenceDescriptor).getMapByParsePosition(1);

        assertThat(mapDescriptor).containsSimpleKeyWithName("avg");
        YMLSimpleKeyDescriptor keyDescriptor = getKeys(mapDescriptor).getKeyByName("avg");

        assertThat(keyDescriptor).hasName("avg").hasScalarValue("0.288");
    }

    @Test
    void cypherTheDocumentContainsToMaps() {
        readSourceDocument();

        String cypherQuery = "MATCH (d:Yaml:Document)" +
                             "-[:HAS_SEQUENCE]->(:Sequence)" +
                             "-[:HAS_ITEM]->(m:Map) " +
                             "RETURN m";

        List<Object> result = query(cypherQuery).getColumn("m");

        assertThat(result).hasSize(2);
    }

    @Test
    void cypherTheSecondMapInTheSequenceContainsTheCorrectMappingForHr() {
        readSourceDocument();

        String cypherQuery = "MATCH (d:Yaml:Document)" +
                             "-[:HAS_SEQUENCE]->(:Sequence)" +
                             "-[:HAS_ITEM]->(m:Map { index: 1})" +
                             "-[:HAS_KEY]->(k:Key {name: 'hr'})" +
                             "-[:HAS_VALUE]->(v:Value) " +
                             "RETURN v.value AS val";

        List<Object> result = query(cypherQuery).getColumn("val");

        assertThat(result).hasSize(1).containsExactly("63");
    }
}
