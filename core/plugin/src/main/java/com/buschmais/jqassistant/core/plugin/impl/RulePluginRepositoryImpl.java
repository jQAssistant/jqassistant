package com.buschmais.jqassistant.core.plugin.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.schema.v1.IdClassListType;
import com.buschmais.jqassistant.core.plugin.schema.v1.IdClassType;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.plugin.schema.v1.RulesType;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.ClasspathRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.spi.RulePluginRepository;

/**
 * Rule repository implementation.
 */
public class RulePluginRepositoryImpl extends AbstractPluginRepository implements RulePluginRepository {

    /**
     * The resource path where to load rule files from.
     */
    private static final String RULE_RESOURCE_PATH = "META-INF/jqassistant-rules/";

    private final ClassLoader classLoader;

    private final List<RuleSource> sources;

    private final Collection<RuleParserPlugin> ruleParserPlugins = new LinkedList<>();

    /**
     * Constructor.
     */
    public RulePluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) {
        super(pluginConfigurationReader);
        this.classLoader = pluginConfigurationReader.getClassLoader();
        this.sources = getRuleSources(pluginConfigurationReader.getPlugins());
    }

    @Override
    public List<RuleSource> getRuleSources() {
        return sources;
    }

    @Override
    public Collection<RuleParserPlugin> getRuleParserPlugins(RuleConfiguration ruleConfiguration) throws RuleException {
        for (RuleParserPlugin ruleParserPlugin : ruleParserPlugins) {
            ruleParserPlugin.configure(ruleConfiguration);
        }
        return ruleParserPlugins;
    }

    @Override
    public void initialize() {
        for (JqassistantPlugin plugin : plugins) {
            IdClassListType ruleParsers = plugin.getRuleParser();
            if (ruleParsers != null) {
                for (IdClassType pluginType : ruleParsers.getClazz()) {
                    RuleParserPlugin ruleParserPlugin = createInstance(pluginType.getValue());
                    try {
                        ruleParserPlugin.initialize();
                    } catch (RuleException e) {
                        throw new PluginRepositoryException("Cannot initialize plugin " + ruleParserPlugin, e);
                    }
                    ruleParserPlugins.add(ruleParserPlugin);
                }
            }
        }
    }

    @Override
    public void destroy() {
        for (RuleParserPlugin ruleParserPlugin : ruleParserPlugins) {
            try {
                ruleParserPlugin.destroy();
            } catch (RuleException e) {
                throw new PluginRepositoryException("Cannot destroy plugin " + ruleParserPlugin);
            }
        }
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
