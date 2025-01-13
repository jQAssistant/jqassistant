package com.buschmais.jqassistant.maven.test;

import com.buschmais.jqassistant.core.runtime.api.configuration.JsonSchemaGenerator;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MavenJsonSchemaGeneratorTest {

    private final JsonSchemaGenerator generator = new JsonSchemaGenerator();

    @Test
    void generateSchema() throws Exception {
        JsonNode node = generator.generateSchema(MavenConfiguration.class, "target/generated-resources/schema/jqassistant-configuration-maven.schema.json");
        assertThat(generator.validateYaml("src/test/resources/validMavenYaml.yaml", node)).isEmpty();
    }
}
