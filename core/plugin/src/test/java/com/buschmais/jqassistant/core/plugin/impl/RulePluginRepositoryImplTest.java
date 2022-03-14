package com.buschmais.jqassistant.core.plugin.impl;

import java.util.Collection;

import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.impl.reader.AsciidocRuleParserPlugin;
import com.buschmais.jqassistant.core.rule.impl.reader.XmlRuleParserPlugin;
import com.buschmais.jqassistant.core.rule.impl.reader.YamlRuleParserPlugin;
import com.buschmais.jqassistant.core.rule.spi.RulePluginRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RulePluginRepositoryImplTest {

    RulePluginRepository ruleParserPluginRepository;

    @Mock
    private Rule rule;

    @BeforeEach
    void setupPluginConfigurationReader() {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl();
        ruleParserPluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
        ruleParserPluginRepository.initialize();
    }

    @Test
    void yamlRuleReaderIsAvailable() throws RuleException {
        Collection<RuleParserPlugin> plugins = ruleParserPluginRepository.getRuleParserPlugins(rule);

        assertThat(plugins).anySatisfy(ruleParserPlugin -> {
            assertThat(ruleParserPlugin).isExactlyInstanceOf(YamlRuleParserPlugin.class);
        });
    }

    @Test
    void xmlRuleReaderIsAvailable() throws RuleException {
        Collection<RuleParserPlugin> plugins = ruleParserPluginRepository.getRuleParserPlugins(rule);

        assertThat(plugins).anySatisfy(ruleParserPlugin -> {
            assertThat(ruleParserPlugin).isExactlyInstanceOf(XmlRuleParserPlugin.class);
        });
    }

    @Test
    void asciidoctorRuleReaderIsAvailable() throws RuleException {
        Collection<RuleParserPlugin> plugins = ruleParserPluginRepository.getRuleParserPlugins(rule);

        assertThat(plugins).anySatisfy(ruleParserPlugin -> {
            assertThat(ruleParserPlugin).isExactlyInstanceOf(AsciidocRuleParserPlugin.class);
        });
    }
}
