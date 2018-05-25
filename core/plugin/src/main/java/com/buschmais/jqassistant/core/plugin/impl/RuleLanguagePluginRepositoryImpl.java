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

    private Map<String, Collection<RuleLanguagePlugin>> ruleLanguagePlugins;

    public RuleLanguagePluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) throws PluginRepositoryException {
        super(pluginConfigurationReader);
        ruleLanguagePlugins = initialize();
    }

    @Override
    public Map<String, Collection<RuleLanguagePlugin>> getRuleLanguagePlugins(Map<String, Object> properties) {
        for (Collection<RuleLanguagePlugin> languagePlugins : ruleLanguagePlugins.values()) {
            for (RuleLanguagePlugin languagePlugin : languagePlugins) {
                languagePlugin.configure(properties);
            }
        }
        return ruleLanguagePlugins;
    };

    private Map<String, Collection<RuleLanguagePlugin>> initialize() throws PluginRepositoryException {
        Map<String, Collection<RuleLanguagePlugin>> ruleLanguagePlugins = new HashMap<>();
        for (JqassistantPlugin plugin : plugins) {
            RuleLanguageType pluginLanguage = plugin.getRuleLanguage();
            if (pluginLanguage != null) {
                for (IdClassType pluginType : pluginLanguage.getClazz()) {
                    RuleLanguagePlugin ruleLanguagePlugin = createInstance(pluginType.getValue());
                    ruleLanguagePlugin.initialize();
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
