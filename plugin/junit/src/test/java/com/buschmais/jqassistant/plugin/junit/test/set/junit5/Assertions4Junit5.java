package com.buschmais.jqassistant.plugin.junit.test.set.junit5;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("This is not an actual test")
public class Assertions4Junit5 extends AbstractAssertions4Junit5 {

    @Test
    public void assertWithoutMessage() {
        assertTrue(true);
    }

    @Test
    public void assertWithMessageButNonVoidReturnType() {
        assertThrows(NullPointerException.class, null, "Condition must be true");
    }

    @Test
    public void assertWithMessageSupplier() {
        assertTrue(() -> true, () -> "S");
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

    @Test
    public void testWithDeepNestedAssertion() {
        deepNestedAssertion();
    }

    @Test
    public void testWithDeepAndShallowAssertion() {
        assertTrue(true, "Condition must be true");
        deepNestedAssertion();
    }

    @Test
    public void assertWithNonVoidReturn() {
        assertThrows(IllegalArgumentException.class, this::throwsException);
    }

    @Test
    public void assertInSuperClass() {
        super.verify();
    }

    private void nestedAssertion() {
        fail("Failing");
    }

    private void deepNestedAssertion() {
        deepNestedAssertion1();
    }

    private void deepNestedAssertion1() {
        deepNestedAssertion2();
    }

    private void deepNestedAssertion2() {
        deepNestedAssertion3();
    }

    private void deepNestedAssertion3() {
        assertTrue(true, "Condition must be true");
    }

    private void throwsException() throws IllegalArgumentException {

    }
}
