package com.buschmais.jqassistant.plugin.osgi.test.api.data;

import javax.validation.constraints.NotNull;

/**
 * An example request.
 */
public class Request {

    @NotNull
    private String value;

    public Request(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
