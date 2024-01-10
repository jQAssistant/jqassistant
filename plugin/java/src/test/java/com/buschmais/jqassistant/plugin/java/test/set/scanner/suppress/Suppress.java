package com.buschmais.jqassistant.plugin.java.test.set.scanner.suppress;

import com.buschmais.jqassistant.plugin.java.api.annotation.jQASuppress;

@jQASuppress(value = "suppress:Class", reason = "For testing this annotation")
public class Suppress {

    @jQASuppress({ "suppress:Field" })
    private String value;

    @jQASuppress(value = { "suppress:Method", "suppress:MethodInPrimaryColumn", "suppress:MethodInNonPrimaryColumn" }, column = "method")
    public void doSomething() {
    }

}
