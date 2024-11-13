package com.buschmais.jqassistant.core.runtime.api.configuration;

import com.buschmais.jqassistant.core.runtime.impl.configuration.Maptest;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaGeneratorTest {

    private final JsonSchemaGenerator generator = new JsonSchemaGenerator();

    @Test
    public void testJsonSchemaGenerating(){
        assertThat(generator.generateSchema(Maptest.class)).isNotNull();

    }

}
