package com.buschmais.jqassistant.plugin.java.test.set.scanner.suppress;

import com.buschmais.jqassistant.plugin.java.annotation.jQASuppress;

@jQASuppress(value = "test-suppress:Class",  reason = "For testing this annotation", until = "2075-08-25")
public class SuppressRules {

    @jQASuppress(value = "test-suppress:Field", until = "2024-08-25") // date expired, should not be suppressed
    private String expiredValue;

    @jQASuppress(value = "test-suppress:Field", reason = "Reason for suppressing this field.") // no expiration date, should be suppressed
    private String noExpirationDateValue;

    @jQASuppress(value = "test-suppress:Field", until = "2075-06-04", reason = "") // should be suppressed
    private String suppressedValue;

    @jQASuppress(value = "test-suppress:Method", column = "method", until = "2075-12-31")
    public void suppressedMethod() {
    }

    @jQASuppress(value = "test-suppress:Class",  reason = "", until = "2025-02-14")
    public class ClassWithoutReason {

    }

}
