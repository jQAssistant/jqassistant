package com.buschmais.jqassistant.plugin.junit4.test.set;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("This is not an actual test")
public class Assertions {

    @Test
    public void assertWithoutMessage() {
        assertTrue(true);
    }

    @Test
    public void assertWithMessage() {
        assertTrue("Condition must be true", true);
    }

    @Test
    public void testWithoutAssertion() {
    }

    @Test
    public void testWithAssertion() {
        assertTrue("Condition must be true", true);
    }

    @Test
    public void testWithNestedAssertion() {
        nestedAssertion();
    }

    private void nestedAssertion() {
        assertTrue("Condition must be true", true);
    }
}
