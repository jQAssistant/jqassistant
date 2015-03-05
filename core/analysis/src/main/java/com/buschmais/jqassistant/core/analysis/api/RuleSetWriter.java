package com.buschmais.jqassistant.core.analysis.api;

import java.io.Writer;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;

/**
 * Provides methods to write rule sets to XML files.
 */
public interface RuleSetWriter {

    /**
     * Writes the given
     * {@link com.buschmais.jqassistant.core.analysis.api.rule.DefaultRuleSet}
     * to an XML file.
     * 
     * @param ruleSet
     *            The
     *            {@link com.buschmais.jqassistant.core.analysis.api.rule.DefaultRuleSet}
     *            .
     * @param writer
     *            The {@link Writer}.
     */
    void write(RuleSet ruleSet, Writer writer) throws RuleException;
}
