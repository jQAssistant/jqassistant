package com.buschmais.jqassistant.plugin.maven3.api.scanner;

/**
 * Represents a parameter for an additional directory to include while scanning.
 */
public class ScanDirectory {

    private String name;

    private String scope;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
