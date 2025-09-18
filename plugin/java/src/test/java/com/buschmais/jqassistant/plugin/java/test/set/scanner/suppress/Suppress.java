package com.buschmais.jqassistant.plugin.java.test.set.scanner.suppress;

import com.buschmais.jqassistant.plugin.java.api.annotation.jQASuppress;

@jQASuppress(value = "suppress:Class",  reason = "For testing this annotation", until = "25/08/2025")
public class Suppress {

    @jQASuppress({ "suppress:Field" })
    private String value;

    @jQASuppress(value = { "suppress:Method", "suppress:MethodInPrimaryColumn", "suppress:MethodInNonPrimaryColumn" }, column = "method", until = "31/12/2049")
    public void doSomething() {
    }

}
