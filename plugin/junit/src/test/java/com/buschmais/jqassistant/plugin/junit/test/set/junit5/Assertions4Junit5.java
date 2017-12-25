package com.buschmais.jqassistant.plugin.junit.test.set.junit5;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Disabled("This is not an actual test")
public class Assertions4Junit5 {

    @Test
    public void assertWithoutMessage() {
        assertTrue(true);
    }

    @RepeatedTest(21)
    public void assertWithMessage() {
        assertTrue(() -> true, "Condition must be true");
    }

    @RepeatedTest(21)
    public void repeatedTestWithoutAssertion() {
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "b"})
    public void parameterizedTestWithoutAssertion(String v) {
    }

    @Test
    public void testWithoutAssertion() {
    }

    @Test
    public void testWithAssertion() {
        assertTrue(true);
    }

    @Test
    public void testWithNestedAssertion() {
        nestedAssertion();
    }

    private void nestedAssertion() {
        assertTrue(() -> 2 > 1,  "Condition must be true");
    }
}
