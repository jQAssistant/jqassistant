package com.buschmais.jqassistant.core.plugin.api;

import java.util.Collection;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.RuleLanguagePlugin;

/**
 * Defines the plugin repository for {@link RuleLanguagePlugin}s.
 */
public interface RuleLanguagePluginRepository {

    /**
     * Return the {@link RuleLanguagePlugin}s.
     *
     * @param properties
     *            The configuration properties.
     * @return The {@link RuleLanguagePlugin}s.
     */
    Map<String, Collection<RuleLanguagePlugin>> getRuleLanguagePlugins(Map<String, Object> properties) throws PluginRepositoryException;
}
