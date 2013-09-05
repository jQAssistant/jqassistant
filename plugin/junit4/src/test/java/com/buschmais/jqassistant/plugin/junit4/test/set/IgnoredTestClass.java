package com.buschmais.jqassistant.plugin.junit4.test.set;

import org.junit.Ignore;
import org.junit.Test;

/**
 * A JUnit4 test classes which is annotated with {@link Ignore} on class and method level.
 */
@Ignore
public class IgnoredTestClass {

    @Test
    @Ignore
    public void ignoredTestMethod() {
    }
}
