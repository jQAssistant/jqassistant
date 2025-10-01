package com.buschmais.jqassistant.plugin.java.test.set.scanner.suppress;

import com.buschmais.jqassistant.plugin.java.api.annotation.jQASuppress;

@jQASuppress(value = "test-suppress:Class", reason = "For testing this annotation")
public class Suppress {

    @jQASuppress({ "test-suppress:Field" })
    private String value;

    @jQASuppress({ "test-suppress:Method", "test-suppress:MethodInPrimaryColumn" })
    public void doSomething() {
    }
}
