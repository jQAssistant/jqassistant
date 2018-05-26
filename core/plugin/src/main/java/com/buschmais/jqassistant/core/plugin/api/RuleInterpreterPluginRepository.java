package com.buschmais.jqassistant.core.plugin.api;

import java.util.Collection;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;

/**
 * Defines the plugin repository for {@link RuleInterpreterPlugin}s.
 */
public interface RuleInterpreterPluginRepository {

    /**
     * Return the {@link RuleInterpreterPlugin}s.
     *
     * @param properties
     *            The configuration properties.
     * @return The {@link RuleInterpreterPlugin}s.
     */
    Map<String, Collection<RuleInterpreterPlugin>> getRuleInterpreterPlugins(Map<String, Object> properties) throws PluginRepositoryException;
}
