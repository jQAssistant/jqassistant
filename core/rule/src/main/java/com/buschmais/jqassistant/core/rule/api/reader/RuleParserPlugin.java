package com.buschmais.jqassistant.core.rule.api.reader;

import javax.xml.transform.Source;

import com.buschmais.jqassistant.core.analysis.api.rule.Rule;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

/**
 * Defines the interface of the rule parser.
 */
public interface RuleParserPlugin {

    /**
     * Initialize the parser.
     */
    void initialize();

    /**
     * Configure the parser.
     *
     * @param ruleConfiguration
     *            The {@link RuleConfiguration} to use.
     */
    void configure(RuleConfiguration ruleConfiguration) throws RuleException;

    /**
     * Determine if the reader accepts the {@link RuleSource}.
     *
     * @param ruleSource
     *            The {@link RuleSource}.
     * @return <code>true</code> if the reader accepts the source.
     */
    boolean accepts(RuleSource ruleSource) throws RuleException;

    /**
     * Reads the given {@link Source} and adds contained {@link Rule}s using the
     * {@link RuleSetBuilder}.
     *
     * @param ruleSource
     *            The source to be read.
     * @param ruleSetBuilder
     *            {@link RuleSetBuilder}.
     */
    void read(RuleSource ruleSource, RuleSetBuilder ruleSetBuilder) throws RuleException;

}
