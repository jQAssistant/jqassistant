package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import java.util.List;

import com.buschmais.jqassistant.plugin.yaml2.api.model.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


// todo finish this test!

class ExampleC2E01IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e01-sequence-of-scalars.yaml";

    @Override
    String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @AfterEach
    void commitTransaction() {
        store.commitTransaction();
    }

    @Test
    void thereIsOneDocument() {
        YMLFileDescriptor result = readSourceDocument();

        assertThat(result).isNotNull();
        assertThat(result.getDocuments()).isNotNull().isNotEmpty();
        assertThat(result.getDocuments()).hasSize(1);
    }

    @Test
    void theDocumentContainsASequence() {
        YMLFileDescriptor result = readSourceDocument();

        YMLDocumentDescriptor documentDescriptor = result.getDocuments().get(0);
        assertThat(documentDescriptor.getSequences()).isNotNull().isNotEmpty();

        YMLDescriptor actual = documentDescriptor.getSequences().get(0);
        assertThat(actual).isInstanceOf(YMLSequenceDescriptor.class);
    }

    @Test
    void theSequenceContainsThreeItems() {
        YMLFileDescriptor result = readSourceDocument();

        YMLDocumentDescriptor documentDescriptor = result.getDocuments().get(0);
        YMLSequenceDescriptor sequenceDescriptor = documentDescriptor.getSequences().get(0);
        assertThat(sequenceDescriptor.getItems()).isNotNull()
                                                 .isNotEmpty()
                                                 .hasSize(3);
    }

    @Test
    void theThreeItemsAreTheExpectedStrings() {
        YMLFileDescriptor result = readSourceDocument();

        YMLDocumentDescriptor documentDescriptor = result.getDocuments().get(0);
        YMLSequenceDescriptor sequenceDescriptor = documentDescriptor.getSequences().get(0);
        List<YMLDescriptor> items = sequenceDescriptor.getItems();

        String[] values = items.stream()
            .map(YMLScalarDescriptor.class::cast)
            .map(YMLScalarDescriptor::getValue)
            .toArray(String[]::new);

        assertThat(values).containsExactly("Mark McGwire", "Sammy Sosa", "Ken Griffey");
    }

    @Test
    void viaCypherFileCanBeFound() {
        readSourceDocument();

        List<Object> result = query("MATCH (s:File:Yaml) WHERE s.fileName =~ '.*" +
                               YAML_FILE + "' RETURN s").getColumn("s");

        assertThat(result).hasSize(1);
    }

    @Test
    void viaCypherDocumentCanBeFound() {
        readSourceDocument();

        String cypherQuery = "MATCH (f:File:Yaml) " +
                             "-[:HAS_DOCUMENT]->(d:Yaml:Document) " +
                             "WHERE f.fileName =~ '.*" +
                             YAML_FILE + "' RETURN d";

        List<?> results = query(cypherQuery).getColumn("d");

        assertThat(results).hasSize(1);
    }

    @Test
    void viaCypherSequenceInDocumentCanBeFound() {
        readSourceDocument();

        String cypherQuery = "MATCH (f:File:Yaml) " +
                             "-[:HAS_DOCUMENT]->(d:Yaml:Document) " +
                             "-[:HAS_SEQUENCE]->(s:Yaml:Sequence) " +
                             "WHERE f.fileName =~ '.*" +
                             YAML_FILE + "' RETURN s";

        List<?> results = query(cypherQuery).getColumn("s");

        assertThat(results).hasSize(1);
    }

    @Test
    void viaCypherSequenceValuesCanBeFound() {
        readSourceDocument();

        String cypherQuery = "MATCH (f:File:Yaml) " +
                             "-[:HAS_DOCUMENT]->(d:Yaml:Document) " +
                             "-[:HAS_SEQUENCE]->(s:Yaml:Sequence) " +
                             "-[:HAS_ITEM]->(sc:Yaml:Scalar) " +
                             "WHERE f.fileName =~ '.*" +
                             YAML_FILE + "' RETURN sc.value AS v";

        List<String> results = query(cypherQuery).getColumn("v");

        assertThat(results).hasSize(3)
                           .containsExactly("Mark McGwire",
                                            "Sammy Sosa",
                                            "Ken Griffey");
    }


}
