package com.buschmais.jqassistant.core.analysis.impl;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.api.rule.source.UrlRuleSource;

public class AsciiDocRuleSetReaderTest {

    @Test
    public void cypherRules() throws Exception {
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        AsciiDocRuleSetReader reader = new AsciiDocRuleSetReader();
        final URL url = getClass().getResource("/junit-without-assert.adoc");
        RuleSource ruleSource = new UrlRuleSource(url);
        reader.read(asList(ruleSource), ruleSetBuilder);
        RuleSet ruleSet = ruleSetBuilder.getRuleSet();
        ConceptBucket concepts = ruleSet.getConceptBucket();
        assertEquals(2, concepts.size());
        Concept concept1 = concepts.getConcept("junit4:TestClassOrMethod");
        assertEquals("junit4:TestClassOrMethod", concept1.getId());
        assertEquals(true, concept1.getDescription().contains("labels them and their containing classes with `:Test` and `:Junit4`."));
        Executable executable1 = concept1.getExecutable();
        assertThat(executable1, instanceOf(CypherExecutable.class));
        assertEquals(true, ((CypherExecutable) executable1).getStatement().contains("c:Test:Junit4, m:Test:Junit4"));
        assertEquals(Collections.emptySet(), concept1.getRequiresConcepts());

        Concept concept2 = concepts.getConcept("junit4:AssertMethod");
        assertEquals("junit4:AssertMethod", concept2.getId());
        assertEquals("Labels all assertion methods declared by `org.junit.Assert` with `:Assert`.", concept2.getDescription());
        Executable executable2 = concept2.getExecutable();
        assertThat(executable2, instanceOf(CypherExecutable.class));
        assertEquals(true, ((CypherExecutable) executable2).getStatement().contains("and assertMethod.signature =~ 'void assert.*'"));
        assertEquals(Collections.emptySet(), concept2.getRequiresConcepts());

        ConstraintBucket constraints = ruleSet.getConstraintBucket();
        assertEquals(1, constraints.size());

        Constraint constraint = constraints.getConstraint("junit4:TestMethodWithoutAssertion");

        assertEquals("junit4:TestMethodWithoutAssertion", constraint.getId());
        assertEquals("All test methods must perform assertions.", constraint.getDescription());
        Executable constraintExecutable = constraint.getExecutable();
        assertThat(constraintExecutable, instanceOf(CypherExecutable.class));
        assertEquals(true, ((CypherExecutable) constraintExecutable).getStatement().contains("not (testMethod)-[:INVOKES*]->(:Method:Assert)"));
        assertEquals(new HashSet<>(ruleSet.getConceptBucket().getConceptIds()), constraint.getRequiresConcepts());

    }

    @Test
    public void scriptRules() throws Exception {
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        AsciiDocRuleSetReader reader = new AsciiDocRuleSetReader();
        final URL url = getClass().getResource("/javascript-rules.adoc");
        RuleSource ruleSource = new UrlRuleSource(url);
        reader.read(asList(ruleSource), ruleSetBuilder);
        RuleSet ruleSet = ruleSetBuilder.getRuleSet();
        ConceptBucket concepts = ruleSet.getConceptBucket();
        assertEquals(1, concepts.size());
        Concept concept1 = concepts.getConcept("concept:JavaScript");
        assertEquals("concept:JavaScript", concept1.getId());
        assertEquals(true, concept1.getDescription().contains("Demonstrates a concept using JavaScript."));
        Executable executable = concept1.getExecutable();
        assertThat(executable, instanceOf(ScriptExecutable.class));
        ScriptExecutable scriptExecutable = (ScriptExecutable) executable;
        assertThat(scriptExecutable, notNullValue());
        assertThat(scriptExecutable.getLanguage(), equalTo("javascript"));
        assertThat(scriptExecutable.getSource(), CoreMatchers.containsString("var row = new java.util.HashMap();"));
        assertEquals(Collections.emptySet(), concept1.getRequiresConcepts());
    }

    @Test
    public void groups() throws Exception {
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        AsciiDocRuleSetReader reader = new AsciiDocRuleSetReader();
        URL url = getClass().getResource("/group.adoc");
        RuleSource ruleSource = new UrlRuleSource(url);
        reader.read(asList(ruleSource), ruleSetBuilder);
        RuleSet ruleSet = ruleSetBuilder.getRuleSet();
        assertThat(ruleSet.getConceptBucket().getConceptIds(), hasItems("test:Concept", "test:CriticalConcept"));
        assertThat(ruleSet.getConstraintBucket().getConstraintIds(), hasItems("test:Constraint", "test:CriticalConstraint"));
        Map<String, Group> groups = ruleSet.getGroups();
        Group defaultGroup = groups.get("test:Default");
        assertThat(defaultGroup, notNullValue());
        Map<String, Severity> includedConcepts = defaultGroup.getConcepts();
        assertThat(includedConcepts.containsKey("test:Concept"), equalTo(true));
        assertThat(includedConcepts.get("test:Concept"), nullValue());
        assertThat(includedConcepts.containsKey("test:CriticalConcept"), equalTo(true));
        assertThat(includedConcepts.get("test:CriticalConcept"), equalTo(Severity.CRITICAL));
        Map<String, Severity> includedConstraints = defaultGroup.getConstraints();
        assertThat(includedConstraints.containsKey("test:Constraint"), equalTo(true));
        assertThat(includedConstraints.get("test:Constraint"), nullValue());
        assertThat(includedConstraints.containsKey("test:CriticalConstraint"), equalTo(true));
        assertThat(includedConstraints.get("test:CriticalConstraint"), equalTo(Severity.CRITICAL));
        Set<String> includedGroups = defaultGroup.getGroups();
        assertThat(includedGroups, IsCollectionContaining.hasItems("test:Group"));
        Group group = groups.get("test:Group");
        assertThat(group, notNullValue());
    }
}
