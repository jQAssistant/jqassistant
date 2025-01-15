package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.runtime.api.configuration.JsonSchemaGenerator.validateYaml;
import static org.assertj.core.api.Assertions.assertThat;

class JsonSchemaGeneratorTest {

    private JsonNode schemaNode;

    @BeforeEach
    void generateSchema() throws IOException {
        schemaNode = JsonSchemaGenerator.generateSchema(Configuration.class);
    }

    @Test
    void testValidYaml() throws Exception {
        assertThat(validateYaml(JsonSchemaGenerator.class.getResource("/testdata/generate-schema/validJQAYaml.yaml"), schemaNode)).isEmpty();
    }

    @Test
    void testInvalidYaml() throws Exception {
        Set<ValidationMessage> messages = validateYaml(JsonSchemaGenerator.class.getResource("/testdata/generate-schema/invalidJQAYaml.yaml"), schemaNode);
        Set<String> validationResults = new HashSet<>();
        for (ValidationMessage message : messages) {
            validationResults.add(message.getInstanceLocation()
                .toString());
        }

        Set<String> expectedPaths = new HashSet<>();
        expectedPaths.add("$.jqassistant.analyze.report.properties");
        expectedPaths.add("$.jqassistant.plugins[0]");
        expectedPaths.add("$.jqassistant.scan.include.files");

        for (String path : expectedPaths) {
            assertThat(validationResults).contains(path);
        }
    }

}
