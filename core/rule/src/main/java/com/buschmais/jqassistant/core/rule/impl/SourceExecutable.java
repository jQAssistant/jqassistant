package com.buschmais.jqassistant.core.rule.impl;

import com.buschmais.jqassistant.core.analysis.api.rule.AbstractExecutable;

/**
 * The implementation of
 * {@link com.buschmais.jqassistant.core.analysis.api.rule.Executable}.
 */
public class SourceExecutable extends AbstractExecutable {

    /**
     * Constructor
     *
     * @param language
     *            The language.
     * @param source
     *            The source.
     */
    public SourceExecutable(String language, String source) {
        super(language, source);
    }

}
