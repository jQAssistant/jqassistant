package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.CompoundRuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.api.rule.source.UrlRuleSource;

import java.net.URL;
import java.util.Collections;

public final class RuleSetTestHelper {

    private RuleSetTestHelper() {
    }

    public static RuleSet readRuleSet(String resource) throws RuleException {
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        CompoundRuleSetReader reader = new CompoundRuleSetReader();
        final URL url = RuleSetTestHelper.class.getResource(resource);
        RuleSource ruleSource = new UrlRuleSource(url);
        reader.read(Collections.singletonList(ruleSource), ruleSetBuilder);
        return ruleSetBuilder.getRuleSet();
    }
}
