package com.buschmais.jqassistant.core.plugin.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.RuleInterpreterPluginRepository;
import com.buschmais.jqassistant.core.plugin.schema.v1.IdClassListType;
import com.buschmais.jqassistant.core.plugin.schema.v1.IdClassType;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;

public class RuleInterpreterPluginRepositoryImpl extends AbstractPluginRepository implements RuleInterpreterPluginRepository {

    private Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins = new HashMap<>();

    public RuleInterpreterPluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) {
        super(pluginConfigurationReader);
    }

    @Override
    public Map<String, Collection<RuleInterpreterPlugin>> getRuleInterpreterPlugins(Map<String, Object> properties) {
        for (Collection<RuleInterpreterPlugin> languagePlugins : ruleInterpreterPlugins.values()) {
            for (RuleInterpreterPlugin ruleInterpreterPlugin : languagePlugins) {
                ruleInterpreterPlugin.configure(properties);
            }
        }
        return ruleInterpreterPlugins;
    };

    @Override
    public void initialize() {
        for (JqassistantPlugin plugin : plugins) {
            IdClassListType ruleInterpreters = plugin.getRuleInterpreter();
            if (ruleInterpreters != null) {
                for (IdClassType pluginType : ruleInterpreters.getClazz()) {
                    RuleInterpreterPlugin ruleInterpreterPlugin = createInstance(pluginType.getValue());
                    ruleInterpreterPlugin.initialize();
                    for (String language : ruleInterpreterPlugin.getLanguages()) {
                        Collection<RuleInterpreterPlugin> plugins = ruleInterpreterPlugins.get(language.toLowerCase());
                        if (plugins == null) {
                            plugins = new ArrayList<>();
                            ruleInterpreterPlugins.put(language.toLowerCase(), plugins);
                        }
                        plugins.add(ruleInterpreterPlugin);
                    }
                }
            }
        }
    }

    @Override
    public void destroy() {
        ruleInterpreterPlugins.values().stream().flatMap(plugins -> plugins.stream()).forEach(plugin -> plugin.destroy());
    }
}
