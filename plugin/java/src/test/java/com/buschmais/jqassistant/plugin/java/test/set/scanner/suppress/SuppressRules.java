package com.buschmais.jqassistant.plugin.java.test.set.scanner.suppress;

import com.buschmais.jqassistant.plugin.java.api.annotation.jQASuppress;

@jQASuppress(value = "test-suppress:Class",  reason = "For testing this annotation", until = "2075-08-25")
public class SuppressRules {

    @jQASuppress(value = "test-suppress:Field", until = "2024-08-25") // date expired, should not be suppressed
    private String expiredValue;

    @jQASuppress({ "test-suppress:Field" }) // no expiration Date, should be suppressed
    private String noExpirationDateValue;

    @jQASuppress(value = "test-suppress:Field", until = "2075-03-02") // should be suppressed
    private String suppressedValue;

    @jQASuppress(value = "test-suppress:Method", column = "method", until = "2075-12-31")
    public void suppressedMethod() {
    }

}
