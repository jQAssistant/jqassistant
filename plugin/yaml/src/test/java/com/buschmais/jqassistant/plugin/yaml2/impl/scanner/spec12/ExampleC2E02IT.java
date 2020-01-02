package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import java.util.List;
import java.util.stream.Stream;

import com.buschmais.jqassistant.plugin.yaml2.api.model.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.buschmais.jqassistant.plugin.yaml2.helper.TestHelper.getDocuments;
import static com.buschmais.jqassistant.plugin.yaml2.helper.TestHelper.getKeys;
import static com.buschmais.jqassistant.plugin.yaml2.helper.YMLPluginAssertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;


class ExampleC2E02IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e02-mapping-scalars-to-scalars.yaml";

    @Override
    protected String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Test
    void theKeysHaveTheCorrectValuesAssigned() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        YMLMapDescriptor map = getDocuments(ymlFileDescriptor).getDocument(0)
                                                .getMaps().get(0);

        YMLKeyDescriptor keyHr = getKeys(map).getKeyByName("hr");
        YMLKeyDescriptor keyAvg = getKeys(map).getKeyByName("avg");
        YMLKeyDescriptor keyRbi = getKeys(map).getKeyByName("rbi");

        assertThat(keyHr).hasValue().hasScalarAsValue().hasScalarValue("65");
        assertThat(keyAvg).hasValue().hasScalarAsValue().hasScalarValue("0.278");
        assertThat(keyRbi).hasValue().hasScalarAsValue().hasScalarValue("147");
    }


    @Test
    void theKeysHaveTheCorrectKeyNamesAssigned() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        YMLMapDescriptor map = getDocuments(ymlFileDescriptor).getDocument(0)
                                                .getMaps().get(0);

        assertThat(map).containsSimpleKeyWithName("hr");
        assertThat(map).containsSimpleKeyWithName("avg");
        assertThat(map).containsSimpleKeyWithName("rbi");
    }

    @Test
    void thereIsOneDocument() {
        YMLFileDescriptor yamlFileDescriptor = readSourceDocument();

        assertThat(yamlFileDescriptor).hasDocuments(1);
    }

    @Test
    void thereAreThreeKeysInTheMap() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        YMLMapDescriptor map = getDocuments(ymlFileDescriptor).getDocument(0)
                                                              .getMaps().get(0);

        assertThat(map.getKeys()).isNotEmpty().hasSize(3);
        assertThat(map).containsSimpleKeyWithName("hr");
        assertThat(map).containsSimpleKeyWithName("rbi");
        assertThat(map).containsSimpleKeyWithName("avg");
    }

    @Test
    void theDocumentContainsAMap() {
        YMLFileDescriptor yamlFileDescriptor = readSourceDocument();
        YMLDocumentDescriptor documentDescriptor = getDocuments(yamlFileDescriptor).getDocument(0);

        assertThat(documentDescriptor).hasMaps(1);
        assertThat(documentDescriptor).hasNoSequences();
    }

    @Test
    void viaCypherFileCanBeFound() {
        readSourceDocument();

        List<Object> result = query("MATCH (f:File:Yaml) WHERE f.fileName =~ '.*" +
                                    YAML_FILE + "' RETURN f").getColumn("f");

        assertThat(result).hasSize(1);
    }

    @Test
    void viaCypherDocumentCanBeFound() {
        readSourceDocument();

        String cypherQuery = "MATCH (f:File:Yaml) " +
                             "-[:HAS_DOCUMENT]->(d:Yaml:Document) " +
                             "WHERE f.fileName =~ '.*" +
                             YAML_FILE + "' RETURN d";

        List<?> result = query(cypherQuery).getColumn("d");

        assertThat(result).hasSize(1);
    }

    @Test
    void viaCypherMapInDocumentCanBeFound() {
        readSourceDocument();

        String cypherQuery = "MATCH (f:File:Yaml) " +
                             "-[:HAS_DOCUMENT]->(d:Yaml:Document)" +
                             "-[:HAS_MAP]->(m:Yaml:Map) " +
                             "WHERE f.fileName =~ '.*" +
                             YAML_FILE + "' RETURN m";

        List<?> result = query(cypherQuery).getColumn("m");

        assertThat(result).isNotEmpty().hasSize(1);
    }

    @Test
    void viaCypherKeysCanBeCounted() {
        readSourceDocument();

        String cypherQuery = "MATCH (f:File:Yaml) " +
                             "-[:HAS_DOCUMENT]->(d:Yaml:Document)" +
                             "-[:HAS_MAP]->(m:Yaml:Map)" +
                             "-[:HAS_KEY]->(k:Yaml:Key)" +
                             "WHERE f.fileName =~ '.*" +
                             YAML_FILE + "' RETURN count(k) AS c";

        List<Long> result = query(cypherQuery).getColumn("c");

        assertThat(result).isNotEmpty().hasSize(1);
        assertThat(result.get(0)).isEqualTo(Long.valueOf(3));
    }


    @MethodSource("mappingProvider")
    @ParameterizedTest
    void viaCypherAllMappingsCanBeFound(String key, String expectedValue) {
        readSourceDocument();

        String cypherQuery = "MATCH (f:File:Yaml) " +
                             "-[:HAS_DOCUMENT]->(d:Yaml:Document)" +
                             "-[:HAS_MAP]->(m:Yaml:Map)" +
                             "-[:HAS_KEY]->(k:Yaml:Key { name : '" + key + "'})" +
                             "-[:HAS_VALUE]->(v:Yaml:Value:Scalar) " +
                             "WHERE f.fileName =~ '.*" +
                             YAML_FILE + "' RETURN v.value AS value";

        List<?> result = query(cypherQuery).getColumn("value");

        assertThat(result).isNotEmpty().hasSize(1);
        assertThat(result.get(0)).isEqualTo(expectedValue);
    }

    static Stream<Arguments> mappingProvider() {
        return Stream.of(arguments("hr", "65"),
                         arguments("avg", "0.278"),
                         arguments("rbi", "147"));
    }

}
