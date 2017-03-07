package com.buschmais.jqassistant.core.analysis.api.rule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.hamcrest.Matchers;

import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.api.source.UrlRuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.CompoundRuleSetReader;

public final class RuleSetTestHelper {

    private RuleSetTestHelper() {
    }

    public static RuleSet readRuleSet(String resource) throws RuleException {
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        CompoundRuleSetReader reader = new CompoundRuleSetReader(RuleConfiguration.builder().build());
        URL url = RuleSetTestHelper.class.getResource(resource);
        assertThat("Cannot read resource URL:" + resource, url, notNullValue());
        RuleSource ruleSource = new UrlRuleSource(url);
        reader.read(Collections.singletonList(ruleSource), ruleSetBuilder);
        return ruleSetBuilder.getRuleSet();
    }

    public static <T> void verifyParameter(Map<String, Parameter> parameters, String name, Parameter.Type type, T defaultValue) {
        Parameter parameter = parameters.get(name);
        assertThat("Expected a parameter with name " + name, parameter, notNullValue());
        assertThat(parameter.getName(), equalTo(name));
        assertThat(parameter.getType(), equalTo(type));
        assertThat(parameter.getDefaultValue(), Matchers.<Object> equalTo(defaultValue));
    }

}
