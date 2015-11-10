package com.buschmais.jqassistant.core.analysis.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

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
        assertEquals(1, ruleSet.getConceptBucket().size());
        assertEquals(1, ruleSet.getConstraints().size());
        for (String id : ruleSet.getConceptBucket().getConceptIds()) {
            assertEquals(true, "test:JavaScriptConcept".equals(id));
        }
        for (String id : ruleSet.getConstraints().keySet()) {
            assertEquals(true, "test:JavaScriptConstraint".equals(id));
        }
    }

}
