package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.rule.*;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class AsciiDocRuleSetReaderTest {

    @Test
    public void testReadDocument() throws Exception {
        AsciiDocRuleSetReader reader = new AsciiDocRuleSetReader();
        URL url = getClass().getResource("/junit-without-assert.adoc");
        RuleSet ruleSet = reader.read(asList(new RuleSource(url, RuleSource.Type.AsciiDoc)));
//        assertEquals(url.toString(),group.getId());
//        assertEquals("Find JUnit tests without assertions",group.getDescription());
        Map<String, Concept> concepts = ruleSet.getConcepts();
        assertEquals(2, concepts.size());
        Concept concept1 = concepts.get("junit4:TestClassOrMethod");
        assertEquals("junit4:TestClassOrMethod", concept1.getId());
        assertEquals(true, concept1.getDescription().contains("labels them and their containing classes with `:Test` and `:Junit4`."));
        assertEquals(true, concept1.getQuery().getCypher().contains("c:Test:Junit4, m:Test:Junit4"));
        assertEquals(Collections.emptySet(), concept1.getRequiresConcepts());

        Concept concept2 = concepts.get("junit4:AssertMethod");
        assertEquals("junit4:AssertMethod", concept2.getId());
        assertEquals("Labels all assertion methods declared by `org.junit.Assert` with `:Assert`.", concept2.getDescription());
        assertEquals(true, concept2.getQuery().getCypher().contains("and assertMethod.signature =~ 'void assert.*'"));
        assertEquals(Collections.emptySet(), concept2.getRequiresConcepts());

        Map<String, Constraint> constraints = ruleSet.getConstraints();
        assertEquals(1, constraints.size());
        Constraint constraint = constraints.values().iterator().next();

        assertEquals("junit4:TestMethodWithoutAssertion", constraint.getId());
        assertEquals("All test methods must perform assertions.", constraint.getDescription());
        assertEquals(true, constraint.getQuery().getCypher().contains("not (testMethod)-[:INVOKES*]->(:Method:Assert)"));
        assertEquals(new HashSet<>(ruleSet.getConcepts().values()), constraint.getRequiresConcepts());

    }
}
