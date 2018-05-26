package com.buschmais.jqassistant.core.plugin.api;

import java.util.Collection;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;

/**
 * Defines the plugin repository for {@link RuleParserPlugin}s.
 */
public interface RuleParserPluginRepository {

    /**
     * Return the {@link RuleParserPlugin}s.
     *
     * @return The {@link RuleParserPlugin}s.
     */
    Collection<RuleParserPlugin> getRuleParserPlugins(RuleConfiguration ruleConfiguration) throws RuleException;
}
