package com.buschmais.jqassistant.core.plugin.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.ScopePluginRepository;
import com.buschmais.jqassistant.core.plugin.schema.v1.ClassListType;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;
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
        this.scopes = Collections.unmodifiableMap(this.getScopes(plugins));
    }

    private Map<String, Scope> getScopes(List<JqassistantPlugin> plugins) throws PluginRepositoryException {
        Map<String, Scope> scopes = new HashMap<>();
        for (JqassistantPlugin plugin : plugins) {
            ClassListType scopeTypes = plugin.getScope();
            if (scopeTypes != null) {
                for (String scopePluginName : scopeTypes.getClazz()) {
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

    @Override
    public Map<String, Scope> getScopes() {
        return scopes;
    }
}
