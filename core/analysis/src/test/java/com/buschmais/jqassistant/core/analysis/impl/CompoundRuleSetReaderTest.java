package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.CompoundRuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import org.junit.Test;

import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class CompoundRuleSetReaderTest {

    @Test
    public void testReadSources() throws Exception {
        URL adocUrl = getClass().getResource("/junit-without-assert.adoc");
        URL xmlUrl = getClass().getResource("/test-concepts.xml");
        RuleSetReader reader = new CompoundRuleSetReader();
        RuleSet ruleSet = reader.read(asList(new RuleSource(adocUrl, RuleSource.Type.AsciiDoc), new RuleSource(xmlUrl, RuleSource.Type.XML)));
        assertEquals(3,ruleSet.getConcepts().size());
        assertEquals(2,ruleSet.getConstraints().size());
        for (String id : ruleSet.getConcepts().keySet()) {
            assertEquals(true,asList("junit4:TestClassOrMethod","junit4:AssertMethod","java:Throwable").contains(id));
        }
        for (String id : ruleSet.getConstraints().keySet()) {
            assertEquals(true,asList("junit4:TestMethodWithoutAssertion","example:ConstructorOfDateMustNotBeUsed").contains(id));
        }
        assertEquals(1,ruleSet.getGroups().size());
        Group group = ruleSet.getGroups().values().iterator().next();
        assertEquals("default", group.getId());
        assertEquals(1, group.getConcepts().size());
        assertEquals("java:Throwable", group.getConcepts().iterator().next().getId());
        assertEquals(1, group.getConstraints().size());
        assertEquals("example:ConstructorOfDateMustNotBeUsed", group.getConstraints().iterator().next().getId());
    }
}
