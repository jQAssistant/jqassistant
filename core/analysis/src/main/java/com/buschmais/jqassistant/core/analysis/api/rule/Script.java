package com.buschmais.jqassistant.core.analysis.api.rule;

/**
 * Represents an executable script.
 */
public class Script {

    private String language;

    private String source;

    public Script(String language, String source) {
        this.language = language;
        this.source = source;
    }

    public String getLanguage() {
        return language;
    }

    public String getSource() {
        return source;
    }

}
