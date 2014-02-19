package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;

import java.io.Writer;

/**
 * Provides methods to write rule sets to XML files.
 */
public interface RuleSetWriter {

    /**
     * Writes the given {@link RuleSet} to an XML file.
     *
     * @param ruleSet The {@link RuleSet}.
     * @param writer  The {@link Writer}.
     */
    void write(RuleSet ruleSet, Writer writer);
}
