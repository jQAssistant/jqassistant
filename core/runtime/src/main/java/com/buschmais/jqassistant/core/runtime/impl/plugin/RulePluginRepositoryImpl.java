package com.buschmais.jqassistant.core.runtime.impl.plugin;

import java.net.URL;
import java.util.*;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.ClasspathRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.spi.RulePluginRepository;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginConfigurationReader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepositoryException;

import org.jqassistant.schema.plugin.v2.IdClassListType;
import org.jqassistant.schema.plugin.v2.IdClassType;
import org.jqassistant.schema.plugin.v2.JqassistantPlugin;
import org.jqassistant.schema.plugin.v2.RulesType;

/**
 * Rule repository implementation.
 */
public class RulePluginRepositoryImpl extends AbstractPluginRepository implements RulePluginRepository {

    private final List<RuleSource> sources;

    private final Collection<RuleParserPlugin> ruleParserPlugins = new LinkedList<>();

    /**
     * Constructor.
     */
    public RulePluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) {
        super(pluginConfigurationReader);
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
        for (JqassistantPlugin plugin : plugins.values()) {
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

    private List<RuleSource> getRuleSources(Map<URL, JqassistantPlugin> plugins) {
        List<RuleSource> sources = new ArrayList<>();
        for (Map.Entry<URL, JqassistantPlugin> pluginEntry : plugins.entrySet()) {
            JqassistantPlugin plugin = pluginEntry.getValue();
            RulesType rulesType = plugin.getRules();
            if (rulesType != null) {
                for (String relativePath : rulesType.getResource()) {
                    URL pluginBaseUrl = pluginEntry.getKey();
                    sources.add(new ClasspathRuleSource(pluginBaseUrl, relativePath));
                }
            }
        }
        return sources;
    }
}
