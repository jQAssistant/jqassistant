package com.buschmais.jqassistant.core.rule.api.model;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.api.source.UrlRuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.AsciidocRuleParserPlugin;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleParser;
import com.buschmais.jqassistant.core.rule.impl.reader.XmlRuleParserPlugin;
import com.buschmais.jqassistant.core.rule.impl.reader.YamlRuleParserPlugin;

import org.hamcrest.Matchers;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public final class RuleSetTestHelper {

    private RuleSetTestHelper() {
    }

    public static RuleSet readRuleSet(String resource, Rule rule) throws RuleException {
        RuleParser ruleParser = new RuleParser(getDefaultRuleParserPlugins(rule));
        URL url = RuleSetTestHelper.class.getResource(resource);
        assertThat("Cannot read resource URL:" + resource, url, notNullValue());
        RuleSource ruleSource = new UrlRuleSource(url);
        return ruleParser.parse(Collections.singletonList(ruleSource));
    }

    public static <T> void verifyParameter(Map<String, Parameter> parameters, String name, Parameter.Type type, T defaultValue) {
        Parameter parameter = parameters.get(name);
        assertThat("Expected a parameter with name " + name, parameter, notNullValue());
        assertThat(parameter.getName(), equalTo(name));
        assertThat(parameter.getType(), equalTo(type));
        assertThat(parameter.getDefaultValue(), Matchers.<Object> equalTo(defaultValue));
    }

    public static List<RuleParserPlugin> getDefaultRuleParserPlugins(Rule rule) throws RuleException {
        List<RuleParserPlugin> ruleParserPlugins = asList(new AsciidocRuleParserPlugin(), new XmlRuleParserPlugin(),
                                                          new YamlRuleParserPlugin());
        for (RuleParserPlugin ruleParserPlugin : ruleParserPlugins) {
            ruleParserPlugin.initialize();
            ruleParserPlugin.configure(rule);
        }
        return ruleParserPlugins;
    }
}
