package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.spec12;

import java.util.List;

import com.buschmais.jqassistant.plugin.yaml2.api.model.YMLFileDescriptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static com.buschmais.jqassistant.plugin.yaml2.helper.YMLPluginAssertions.assertThat;

@EnabledIfSystemProperty(named = "jqassistant.yaml2.activate", matches = "^true$")
class ExampleC2E07IT extends AbstractYAMLPluginIT {
    private static String YAML_FILE = "/probes/example-c2-e07-two-documents-in-a-stream.yaml";

    @Override
    protected String getSourceYAMLFile() {
        return YAML_FILE;
    }

    @Test
    void scannerCanReadDocument() {
        readSourceDocument();
    }

    @Test
    void theSourceFileContainsTwoDocuments() {
        YMLFileDescriptor ymlFileDescriptor = readSourceDocument();

        assertThat(ymlFileDescriptor).hasDocuments(2);
    }

    @Test
    void cypherTheSourceFileContainsTwoDocuments() {
        readSourceDocument();

        String cypherQuery = "MATCH (f:File:Yaml)-[:HAS_DOCUMENT]->(d:Document:Yaml) WHERE f.fileName =~ '.*" + YAML_FILE + "' RETURN f";

        List<Object> result = query(cypherQuery).getColumn("f");

        assertThat(result).hasSize(2);
    }
}
