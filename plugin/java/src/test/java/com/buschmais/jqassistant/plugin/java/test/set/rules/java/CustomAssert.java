package com.buschmais.jqassistant.plugin.java.test.set.rules.java;

import com.buschmais.jqassistant.plugin.java.annotation.CustomAssertMethod;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomAssert {

    @CustomAssertMethod
    void customAssertMethodCallingJunitAssertion() {
        assertTrue(true);
    }

    @CustomAssertMethod
    void customAssertMethodNotCallingAssertion() {
        // NOP;
    }

    void somethingElse() {
        // NOP;
    }

}
