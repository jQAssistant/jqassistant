package com.buschmais.jqassistant.core.plugin.api;

import java.util.Collection;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.shared.lifecycle.LifecycleAware;

/**
 * Defines the plugin repository for {@link RuleInterpreterPlugin}s.
 */
public interface RuleInterpreterPluginRepository extends LifecycleAware {

    /**
     * Return the {@link RuleInterpreterPlugin}s.
     *
     * @param properties
     *            The configuration properties.
     * @return The {@link RuleInterpreterPlugin}s.
     */
    Map<String, Collection<RuleInterpreterPlugin>> getRuleInterpreterPlugins(Map<String, Object> properties) throws PluginRepositoryException;

    @Override
    void initialize() throws PluginRepositoryException;

    @Override
    void destroy() throws PluginRepositoryException;
}
