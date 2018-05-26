package com.buschmais.jqassistant.core.plugin.impl;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RuleInterpreterPluginRepository;
import com.buschmais.jqassistant.core.plugin.schema.v1.IdClassType;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.plugin.schema.v1.RuleInterpreterType;

public class RuleInterpreterPluginRepositoryImpl extends AbstractPluginRepository implements RuleInterpreterPluginRepository {

    private Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins;

    public RuleInterpreterPluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) throws PluginRepositoryException {
        super(pluginConfigurationReader);
        ruleInterpreterPlugins = initialize();
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

    private Map<String, Collection<RuleInterpreterPlugin>> initialize() throws PluginRepositoryException {
        Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins = new HashMap<>();
        for (JqassistantPlugin plugin : plugins) {
            RuleInterpreterType ruleInterpreter = plugin.getRuleInterpreter();
            if (ruleInterpreter != null) {
                for (IdClassType pluginType : ruleInterpreter.getClazz()) {
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
        return ruleInterpreterPlugins;
    }

}
