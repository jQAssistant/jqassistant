package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.util.HashSet;
import java.util.Set;

import com.buschmais.jqassistant.core.shared.aether.configuration.Plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.runtime.api.configuration.JsonSchemaGenerator.validateYaml;
import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void testDescriptionsAndDefaults() {
        ObjectNode pluginNode = JsonSchemaGenerator.generateSchema(Plugin.class);

        // inner descriptions should not be available at the moment on outer level
        assertThat(schemaNode.findValue("plugins")
            .has("description")).isFalse();
        assertThat(schemaNode.findValue("default-plugins")
            .has("description")).isFalse();

        // direct descriptions should be available
        assertThat(schemaNode.findValue("skip")
            .get("description")
            .textValue()).isEqualTo("Skip execution of jQAssistant tasks/goals.");
        assertThat(pluginNode.findValue("group-id")
            .get("description")
            .textValue()).isEqualTo("The groupId of the plugin.");
        assertThat(pluginNode.findValue("classifier")
            .get("description")
            .textValue()).isEqualTo("The classifier of the plugin (optional).");
        assertThat(pluginNode.findValue("artifact-id")
            .get("description")
            .textValue()).isEqualTo("The artifactId of the plugin.");
        assertThat(pluginNode.findValues("type")
            .get(11)
            .get("description")
            .textValue()).isEqualTo("The type (extension) of the plugin.");
        assertThat(pluginNode.findValue("version")
            .get("description")
            .textValue()).isEqualTo("The version of the plugin.");
        assertThat(pluginNode.findValue("exclusions")
            .get("description")
            .textValue()).isEqualTo("The exclusions of the plugin.");

        // defaults should be available
        assertThat(schemaNode.findValue("skip")
            .get("default")
            .textValue()).isEqualTo("false");
        assertThat(pluginNode.findValues("type")
            .get(11)
            .get("default")
            .textValue()).isEqualTo("jar");

    }
}
