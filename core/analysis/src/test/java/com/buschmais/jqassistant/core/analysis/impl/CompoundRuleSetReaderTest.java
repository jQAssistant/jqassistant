package com.buschmais.jqassistant.core.analysis.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.CompoundRuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSetBuilder;
import com.buschmais.jqassistant.core.analysis.api.rule.source.FileRuleSource;
import com.buschmais.jqassistant.core.analysis.api.rule.source.UrlRuleSource;

public class CompoundRuleSetReaderTest {

    @Test
    public void testReadCompoundSources() throws Exception {
        File adocFile = new File(getClass().getResource("/junit-without-assert.adoc").getFile());
        File xmlFile = new File(getClass().getResource("/test-concepts.xml").getFile());
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        RuleSetReader reader = new CompoundRuleSetReader();
        reader.read(asList(new FileRuleSource(adocFile), new FileRuleSource(xmlFile)), ruleSetBuilder);
        RuleSet ruleSet = ruleSetBuilder.getRuleSet();
        assertEquals(3, ruleSet.getConceptBucket().size());
        assertEquals(2, ruleSet.getConstraintBucket().size());
        for (String id : ruleSet.getConceptBucket().getConceptIds()) {
            assertEquals(true, asList("junit4:TestClassOrMethod", "junit4:AssertMethod", "java:Throwable").contains(id));
        }
        for (String id : ruleSet.getConstraintBucket().getConstraintIds()) {
            assertEquals(true, asList("junit4:TestMethodWithoutAssertion", "example:ConstructorOfDateMustNotBeUsed").contains(id));
        }
        assertEquals(1, ruleSet.getGroupsBucket().size());
        Group group = ruleSet.getGroupsBucket().getGroup("default");
        assertEquals("default", group.getId());
        assertEquals(1, group.getConcepts().size());
        assertEquals("java:Throwable", group.getConcepts().keySet().iterator().next());
        assertEquals(1, group.getConstraints().size());
        assertEquals("example:ConstructorOfDateMustNotBeUsed", group.getConstraints().keySet().iterator().next());
    }

    @Test
    public void testReadUrlSource() throws Exception {
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        URL url = getClass().getResource("/test-concepts.xml");
        RuleSetReader reader = new XmlRuleSetReader();
        reader.read(Arrays.asList(new UrlRuleSource(url)), ruleSetBuilder);
        RuleSet ruleSet = ruleSetBuilder.getRuleSet();
        assertEquals(1, ruleSet.getConceptBucket().size());
        assertEquals(1, ruleSet.getConstraintBucket().size());
        for (String id : ruleSet.getConceptBucket().getConceptIds()) {
            assertEquals(true, asList("java:Throwable").contains(id));
        }
        for (String id : ruleSet.getConstraintBucket().getConstraintIds()) {
            assertEquals(true, asList("example:ConstructorOfDateMustNotBeUsed").contains(id));
        }
        assertEquals(1, ruleSet.getGroupsBucket().size());
        Group group = ruleSet.getGroupsBucket().getGroup("default");
        assertEquals("default", group.getId());
    }
}
