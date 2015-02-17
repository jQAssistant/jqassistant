package com.buschmais.jqassistant.core.analysis.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.source.FileRuleSource;

public class XmlRuleSetReaderTest {

    @Test
    public void readScriptRule() throws Exception {
        File xmlFile = new File(getClass().getResource("/javascript-rules.xml").getFile());
        RuleSetReader reader = new XmlRuleSetReader();
        RuleSet ruleSet = reader.read(asList(new FileRuleSource(xmlFile)));
        assertEquals(1, ruleSet.getConcepts().size());
        assertEquals(1, ruleSet.getConstraints().size());
        for (String id : ruleSet.getConcepts().keySet()) {
            assertEquals(true, asList("test:JavaScriptConcept").contains(id));
        }
        for (String id : ruleSet.getConstraints().keySet()) {
            assertEquals(true, asList("test:JavaScriptConstraint").contains(id));
        }
    }

}
