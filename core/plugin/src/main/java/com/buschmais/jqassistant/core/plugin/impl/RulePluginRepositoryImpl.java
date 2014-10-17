package com.buschmais.jqassistant.core.plugin.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleSource;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.plugin.schema.v1.RulesType;

/**
 * Rule repository implementation.
 */
public class RulePluginRepositoryImpl implements RulePluginRepository {

    /**
     * The resource path where to load rule files from.
     */
    private static final String RULE_RESOURCE_PATH = "META-INF/jqassistant-rules/";

    private ClassLoader classLoader;

    private List<RuleSource> ruleSources;

    /**
     * Constructor.
     */
    public RulePluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) throws PluginRepositoryException {
        this.ruleSources = getRuleSources(pluginConfigurationReader.getPlugins());
        this.classLoader = pluginConfigurationReader.getClassLoader();
    }

    @Override
    public List<RuleSource> getRuleSources() {
        return ruleSources;
    }

    /**
     * Get the URLs of the rules for the given plugins.
     * 
     * @param plugins
     *            the plugins for which to get the URLs
     * @return the list of URLs
     */
    private List<RuleSource> getRuleSources(List<JqassistantPlugin> plugins) {
        List<RuleSource> sources = new ArrayList<>();
        for (JqassistantPlugin plugin : plugins) {
            RulesType rulesType = plugin.getRules();
            if (rulesType != null) {
                for (String resource : rulesType.getResource()) {
                    final String resourceName = RULE_RESOURCE_PATH + resource;
                    sources.add(new RuleSource() {
                        @Override
                        public String getId() {
                            return classLoader.getResource(resourceName).toExternalForm();
                        }

                        @Override
                        public InputStream getInputStream() {
                            return classLoader.getResourceAsStream(resourceName);
                        }
                    });
                }
            }
        }
        return sources;
    }
}
