package com.buschmais.jqassistant.core.analysis.api.rule;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleSourceReaderPlugin;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.api.source.UrlRuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.AsciiDocRuleSourceReaderPlugin;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleCollector;
import com.buschmais.jqassistant.core.rule.impl.reader.XmlRuleSourceReaderPlugin;

import org.hamcrest.Matchers;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public final class RuleSetTestHelper {

    private RuleSetTestHelper() {
    }

    public static RuleSet readRuleSet(String resource, RuleConfiguration ruleConfiguration) throws RuleException {
        RuleCollector ruleCollector = new RuleCollector(getDefaultRuleSourceReaderPlugins(ruleConfiguration));
        URL url = RuleSetTestHelper.class.getResource(resource);
        assertThat("Cannot read resource URL:" + resource, url, notNullValue());
        RuleSource ruleSource = new UrlRuleSource(url);
        return ruleCollector.read(Collections.singletonList(ruleSource));
    }

    public static <T> void verifyParameter(Map<String, Parameter> parameters, String name, Parameter.Type type, T defaultValue) {
        Parameter parameter = parameters.get(name);
        assertThat("Expected a parameter with name " + name, parameter, notNullValue());
        assertThat(parameter.getName(), equalTo(name));
        assertThat(parameter.getType(), equalTo(type));
        assertThat(parameter.getDefaultValue(), Matchers.<Object> equalTo(defaultValue));
    }

    public static List<RuleSourceReaderPlugin> getDefaultRuleSourceReaderPlugins(RuleConfiguration ruleConfiguration) throws RuleException {
        List<RuleSourceReaderPlugin> ruleSourceReaderPlugins = asList(new XmlRuleSourceReaderPlugin(), new AsciiDocRuleSourceReaderPlugin());
        for (RuleSourceReaderPlugin ruleSourceReaderPlugin : ruleSourceReaderPlugins) {
            ruleSourceReaderPlugin.initialize();
            ruleSourceReaderPlugin.configure(ruleConfiguration);
        }
        return ruleSourceReaderPlugins;
    }
}
