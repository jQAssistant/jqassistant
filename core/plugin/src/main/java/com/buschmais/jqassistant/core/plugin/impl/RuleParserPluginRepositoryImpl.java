package com.buschmais.jqassistant.core.plugin.impl;

import java.util.*;

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

    private Collection<RuleParserPlugin> ruleParserPlugins;

    public RuleParserPluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) throws PluginRepositoryException {
        super(pluginConfigurationReader);
        ruleParserPlugins = getRuleParserPlugins();
    }

    @Override
    public Collection<RuleParserPlugin> getRuleParserPlugins(RuleConfiguration ruleConfiguration) throws RuleException {
        for (RuleParserPlugin ruleParserPlugin : ruleParserPlugins) {
            ruleParserPlugin.configure(ruleConfiguration);
        }
        return ruleParserPlugins;
    }

    private Collection<RuleParserPlugin> getRuleParserPlugins() throws PluginRepositoryException {
        List<RuleParserPlugin> ruleParserPlugins = new LinkedList<>();
        for (JqassistantPlugin plugin : plugins) {
            RuleParserType ruleParser = plugin.getRuleParser();
            if (ruleParser != null) {
                for (IdClassType pluginType : ruleParser.getClazz()) {
                    RuleParserPlugin ruleParserPlugin = createInstance(pluginType.getValue());
                    ruleParserPlugin.initialize();
                    ruleParserPlugins.add(ruleParserPlugin);
                }
            }
        }
        return ruleParserPlugins;
    }

}
