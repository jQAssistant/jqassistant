package com.buschmais.jqassistant.plugin.java_testing.concept;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Example class used by integration tests.
 */
public class AssertExample {

    void assertjAssertExampleMethod() {
        assertThat(true).isTrue();
    }

    void mockitoVerifyExampleMethod() {
        verify(mock(Object.class).equals(any(Object.class)));
    }

}
