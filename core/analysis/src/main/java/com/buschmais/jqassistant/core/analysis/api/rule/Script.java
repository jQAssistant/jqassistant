package com.buschmais.jqassistant.core.analysis.api.rule;

/**
 * Represents an executable script.
 */
public class Script {

    private String type;

    private String source;

    public Script(String type, String source) {
        this.type = type;
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

}
