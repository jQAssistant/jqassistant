package com.buschmais.jqassistant.core.plugin.api;

import java.util.Collection;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleSourceReaderPlugin;

/**
 * Defines the plugin repository for {@link RuleSourceReaderPlugin}s.
 */
public interface RuleSourceReaderPluginRepository {

    /**
     * Return the {@link RuleSourceReaderPlugin}s.
     *
     * @return The {@link RuleSourceReaderPlugin}s.
     */
    Collection<RuleSourceReaderPlugin> getRuleSourceReaderPlugins(RuleConfiguration ruleConfiguration) throws RuleException;
}
