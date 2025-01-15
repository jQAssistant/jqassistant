package com.buschmais.jqassistant.maven.configuration;

import java.io.File;

import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;

import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.buschmais.jqassistant.core.runtime.api.configuration.JsonSchemaGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Executed by Exec Maven Plugin
 */
public class MavenJsonSchemaGenerator {

    public static void main(String[] args) throws Exception {
        assertThat(args).describedAs("Expecting one argument representing the output directory")
            .hasSize(1);
        String outputDirectory = args[0];
        ObjectNode schemaNode = generateSchema(MavenConfiguration.class);
        assertThat(validateYaml(MavenJsonSchemaGenerator.class.getResource("/validMavenYaml.yaml"), schemaNode)).isEmpty();
        File file = writeSchema(schemaNode, new File(outputDirectory), "jqassistant-configuration-maven");
        assertThat(file).exists();
    }
}
