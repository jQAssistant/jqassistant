package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonSchemaGeneratingTest {

    private final JsonSchemaGenerator generator = new JsonSchemaGenerator();

    private JsonNode node;

    @BeforeEach
    void generateSchema() throws IOException {
        String path = "target/generated-resources/schema/jqassistant-configuration.schema.json";
        node = generator.generateSchema(Configuration.class, path);
    }

    @Test
    void testValidYaml() throws Exception {
        assertThat(generator.validateYaml("src/test/resources/testdata/generate-schema/validJQAYaml.yaml", node)).isEmpty();
    }

    @Test
    void testInvalidYaml() throws Exception {
        Set<ValidationMessage> messages = generator.validateYaml("src/test/resources/testdata/generate-schema/invalidJQAYaml.yaml", node);
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
