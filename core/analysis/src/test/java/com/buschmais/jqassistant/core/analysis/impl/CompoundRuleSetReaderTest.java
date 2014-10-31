package com.buschmais.jqassistant.core.analysis.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.CompoundRuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.source.UrlRuleSource;

public class CompoundRuleSetReaderTest {

    @Test
    public void testReadSources() throws Exception {
        URL adocUrl = getClass().getResource("/junit-without-assert.adoc");
        URL xmlUrl = getClass().getResource("/test-concepts.xml");
        RuleSetReader reader = new CompoundRuleSetReader();
        RuleSet ruleSet = reader.read(asList(new UrlRuleSource(adocUrl), new UrlRuleSource(xmlUrl)));
        assertEquals(3, ruleSet.getConcepts().size());
        assertEquals(2, ruleSet.getConstraints().size());
        for (String id : ruleSet.getConcepts().keySet()) {
            assertEquals(true, asList("junit4:TestClassOrMethod", "junit4:AssertMethod", "java:Throwable").contains(id));
        }
        for (String id : ruleSet.getConstraints().keySet()) {
            assertEquals(true, asList("junit4:TestMethodWithoutAssertion", "example:ConstructorOfDateMustNotBeUsed").contains(id));
        }
        assertEquals(1, ruleSet.getGroups().size());
        Group group = ruleSet.getGroups().values().iterator().next();
        assertEquals("default", group.getId());
        assertEquals(1, group.getConcepts().size());
        assertEquals("java:Throwable", group.getConcepts().iterator().next().getId());
        assertEquals(1, group.getConstraints().size());
        assertEquals("example:ConstructorOfDateMustNotBeUsed", group.getConstraints().iterator().next().getId());
    }
}
