package com.buschmais.jqassistant.commandline.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.runtime.api.configuration.JsonSchemaGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import static io.smallrye.config._private.ConfigLogging.log;
import static org.assertj.core.api.Assertions.assertThat;

class CliJsonSchemaGeneratorTest {

    private final JsonSchemaGenerator generator = new JsonSchemaGenerator();
    private JsonNode node;

    @BeforeEach
    void generateSchema() throws IOException {
        String path = "target/generated-resources/schema/jqassistant-configuration-cli.schema.json";
        node = generator.generateSchema(CliConfiguration.class, path);
    }

    @Test
    void testValidYaml() throws Exception {
        assertThat(validateYaml("src/test/resources/validCliYaml.yaml")).isEmpty();
    }

    private Set<ValidationMessage> validateYaml(String filePath) throws Exception {
        JsonSchemaFactory bluePrintFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(bluePrintFactory)
            .build();
        JsonSchema schema = schemaFactory.getSchema(node);
        ObjectMapper objectMapper = new ObjectMapper();
        Yaml yaml = new Yaml();
        FileInputStream yamlInputStream = new FileInputStream(filePath);
        Map<String, Object> yamlData = yaml.load(yamlInputStream);
        String jsonString = objectMapper.writeValueAsString(yamlData);
        Set<ValidationMessage> validationMessages = schema.validate(objectMapper.readTree(jsonString));
        if (!validationMessages.isEmpty()) {
            log.error(validationMessages.toString());
        }
        return validationMessages;

    }
}
