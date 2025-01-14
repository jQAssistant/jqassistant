package com.buschmais.jqassistant.commandline.task;

import com.buschmais.jqassistant.commandline.configuration.CliConfiguration;
import com.buschmais.jqassistant.core.runtime.api.configuration.JsonSchemaGenerator;

import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CliJsonSchemaGeneratorTest {

    @Test
    void generateSchemaAndValidate() throws Exception {
        JsonSchemaGenerator generator = new JsonSchemaGenerator();
        String path = "target/generated-resources/schema/jqassistant-configuration-cli.schema.json";
        JsonNode node = generator.generateSchema(CliConfiguration.class, path);
        Assertions.assertThat(generator.validateYaml("src/test/resources/validCliYaml.yaml", node)).isEmpty();
    }

}
