package com.buschmais.jqassistant.core.plugin.impl;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.RuleLanguagePlugin;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RuleLanguagePluginRepository;
import com.buschmais.jqassistant.core.plugin.schema.v1.IdClassType;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.plugin.schema.v1.RuleLanguageType;

public class RuleLanguagePluginRepositoryImpl extends AbstractPluginRepository implements RuleLanguagePluginRepository {

    private final List<JqassistantPlugin> plugins;

    public RuleLanguagePluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) {
        super(pluginConfigurationReader);
        this.plugins = pluginConfigurationReader.getPlugins();
    }

    @Override
    public Map<String, Collection<RuleLanguagePlugin>> getRuleLanguagePlugins() throws PluginRepositoryException {
        Map<String, Collection<RuleLanguagePlugin>> ruleLanguagePlugins = new HashMap<>();
        for (JqassistantPlugin plugin : plugins) {
            RuleLanguageType pluginLanguage = plugin.getLanguage();
            if (pluginLanguage != null) {
                for (IdClassType pluginType : pluginLanguage.getClazz()) {
                    RuleLanguagePlugin ruleLanguagePlugin = createInstance(pluginType.getValue());
                    for (String language : ruleLanguagePlugin.getLanguages()) {
                        Collection<RuleLanguagePlugin> plugins = ruleLanguagePlugins.get(language.toLowerCase());
                        if (plugins == null) {
                            plugins = new ArrayList<>();
                            ruleLanguagePlugins.put(language.toLowerCase(), plugins);
                        }
                        plugins.add(ruleLanguagePlugin);
                    }
                }
            }
        }
        return ruleLanguagePlugins;
    }

}
