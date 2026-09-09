package com.buschmais.jqassistant.plugin.junit.test.set.junit4;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("Some reason")
public class IgnoredTestClassWithoutAssertions {

    @Test
    public void ignoredTestWithoutAssertion() {
    }

}
