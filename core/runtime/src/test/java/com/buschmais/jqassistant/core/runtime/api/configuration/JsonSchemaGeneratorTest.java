package com.buschmais.jqassistant.core.runtime.api.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.smallrye.config.ConfigMapping;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaGeneratorTest {

    private final JsonSchemaGenerator generator = new JsonSchemaGenerator();

    @Test
    public void testJsonSchemaGenerating() throws IOException {
        JsonNode node = generator.generateSchema(JQAssistant.class);
        assertThat(node).isNotNull();

        File file = new File("target/test-classes/jsonSchema.json");
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        JsonSchemaFactory bluePrintFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.builder(bluePrintFactory).build();
        JsonSchema schema = schemaFactory.getSchema(node);
        JsonNode rootNode = mapper.readTree(file);
        Set<ValidationMessage> validationMessages = schema.validate(rootNode);
        System.out.println(validationMessages);

    }

    @ConfigMapping
    private interface JQAssistant {

        Configuration jqassistant();

    }
}
