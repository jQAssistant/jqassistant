package com.buschmais.jqassistant.core.runtime.api.configuration;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaGeneratorTest {

    private JsonSchemaGenerator generator = new JsonSchemaGenerator();

    @Test
    public void testJsonSchemaGenerating(){
        assertThat(generator.generateSchema(Configuration.class)).isNotNull();

    }

}
