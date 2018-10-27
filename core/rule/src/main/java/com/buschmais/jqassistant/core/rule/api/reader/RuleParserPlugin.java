package com.buschmais.jqassistant.core.rule.api.reader;

import javax.xml.transform.Source;

import com.buschmais.jqassistant.core.analysis.api.rule.Rule;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.shared.lifecycle.ConfigurableLifecycleAware;

/**
 * Defines the interface of the rule parser.
 */
public interface RuleParserPlugin extends ConfigurableLifecycleAware<RuleConfiguration> {

    /**
     * Initialize the parser.
     */
    @Override
    default void initialize() throws RuleException {
    }

    /**
     * Configure the parser.
     *
     * @param ruleConfiguration
     *            The {@link RuleConfiguration} to use.
     */
    @Override
    default void configure(RuleConfiguration ruleConfiguration) throws RuleException {
    }

    @Override
    default void destroy() throws RuleException {
    }

    /**
     * Determine if the reader accepts the {@link RuleSource}.
     *
     * @param ruleSource
     *            The {@link RuleSource}.
     * @return <code>true</code> if the reader accepts the source.
     */
    boolean accepts(RuleSource ruleSource) throws RuleException;

    /**
     * Parse the given {@link Source} and adds contained {@link Rule}s using the
     * {@link RuleSetBuilder}.
     *
     * @param ruleSource
     *            The source to be read.
     * @param ruleSetBuilder
     *            {@link RuleSetBuilder}.
     */
    void parse(RuleSource ruleSource, RuleSetBuilder ruleSetBuilder) throws RuleException;

}
