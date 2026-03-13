package com.buschmais.jqassistant.core.rule.api.reader;

import javax.xml.transform.Source;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSetBuilder;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.shared.lifecycle.ConfigurableLifecycleAware;

/**
 * Defines the interface of the rule parser.
 */
public interface RuleParserPlugin extends ConfigurableLifecycleAware<Rule> {

    /**
     * Initialize the parser.
     */
    @Override
    default void initialize() throws RuleException {
    }

    /**
     * Configure the parser.
     *
     * @param rule
     *     The {@link Rule} configuration to use.
     */
    @Override
    default void configure(Rule rule) throws RuleException {
    }

    @Override
    default void destroy() throws RuleException {
    }

    /**
     * Determine if the reader accepts the {@link RuleSource}.
     *
     * @param ruleSource
     *     The {@link RuleSource}.
     * @return <code>true</code> if the reader accepts the source.
     */
    boolean accepts(RuleSource ruleSource) throws RuleException;

    /**
     * Parse the given {@link Source} and adds contained rules using the
     * {@link RuleSetBuilder}.
     *
     * @param ruleSource
     *     The source to be read.
     * @param ruleSetBuilder
     *     {@link RuleSetBuilder}.
     */
    void parse(RuleSource ruleSource, RuleSetBuilder ruleSetBuilder) throws RuleException;

}
