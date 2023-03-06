package com.buschmais.jqassistant.core.rule.api.model;

import com.buschmais.jqassistant.core.rule.impl.SourceExecutable;

/**
 * Represents an executable script.
 */
public class ScriptExecutable extends SourceExecutable<String> {

    public ScriptExecutable(String language, String source, boolean transactional) {
        super(language, source, String.class, transactional);
    }
}
