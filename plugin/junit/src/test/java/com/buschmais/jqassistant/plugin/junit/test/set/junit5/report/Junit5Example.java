package com.buschmais.jqassistant.plugin.junit.test.set.junit5.report;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class Junit5Example extends AbstractJunit5Example {

    @ParameterizedTest
    @ValueSource(strings = {"foor", "bar"})
    void parameterizedTest(String value) {
    }

    @RepeatedTest(10)
    public void repeatedTest() {
    }

    @Test
    public void success() {
    }

    @Test
    public void failure() {
        Assertions.fail("Failure");
    }

    @Test
    public void error() {
        throw new UnsupportedOperationException();
    }

    @Disabled
    @Test
    public void skipped() {
    }
}
