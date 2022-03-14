package com.buschmais.jqassistant.core.plugin.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.ClasspathRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.spi.RulePluginRepository;

import org.jqassistant.schema.plugin.v1.IdClassListType;
import org.jqassistant.schema.plugin.v1.IdClassType;
import org.jqassistant.schema.plugin.v1.JqassistantPlugin;
import org.jqassistant.schema.plugin.v1.RulesType;

/**
 * Rule repository implementation.
 */
public class RulePluginRepositoryImpl extends AbstractPluginRepository implements RulePluginRepository {

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
    public Collection<RuleParserPlugin> getRuleParserPlugins(Rule rule) throws RuleException {
        for (RuleParserPlugin ruleParserPlugin : ruleParserPlugins) {
            ruleParserPlugin.configure(rule);
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
                for (String relativePath : rulesType.getResource()) {
                    sources.add(new ClasspathRuleSource(classLoader, relativePath));
                }
            }
        }
        return sources;
    }
}
