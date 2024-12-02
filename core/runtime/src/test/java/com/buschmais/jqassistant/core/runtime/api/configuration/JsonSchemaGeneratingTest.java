package com.buschmais.jqassistant.core.runtime.api.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.smallrye.config.ConfigMapping;
import org.yaml.snakeyaml.Yaml;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaGeneratingTest {


    private final JsonSchemaGenerator generator = new JsonSchemaGenerator();

    public String validateYaml(JsonSchema schema, String filePath) throws IOException {
        JsonNode node = generator.generateSchema(JQAssistant.class);
        File file = new File("target/test-classes/jsonSchema.schema.json");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, node);

        try{
        Yaml yaml = new Yaml();
        FileInputStream yamlInputStream = new FileInputStream(filePath);
        Map<String, Object> yamlData = yaml.load(yamlInputStream);
        String jsonString = objectMapper.writeValueAsString(yamlData);
        Set<ValidationMessage> validationMessages = schema.validate(objectMapper.readTree(jsonString));
        if (validationMessages.isEmpty()) {
            return "YAML is valid.";
        } else {

            validationMessages.forEach(msg -> System.out.println("- " + msg.getMessage()));
            return "YAML is invalid.";
        }
        } catch (Exception e) {
            System.err.println("Errors: " + e.getMessage());
        }
        return null;
    }

    @Test
    public void testValidYaml() throws IOException {
        JsonNode node = generator.generateSchema(JQAssistant.class);
        assertThat(node).isNotNull();
        JsonSchemaFactory bluePrintFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(bluePrintFactory).build();
        JsonSchema schema = schemaFactory.getSchema(node);
        assertThat(validateYaml(schema, "src/test/resources/testdata/generate-schema/validJQAYaml.yaml")).isEqualTo("YAML is valid.");
    }

    @Test
    public void testInvalidYaml() throws IOException {
        JsonNode node = generator.generateSchema(JQAssistant.class);
        assertThat(node).isNotNull();
        JsonSchemaFactory bluePrintFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(bluePrintFactory).build();
        JsonSchema schema = schemaFactory.getSchema(node);
        assertThat(validateYaml(schema, "src/test/resources/testdata/generate-schema/invalidJQAYaml.yaml")).isEqualTo("YAML is invalid.");
    }

    @Test
    public void generateSchemaAndValidTest() throws IOException {
        JsonNode node = generator.generateSchema(JQAssistant.class);
        assertThat(node).isNotNull();
        File file = new File("src/test/resources/jqassistant-configuration.schema.json");
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonSchemaFactory bluePrintFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(bluePrintFactory).build();
        JsonSchema schema = schemaFactory.getSchema(node);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, node);
        JsonNode rootNode = mapper.readTree(file);
        Set<ValidationMessage> validationMessages = schema.validate(rootNode);
        System.out.println(validationMessages);
    }

    @ConfigMapping
    private interface JQAssistant {
        Configuration jqassistant();
    }
}
