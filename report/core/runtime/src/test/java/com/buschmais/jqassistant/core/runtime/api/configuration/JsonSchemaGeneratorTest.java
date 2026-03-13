package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.runtime.api.configuration.JsonSchemaGenerator.validateYaml;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.org.webcompere.modelassert.json.JsonAssertions.assertJson;

class JsonSchemaGeneratorTest {

    private JsonNode schemaNode;

    @BeforeEach
    void generateSchema() {
        schemaNode = JsonSchemaGenerator.generateSchema(Configuration.class);
    }

    @Test
    void testValidYaml() throws Exception {
        assertThat(validateYaml(JsonSchemaGenerator.class.getResource("/testdata/generate-schema/validJQAYaml.yaml"), schemaNode)).isEmpty();
    }

    @Test
    void testInvalidYaml() throws Exception {
        Set<String> validationResults = new HashSet<>();
        for (Error message : validateYaml(JsonSchemaGenerator.class.getResource("/testdata/generate-schema/invalidJQAYaml.yaml"), schemaNode)) {
            validationResults.add(message.getInstanceLocation()
                .toString());
        }

        Set<String> expectedPaths = new HashSet<>();
        expectedPaths.add("/jqassistant/analyze/report/properties");
        expectedPaths.add("/jqassistant/plugins/0");
        expectedPaths.add("/jqassistant/scan/include/files");

        for (String path : expectedPaths) {
            assertThat(validationResults).contains(path);
        }
    }

    @Test
    void testDescriptions() {
        // inner descriptions should not be available on outer level at the moment
        assertJson(schemaNode).at("/$defs/com.buschmais.jqassistant.core.runtime.api.configuration.-plugin/description")
            .isMissing();

        // direct descriptions should be available
        assertJson(schemaNode).at("/$defs/com.buschmais.jqassistant.core.runtime.api.configuration.-configuration/properties/skip/description")
            .isText("Skip execution of jQAssistant tasks/goals.");
    }

    @Test
    void testDefaults() {
        // default values should be available
        assertJson(schemaNode).at("/$defs/com.buschmais.jqassistant.core.runtime.api.configuration.-configuration/properties/skip/default")
            .isText("false");
    }
}
