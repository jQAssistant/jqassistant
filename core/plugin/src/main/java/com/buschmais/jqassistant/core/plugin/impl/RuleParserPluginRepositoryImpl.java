package com.buschmais.jqassistant.core.plugin.impl;

import java.util.Collection;
import java.util.LinkedList;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RuleParserPluginRepository;
import com.buschmais.jqassistant.core.plugin.schema.v1.IdClassType;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.plugin.schema.v1.RuleParserType;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;

public class RuleParserPluginRepositoryImpl extends AbstractPluginRepository implements RuleParserPluginRepository {

    private Collection<RuleParserPlugin> ruleParserPlugins = new LinkedList<>();

    public RuleParserPluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) throws PluginRepositoryException {
        super(pluginConfigurationReader);
    }

    @Override
    public Collection<RuleParserPlugin> getRuleParserPlugins(RuleConfiguration ruleConfiguration) throws RuleException {
        for (RuleParserPlugin ruleParserPlugin : ruleParserPlugins) {
            ruleParserPlugin.configure(ruleConfiguration);
        }
        return ruleParserPlugins;
    }

    @Override
    public void initialize() throws PluginRepositoryException {
        for (JqassistantPlugin plugin : plugins) {
            RuleParserType ruleParser = plugin.getRuleParser();
            if (ruleParser != null) {
                for (IdClassType pluginType : ruleParser.getClazz()) {
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
    public void destroy() throws PluginRepositoryException {
        for (RuleParserPlugin ruleParserPlugin : ruleParserPlugins) {
            try {
                ruleParserPlugin.destroy();
            } catch (RuleException e) {
                throw new PluginRepositoryException("Cannot destroy plugin " + ruleParserPlugin);
            }
        }
    }

}
