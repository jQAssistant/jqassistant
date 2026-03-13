package com.buschmais.jqassistant.plugin.junit.test.set.junit5;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ParameterizedTestClass {
    @ParameterizedTest
    @ValueSource(strings = {"foor", "bar"})
    public void parameterizedTest(String value) {
    }
}
