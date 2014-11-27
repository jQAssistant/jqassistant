package com.buschmais.jqassistant.core.plugin.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.ScopePluginRepository;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.plugin.schema.v1.ScopeType;
import com.buschmais.jqassistant.core.scanner.api.Scope;

/**
 * Scope repository implementation.
 */
public class ScopePluginRepositoryImpl extends AbstractPluginRepository implements ScopePluginRepository {

    private Map<String, Scope> scopes;

    /**
     * Constructor.
     */
    public ScopePluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) throws PluginRepositoryException {
        super(pluginConfigurationReader);
        List<JqassistantPlugin> plugins = pluginConfigurationReader.getPlugins();
        this.scopes = this.getScopes(plugins);
    }

    private Map<String, Scope> getScopes(List<JqassistantPlugin> plugins) throws PluginRepositoryException {
        Map<String, Scope> scopes = new HashMap<>();
        for (JqassistantPlugin plugin : plugins) {
            ScopeType scopeType = plugin.getScope();
            if (scopeType != null) {
                for (String scopePluginName : scopeType.getClazz()) {
                    Class<? extends Enum<?>> type = getType(scopePluginName);
                    for (Enum enumConstant : type.getEnumConstants()) {
                        Scope scope = (Scope) enumConstant;
                        String scopeName = scope.getPrefix() + ":" + scope.getName();
                        scopes.put(scopeName.toLowerCase(), scope);
                    }
                }
            }
        }
        return scopes;
    }

    @Override
    public Scope getScope(String name) {
        return scopes.get(name.toLowerCase());
    }
}
