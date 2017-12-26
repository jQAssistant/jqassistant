package com.buschmais.jqassistant.core.plugin.impl;

import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.plugin.schema.v1.RulesType;
import com.buschmais.jqassistant.core.rule.api.source.ClasspathRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;

/**
 * Rule repository implementation.
 */
public class RulePluginRepositoryImpl implements RulePluginRepository {

    /**
     * The resource path where to load rule files from.
     */
    private static final String RULE_RESOURCE_PATH = "META-INF/jqassistant-rules/";

    private ClassLoader classLoader;

    private List<RuleSource> sources;

    /**
     * Constructor.
     */
    public RulePluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) throws PluginRepositoryException {
        this.classLoader = pluginConfigurationReader.getClassLoader();
        this.sources = getRuleSources(pluginConfigurationReader.getPlugins());
    }

    @Override
    public List<RuleSource> getRuleSources() {
        return sources;
    }

    private List<RuleSource> getRuleSources(List<JqassistantPlugin> plugins) {
        List<RuleSource> sources = new ArrayList<>();
        for (JqassistantPlugin plugin : plugins) {
            RulesType rulesType = plugin.getRules();
            if (rulesType != null) {
                for (String resource : rulesType.getResource()) {
                    String resourceName = RULE_RESOURCE_PATH + resource;
                    sources.add(new ClasspathRuleSource(classLoader, resourceName));
                }
            }
        }
        return sources;
    }
}
