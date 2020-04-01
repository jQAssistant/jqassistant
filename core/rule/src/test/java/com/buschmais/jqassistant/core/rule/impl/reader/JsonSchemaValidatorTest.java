package com.buschmais.jqassistant.core.rule.impl.reader;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class JsonSchemaValidatorTest {
    private JsonSchemaValidator validator;

    @BeforeEach
    void setUp() throws RuleException {
         validator = new JsonSchemaValidator();
    }

    @Disabled
    @Test
    void thereIsNoFuckingTest() {
        throw new RuntimeException("Add some fucking tests!");
    }

}
