package com.buschmais.jqassistant.plugin.java.test.set.rules.java;

import com.buschmais.jqassistant.plugin.java.annotation.CustomAssertMethod;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomAssert {

    @CustomAssertMethod
    void customAssertMethodDirectlyCallingJunitAssertion() {
        assertTrue(true);
    }

    @CustomAssertMethod
    void customAssertMethodNotCallingAssertion() {
        // NOP;
    }

    @CustomAssertMethod
    void customAssertMethodCallingAnotherCustomAssertMethod() {
        customAssertMethodNotCallingAssertion();
    }

    @CustomAssertMethod
    void customAssertMethodIndirectlyCallingJunitAssertMethod() {
        customAssertMethodDirectlyCallingJunitAssertion();
    }

    void somethingElse() {
        // NOP;
    }

}
