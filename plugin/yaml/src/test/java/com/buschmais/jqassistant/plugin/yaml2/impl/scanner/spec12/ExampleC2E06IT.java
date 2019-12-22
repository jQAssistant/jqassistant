package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import java.util.List;

import com.buschmais.jqassistant.plugin.yaml2.api.model.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.yaml2.helper.TestHelper.*;
import static com.buschmais.jqassistant.plugin.yaml2.helper.YMLPluginAssertions.assertThat;

class ExampleC2E06IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e06-mapping-of-mappings.yaml";

    @Override
    String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }

    @Test
    void theDocumentContainsOnlyAMap() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();
        YMLDocumentDescriptor ymlDocumentDescriptor = getDocuments(ymlFileDescriptor).getDocument(0);

        assertThat(ymlDocumentDescriptor).hasMaps(1);
        assertThat(ymlDocumentDescriptor).hasNoSequences();
        assertThat(ymlDocumentDescriptor).hasNoScalars();
    }

    @Disabled("Depends on https://github.com/buschmais/extended-objects/issues/174")
    @Test
    void theMapOfTheDocumentHasTwoMappings() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();
        YMLDocumentDescriptor ymlDocumentDescriptor = getDocuments(ymlFileDescriptor).getDocument(0);
        YMLSequenceDescriptor sequence = getSequences(ymlDocumentDescriptor).getSequence(0);

        // todo Finish this test if https://github.com/buschmais/extended-objects/issues/174 has been fixed
        // assertThat(sequence).has

        throw new RuntimeException("This test is not implemented.");
    }

    @Test
    void theValueOfEachKeyIsAMap() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();
        YMLDocumentDescriptor ymlDocumentDescriptor = getDocuments(ymlFileDescriptor).getDocument(0);
        YMLMapDescriptor ymlMapDescriptor = getMaps(ymlDocumentDescriptor).getMap(0);

        YMLKeyDescriptor keyValue1 = getKeys(ymlMapDescriptor).getKeyByName("Mark McGwire");
        YMLKeyDescriptor keyValue2 = getKeys(ymlMapDescriptor).getKeyByName("Sammy Sosa");

        assertThat(keyValue1).hasMapAsValue();
        assertThat(keyValue2).hasMapAsValue();
    }

    @Test
    void theMappingsOfTheSecondMappingOfTheMainMapAreCorrect() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();
        YMLDocumentDescriptor ymlDocumentDescriptor = getDocuments(ymlFileDescriptor).getDocument(0);
        YMLMapDescriptor ymlMapDescriptor = ymlDocumentDescriptor.getMaps().get(0);
        YMLKeyDescriptor key = getKeys(ymlMapDescriptor).getKeyByName("Sammy Sosa");
        YMLMapDescriptor map = (YMLMapDescriptor) key.getValue();

        YMLKeyDescriptor hrKey = getKeys(map).getKeyByName("hr");
        YMLKeyDescriptor avgKey = getKeys(map).getKeyByName("avg");

        assertThat(hrKey).hasScalarValue("63");
        assertThat(avgKey).hasScalarValue("0.288");
    }

    @Test
    void cypherMainMapOfTheDocumentCanBeFound() {
        readSourceDocument();

        String cypherQuey = "MATCH (d:Yaml:Document)-[:HAS_MAP]->" +
                            "(m:Yaml) " +
                            "RETURN m";

        List<YMLDescriptor> result = query(cypherQuey).getColumn("m");

        assertThat(result).isNotEmpty().hasSize(1);
    }

    @Test
    void cypherTheMappingForSammySosaCanBeFound() {
        readSourceDocument();

        String cypherQuery = "MATCH \n" +
                             "(d:Yaml:Document)\n" +
                             "-[:HAS_MAP]->(:Yaml)\n" +
                             "-[:HAS_KEY]->(:Key {name: 'Sammy Sosa'})\n" +
                             "-[:HAS_VALUE]->(m:Map:Yaml)\n" +
                             "RETURN m";

        List<YMLDescriptor> result = query(cypherQuery).getColumn("m");

        assertThat(result).hasSize(1);
        assertThat(result).element(0).isInstanceOf(YMLMapDescriptor.class);
    }

    @Test
    void cypherTheValueForTheKeyHrForSammySosaCanBeFound() {
        readSourceDocument();

        String cypherQuery = "MATCH \n" +
                             "(d:Yaml:Document)-[:HAS_MAP]->\n" +
                             "(m1:Yaml)\n" +
                             "-[:HAS_KEY]->(k1:Key {name: 'Sammy Sosa'})\n" +
                             "-[:HAS_VALUE]->(m2:Yaml:Map)\n" +
                             "-[:HAS_KEY]->(k2:Key {name: 'hr'})\n" +
                             "-[:HAS_VALUE]->(v:Value)\n" +
                             "RETURN v";

        List<YMLDescriptor> result = query(cypherQuery).getColumn("v");

        assertThat(result).isNotEmpty().hasSize(1);
    }

    @Test
    void cypherValueOfMapCanBeFoundViaTheValueLabel() {
        readSourceDocument();

        String cypherQuery = "MATCH (v:Value)\n" +
                             "WHERE v.value = '65'\n" +
                             "return v.value AS v";

        List<String> result = query(cypherQuery).getColumn("v");

        assertThat(result).isNotEmpty().hasSize(1);
        assertThat(result.get(0)).isEqualTo("65");
    }
}
