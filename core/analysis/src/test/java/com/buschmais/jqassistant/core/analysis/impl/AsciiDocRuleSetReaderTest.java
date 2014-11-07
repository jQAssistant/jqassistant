package com.buschmais.jqassistant.core.analysis.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.api.rule.source.UrlRuleSource;

public class AsciiDocRuleSetReaderTest {

    @Test
    public void testReadDocument() throws Exception {
        AsciiDocRuleSetReader reader = new AsciiDocRuleSetReader();
        final URL url = getClass().getResource("/junit-without-assert.adoc");
        RuleSource ruleSource = new UrlRuleSource(url);
        RuleSet ruleSet = reader.read(asList(ruleSource));
        // assertEquals(url.toString(),group.getId());
        // assertEquals("Find JUnit tests without assertions",group.getDescription());
        Map<String, Concept> concepts = ruleSet.getConcepts();
        assertEquals(2, concepts.size());
        Concept concept1 = concepts.get("junit4:TestClassOrMethod");
        assertEquals("junit4:TestClassOrMethod", concept1.getId());
        assertEquals(true, concept1.getDescription().contains("labels them and their containing classes with `:Test` and `:Junit4`."));
        assertEquals(true, concept1.getCypher().contains("c:Test:Junit4, m:Test:Junit4"));
        assertEquals(Collections.emptySet(), concept1.getRequiresConcepts());

        Concept concept2 = concepts.get("junit4:AssertMethod");
        assertEquals("junit4:AssertMethod", concept2.getId());
        assertEquals("Labels all assertion methods declared by `org.junit.Assert` with `:Assert`.", concept2.getDescription());
        assertEquals(true, concept2.getCypher().contains("and assertMethod.signature =~ 'void assert.*'"));
        assertEquals(Collections.emptySet(), concept2.getRequiresConcepts());

        Map<String, Constraint> constraints = ruleSet.getConstraints();
        assertEquals(1, constraints.size());
        Constraint constraint = constraints.values().iterator().next();

        assertEquals("junit4:TestMethodWithoutAssertion", constraint.getId());
        assertEquals("All test methods must perform assertions.", constraint.getDescription());
        assertEquals(true, constraint.getCypher().contains("not (testMethod)-[:INVOKES*]->(:Method:Assert)"));
        assertEquals(new HashSet<>(ruleSet.getConcepts().keySet()), constraint.getRequiresConcepts());

    }
}
