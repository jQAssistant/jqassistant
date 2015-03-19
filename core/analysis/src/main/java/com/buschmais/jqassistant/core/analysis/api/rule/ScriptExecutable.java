package com.buschmais.jqassistant.core.analysis.api.rule;

/**
 * Represents an executable script.
 */
public class ScriptExecutable implements Executable {

    private String language;

    private String source;

    public ScriptExecutable(String language, String source) {
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
