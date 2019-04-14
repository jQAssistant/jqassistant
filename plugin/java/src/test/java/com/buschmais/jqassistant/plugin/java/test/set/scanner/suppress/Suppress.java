package com.buschmais.jqassistant.plugin.java.test.set.scanner.suppress;

import com.buschmais.jqassistant.plugin.java.api.annotation.jQASuppress;

@jQASuppress({ "suppress:Class" })
public class Suppress {

    @jQASuppress({ "suppress:Field" })
    private String value;

    @jQASuppress({ "suppress:Method" })
    public void doSomething() {
    }
}
