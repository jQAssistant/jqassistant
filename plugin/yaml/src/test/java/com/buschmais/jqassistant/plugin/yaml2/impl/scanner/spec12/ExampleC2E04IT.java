package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import java.util.List;
import java.util.Optional;

import com.buschmais.jqassistant.plugin.yaml2.api.model.*;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExampleC2E04IT extends AbstractYAMLPluginIT {

    private static String YAML_FILE = "/probes/example-c2-e04-sequence-of-mappings.yaml";

    @Override
    String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }

    @Test
    void theDocumentContainsOnlyASequence() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        YMLDocumentDescriptor documentDescriptor = ymlFileDescriptor.getDocuments().get(0);

        assertThat(documentDescriptor.getSequences()).hasSize(1);
        assertThat(documentDescriptor.getMaps()).isEmpty();
        // todo add later assert for scalars
    }

    @Test
    void theSecondListItemIsAMap() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        List<YMLSequenceDescriptor> sequences = ymlFileDescriptor.getDocuments().get(0).getSequences();
        YMLSequenceDescriptor sequenceDescriptor = sequences.get(0);
        Optional<YMLMapDescriptor> mapDescriptor = sequenceDescriptor.getMaps().stream().filter(m -> m.getIndex() == 1).findFirst();

        assertThat(mapDescriptor).hasValueSatisfying(m -> {
            assertThat(m).isInstanceOf(YMLMapDescriptor.class);
        });
    }

    @Test
    void theSecondMapHasThreeKeys() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        YMLSequenceDescriptor sequenceDescriptor = ymlFileDescriptor.getDocuments().get(0)
                                                                    .getSequences().get(0);

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

        YMLSequenceDescriptor sequenceDescriptor = ymlFileDescriptor.getDocuments().get(0)
                                                                    .getSequences().get(0);

        Optional<YMLMapDescriptor> firstMap = sequenceDescriptor.getMaps().stream()
                                                                .filter(m -> m.getIndex() == 1)
                                                                .findFirst();
        Optional<YMLKeyDescriptor> keyDescriptor = firstMap.get().getKeys().stream().filter(k -> k.getName().equals("avg")).findFirst();

        assertThat(keyDescriptor.get().getName()).isEqualTo("avg");
        YMLScalarDescriptor scalarDescriptor = (YMLScalarDescriptor) keyDescriptor.get().getValue();

        assertThat(scalarDescriptor.getValue()).isEqualTo("0.288");
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
