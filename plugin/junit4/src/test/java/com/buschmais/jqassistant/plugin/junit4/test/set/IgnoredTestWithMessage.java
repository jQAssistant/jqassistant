package com.buschmais.jqassistant.plugin.junit4.test.set;

import org.junit.Ignore;
import org.junit.Test;

/**
 * A JUnit4 test classes which is annotated with {@link org.junit.Ignore} on
 * class and method level and providing message.
 */
@Ignore("ignored")
public class IgnoredTestWithMessage {

    @Test
    @Ignore("ignored")
    public void ignoredTestWithMessage() {
    }

}
