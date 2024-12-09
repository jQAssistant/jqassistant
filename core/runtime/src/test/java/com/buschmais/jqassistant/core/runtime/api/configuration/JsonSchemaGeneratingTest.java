package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaGeneratingTest {

    private final JsonSchemaGenerator generator = new JsonSchemaGenerator();

    JsonNode node;
    File file;

    @BeforeEach
    public void generateSchema() throws IOException {
        String path = "target/generated-resources/schema/jqassistant-configuration.schema.json";
        node = generator.generateSchema(Configuration.class, path); //target/generated-resources/schema/jqassistant-configuration.schema.json
        file = new File(path);
    }

    @Test
    public void validateSchema() throws IOException {
        assertThat(node).isNotNull();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonSchemaFactory bluePrintFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(bluePrintFactory).build();
        JsonSchema schema = schemaFactory.getSchema(node);
        JsonNode rootNode = mapper.readTree(file);
        Set<ValidationMessage> validationMessages = schema.validate(rootNode);
        validationMessages.forEach(msg -> System.out.println("- " + msg.getMessage()));
    }

    @Test
    public void testValidYaml() {
        JsonSchemaFactory bluePrintFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(bluePrintFactory).build();
        JsonSchema schema = schemaFactory.getSchema(node);
        assertThat(validateYaml(schema, "src/test/resources/testdata/generate-schema/validJQAYaml.yaml")).isEqualTo("YAML is valid.");
    }

    @Test
    public void testInvalidYaml() {
        JsonSchemaFactory bluePrintFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(bluePrintFactory).build();
        JsonSchema schema = schemaFactory.getSchema(node);
        assertThat(validateYaml(schema, "src/test/resources/testdata/generate-schema/invalidJQAYaml.yaml")).isEqualTo("YAML is invalid.");
    }

    private String validateYaml(JsonSchema schema, String filePath) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Yaml yaml = new Yaml();
            FileInputStream yamlInputStream = new FileInputStream(filePath);
            Map<String, Object> yamlData = yaml.load(yamlInputStream);
            String jsonString = objectMapper.writeValueAsString(yamlData);
            Set<ValidationMessage> validationMessages = schema.validate(objectMapper.readTree(jsonString));
            if (validationMessages.isEmpty()) {
                System.out.println("Yaml was successfully validated.");
                return "YAML is valid.";
            } else {
                System.out.println("Yaml validation failed on errors:");
                validationMessages.forEach(msg -> System.out.println("- " + msg.getMessage()));
                return "YAML is invalid.";
            }
        } catch (Exception e) {
            System.err.println("Errors: " + e.getMessage());
        }
        return null;
    }

}
