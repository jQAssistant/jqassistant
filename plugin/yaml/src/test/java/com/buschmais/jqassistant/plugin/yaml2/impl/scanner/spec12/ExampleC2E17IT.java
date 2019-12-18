package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class ExampleC2E17IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e17-quoted-scalars.yaml";

    @Override
    String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }

    @Test
    void cypherDocumentContainsMapWithSixKeys() {
        readSourceDocument();

        String cypherQuery = "MATCH (f:File:Yaml) " +
                             "-[:HAS_DOCUMENT]->(d:Yaml:Document)" +
                             "-[:HAS_MAP]->(m:Yaml:Map)" +
                             "-[:HAS_KEY]->(k:Yaml:Key)" +
                             "WHERE f.fileName =~ '.*" +
                             YAML_FILE + "' RETURN count(k) AS c";

        List<Long> result = query(cypherQuery).getColumn("c");

        assertThat(result).isNotEmpty().hasSize(1);
        assertThat(result.get(0)).isEqualTo(Long.valueOf(6));
    }

    @ParameterizedTest
    @MethodSource("mappingProvider")
    void cypherValueForEachKeyIsCorrect(String key, String expectedValue) {
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
        return Stream.of(arguments("unicode", "Sosa did fine.\u263A"),
                         arguments("control", "\b1998\t1999\t2000\n"),
                         arguments("hex esc", "\r\n is \r\n"),
                         arguments("single", "\"Howdy!\" he cried."),
                         arguments("quoted", " # Not a 'comment'."),
                         arguments("tie-fighter", "|\\-*-/|"));
    }

}
