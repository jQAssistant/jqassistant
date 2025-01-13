package com.buschmais.jqassistant.commandline.test;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.runtime.api.configuration.JsonSchemaGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CliJsonSchemaGeneratorTest {

    @Test
    void generateSchemaAndValidate() throws Exception {
        JsonSchemaGenerator generator = new JsonSchemaGenerator();
        String path = "target/generated-resources/schema/jqassistant-configuration-cli.schema.json";
        JsonNode node = generator.generateSchema(CliConfiguration.class, path);
        assertThat(generator.validateYaml("src/test/resources/validCliYaml.yaml", node)).isEmpty();
    }

}
