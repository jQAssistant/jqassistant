package com.buschmais.jqassistant.core.plugin.impl;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException;
import com.buschmais.jqassistant.core.plugin.api.RuleSourceReaderPluginRepository;
import com.buschmais.jqassistant.core.plugin.schema.v1.IdClassType;
import com.buschmais.jqassistant.core.plugin.schema.v1.JqassistantPlugin;
import com.buschmais.jqassistant.core.plugin.schema.v1.RuleSourceReaderType;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleSourceReaderPlugin;

public class RuleSourceReaderPluginRepositoryImpl extends AbstractPluginRepository implements RuleSourceReaderPluginRepository {

    private Collection<RuleSourceReaderPlugin> ruleSourceReaderPlugins;

    public RuleSourceReaderPluginRepositoryImpl(PluginConfigurationReader pluginConfigurationReader) throws PluginRepositoryException {
        super(pluginConfigurationReader);
        ruleSourceReaderPlugins = getRuleSourceReaderPlugins();
    }

    @Override
    public Collection<RuleSourceReaderPlugin> getRuleSourceReaderPlugins(RuleConfiguration ruleConfiguration) throws RuleException {
        for (RuleSourceReaderPlugin ruleSourceReaderPlugin : ruleSourceReaderPlugins) {
            ruleSourceReaderPlugin.configure(ruleConfiguration);
        }
        return ruleSourceReaderPlugins;
    }

    private Collection<RuleSourceReaderPlugin> getRuleSourceReaderPlugins() throws PluginRepositoryException {
        List<RuleSourceReaderPlugin> ruleSourceReaderPlugins = new LinkedList<>();
        for (JqassistantPlugin plugin : plugins) {
            RuleSourceReaderType ruleSourceReader = plugin.getRuleSourceReader();
            if (ruleSourceReader != null) {
                for (IdClassType pluginType : ruleSourceReader.getClazz()) {
                    RuleSourceReaderPlugin ruleSourceReaderPlugin = createInstance(pluginType.getValue());
                    ruleSourceReaderPlugin.initialize();
                    ruleSourceReaderPlugins.add(ruleSourceReaderPlugin);
                }
            }
        }
        return ruleSourceReaderPlugins;
    }

}
