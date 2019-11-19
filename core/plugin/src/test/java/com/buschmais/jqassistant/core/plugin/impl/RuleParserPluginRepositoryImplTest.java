package com.buschmais.jqassistant.core.plugin.impl;

import java.util.Collection;

import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.impl.reader.AsciidocRuleParserPlugin;
import com.buschmais.jqassistant.core.rule.impl.reader.XmlRuleParserPlugin;
import com.buschmais.jqassistant.core.rule.impl.reader.YamlRuleParserPlugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration.*;
import static org.assertj.core.api.Assertions.assertThat;

class RuleParserPluginRepositoryImplTest {
    RuleParserPluginRepositoryImpl ruleParserPluginRepository;

    @BeforeEach
    void setupPluginConfigurationReader() {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl();
        ruleParserPluginRepository = new RuleParserPluginRepositoryImpl(pluginConfigurationReader);
        ruleParserPluginRepository.initialize();
    }

    @Test
    void yamlRuleReaderIsAvailable() throws RuleException {
        Collection<RuleParserPlugin> plugins = ruleParserPluginRepository.getRuleParserPlugins(DEFAULT);

        assertThat(plugins).anySatisfy(ruleParserPlugin -> {
            assertThat(ruleParserPlugin).isExactlyInstanceOf(YamlRuleParserPlugin.class);
        });
    }

    @Test
    void xmlRuleReaderIsAvailable() throws RuleException {
        Collection<RuleParserPlugin> plugins = ruleParserPluginRepository.getRuleParserPlugins(DEFAULT);

        assertThat(plugins).anySatisfy(ruleParserPlugin -> {
            assertThat(ruleParserPlugin).isExactlyInstanceOf(XmlRuleParserPlugin.class);
        });
    }

    @Test
    void asciidoctorRuleReaderIsAvailable() throws RuleException {
        Collection<RuleParserPlugin> plugins = ruleParserPluginRepository.getRuleParserPlugins(DEFAULT);

        assertThat(plugins).anySatisfy(ruleParserPlugin -> {
            assertThat(ruleParserPlugin).isExactlyInstanceOf(AsciidocRuleParserPlugin.class);
        });
    }
}
