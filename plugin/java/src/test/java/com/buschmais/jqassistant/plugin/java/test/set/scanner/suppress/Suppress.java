package com.buschmais.jqassistant.plugin.java.test.set.scanner.suppress;

import com.buschmais.jqassistant.plugin.java.api.annotation.jQASuppress;

@jQASuppress(value = "suppress:Class",  reason = "For testing this annotation", until = "25/08/2075")
public class Suppress {

    @jQASuppress(value = "suppress:Field", until = "25/08/2025") // date expired, should not be suppressed
    private String expiredValue;

    @jQASuppress({ "suppress:Field" }) // no expiration Date, should not be suppressed
    private String noExpirationDateValue;

    @jQASuppress(value = "suppress:Field", until = "02/03/2075") // should be suppressed
    private String suppressedValue;

    @jQASuppress(value = { "suppress:Method", "suppress:MethodInPrimaryColumn", "suppress:MethodInNonPrimaryColumn" }, column = "method", until = "31/12/2075")
    public void suppressedMethod() {
    }

}
