package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.buschmais.jqassistant.plugin.yaml2.api.model.*;
import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.YMLFileScannerPlugin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;


class ExampleC2E02IT extends AbstractPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e02-mapping-scalars-to-scalars.yaml";

    @BeforeEach
    void startTransaction() {
        store.beginTransaction();
    }

    @AfterEach
    void commitTransaction() {
        store.commitTransaction();
    }

    YMLFileDescriptor readSourceDocument() {
        File yamlFile = new File(getClassesDirectory(YMLFileScannerPlugin.class), YAML_FILE);

        return getScanner().scan(yamlFile, yamlFile.getAbsolutePath(), null);
    }

    @Test
    void theKeysHaveTheCorrectValuesAssigned() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        YMLMapDescriptor map = ymlFileDescriptor.getDocuments().get(0)
                                                .getMaps().get(0);

        Optional<YMLKeyDescriptor> keyHr = map.getKeys().stream().filter(k -> k.getName().equals("hr")).findAny();
        Optional<YMLKeyDescriptor> keyAvg = map.getKeys().stream().filter(k -> k.getName().equals("avg")).findAny();
        Optional<YMLKeyDescriptor> keyRbi = map.getKeys().stream().filter(k -> k.getName().equals("rbi")).findAny();

        assertThat(keyHr).get().extracting(YMLKeyDescriptor::getValue)
                         .isNotNull()
                         .isInstanceOfSatisfying(YMLScalarDescriptor.class, descriptor -> {
                             assertThat(descriptor).extracting(YMLScalarDescriptor::getValue)
                                                   .isEqualTo("65");
                         });

        assertThat(keyAvg).get().extracting(YMLKeyDescriptor::getValue)
                          .isNotNull()
                          .isInstanceOfSatisfying(YMLScalarDescriptor.class, descriptor -> {
                              assertThat(descriptor).extracting(YMLScalarDescriptor::getValue)
                                                    .isEqualTo("0.278");
                          });

        assertThat(keyRbi).get().extracting(YMLKeyDescriptor::getValue)
                          .isNotNull()
                          .isInstanceOfSatisfying(YMLScalarDescriptor.class, descriptor -> {
                              assertThat(descriptor).extracting(YMLScalarDescriptor::getValue)
                                                    .isEqualTo("147");
                          });
    }


    @Test
    void theKeysHaveTheCorrectKeyNamesAssigned() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        YMLMapDescriptor map = ymlFileDescriptor.getDocuments().get(0)
                                                .getMaps().get(0);

        Optional<YMLKeyDescriptor> keyHr = map.getKeys().stream().filter(k -> k.getName().equals("hr")).findAny();
        Optional<YMLKeyDescriptor> keyAvg = map.getKeys().stream().filter(k -> k.getName().equals("avg")).findAny();
        Optional<YMLKeyDescriptor> keyRbi = map.getKeys().stream().filter(k -> k.getName().equals("rbi")).findAny();

        assertThat(keyHr).get().returns("hr", YMLKeyDescriptor::getName);
        assertThat(keyAvg).get().returns("avg", YMLKeyDescriptor::getName);
        assertThat(keyRbi).get().returns("rbi", YMLKeyDescriptor::getName);
    }

    @Test
    void thereIsOneDocument() {
        YMLFileDescriptor yamlFileDescriptor = readSourceDocument();

        assertThat(yamlFileDescriptor.getDocuments()).hasSize(1);
    }

    @Test
    void thereAreThreeKeysInTheMap() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        YMLMapDescriptor map = ymlFileDescriptor.getDocuments().get(0)
                                                .getMaps().get(0);

        assertThat(map.getKeys()).isNotEmpty().hasSize(3);

        List<String> keys = map.getKeys().stream().map(YMLKeyDescriptor::getName).collect(Collectors.toList());

        assertThat(keys).containsExactlyInAnyOrder("hr", "avg", "rbi");
    }

    @Test
    void theDocumentContainsAMap() {
        YMLFileDescriptor yamlFileDescriptor = readSourceDocument();
        YMLDocumentDescriptor documentDescriptor = yamlFileDescriptor.getDocuments().get(0);

        assertThat(documentDescriptor.getMaps()).hasSize(1);
        assertThat(documentDescriptor.getSequences()).hasSize(0);
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
