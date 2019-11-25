package com.buschmais.jqassistant.core.rule.api.writer;

import java.io.Writer;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;

/**
 * Provides methods to write rule sets to XML files.
 */
public interface RuleSetWriter {

    /**
     * Writes the given {@link RuleSet} to an XML file.
     *
     * @param ruleSet
     *            The {@link RuleSet} .
     * @param writer
     *            The {@link Writer}.
     */
    void write(RuleSet ruleSet, Writer writer) throws RuleException;
}
