package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.rule.impl.SourceExecutable;

/**
 * Represents an executable script.
 */
public class ScriptExecutable extends SourceExecutable<String> {

    public ScriptExecutable(String language, String source) {
        super(language, source);
    }
}
