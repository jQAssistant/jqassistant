package com.buschmais.jqassistant.core.rule.api.model;

import java.net.URL;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.api.source.UrlRuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleParser;
import com.buschmais.jqassistant.core.rule.impl.reader.XmlRuleParserPlugin;
import com.buschmais.jqassistant.core.rule.impl.reader.YamlRuleParserPlugin;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public final class RuleSetTestHelper {

    private RuleSetTestHelper() {
    }

    public static RuleSet readRuleSet(String resource, Rule rule) throws RuleException {
        RuleParser ruleParser = new RuleParser(getDefaultRuleParserPlugins(rule));
        URL url = RuleSetTestHelper.class.getResource(resource);
        assertThat(url).as("Cannot read resource URL:" + resource).isNotNull();
        RuleSource ruleSource = new UrlRuleSource(url);
        return ruleParser.parse(singletonList(ruleSource));
    }

    public static <T> void verifyParameter(Map<String, Parameter> parameters, String name, Parameter.Type type, T defaultValue) {
        Parameter parameter = parameters.get(name);
        assertThat(parameter).as("Expected a parameter with name " + name).isNotNull();
        assertThat(parameter.getName()).isEqualTo(name);
        assertThat(parameter.getType()).isEqualTo(type);
        assertThat(parameter.getDefaultValue()).isEqualTo(defaultValue);
    }

    public static List<RuleParserPlugin> getDefaultRuleParserPlugins(Rule rule) throws RuleException {
        List<RuleParserPlugin> ruleParserPlugins = asList(new XmlRuleParserPlugin(), new YamlRuleParserPlugin());
        for (RuleParserPlugin ruleParserPlugin : ruleParserPlugins) {
            ruleParserPlugin.initialize();
            ruleParserPlugin.configure(rule);
        }
        return ruleParserPlugins;
    }
}
