package com.buschmais.jqassistant.core.analysis.impl;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.analysis.api.rule.source.FileRuleSource;

public class XmlRuleSetReaderTest {

    @Test
    public void readScriptRule() throws Exception {
        File xmlFile = new File(getClass().getResource("/javascript-rules.xml").getFile());
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        RuleSetReader reader = new XmlRuleSetReader();
        reader.read(asList(new FileRuleSource(xmlFile)), ruleSetBuilder);

        RuleSet ruleSet = ruleSetBuilder.getRuleSet();
        assertThat(ruleSet.getConceptBucket().size(), equalTo(1));
        assertThat(ruleSet.getConstraintBucket().size(), equalTo(1));
        assertThat(ruleSet.getConceptBucket().getIds(), contains("test:JavaScriptConcept"));
        assertThat(ruleSet.getConstraintBucket().getIds(), contains("test:JavaScriptConstraint"));
    }

}
