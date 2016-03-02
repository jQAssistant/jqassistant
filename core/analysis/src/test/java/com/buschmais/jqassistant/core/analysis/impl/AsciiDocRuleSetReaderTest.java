package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.rule.*;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AsciiDocRuleSetReaderTest {

    @Test
    public void cypherRules() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/junit-without-assert.adoc");
        ConceptBucket concepts = ruleSet.getConceptBucket();
        assertEquals(2, concepts.size());
        Concept concept1 = concepts.getById("junit4:TestClassOrMethod");
        assertEquals("junit4:TestClassOrMethod", concept1.getId());
        assertEquals(true, concept1.getDescription().contains("labels them and their containing classes with `:Test` and `:Junit4`."));
        Executable executable1 = concept1.getExecutable();
        assertThat(executable1, instanceOf(CypherExecutable.class));
        assertEquals(true, ((CypherExecutable) executable1).getStatement().contains("c:Test:Junit4, m:Test:Junit4"));
        assertEquals(Collections.emptySet(), concept1.getRequiresConcepts());

        Concept concept2 = concepts.getById("junit4:AssertMethod");
        assertEquals("junit4:AssertMethod", concept2.getId());
        assertEquals("Labels all assertion methods declared by `org.junit.Assert` with `:Assert`.", concept2.getDescription());
        Executable executable2 = concept2.getExecutable();
        assertThat(executable2, instanceOf(CypherExecutable.class));
        assertEquals(true, ((CypherExecutable) executable2).getStatement().contains("and assertMethod.signature =~ 'void assert.*'"));
        assertEquals(Collections.emptySet(), concept2.getRequiresConcepts());

        ConstraintBucket constraints = ruleSet.getConstraintBucket();
        assertEquals(1, constraints.size());

        Constraint constraint = constraints.getById("junit4:TestMethodWithoutAssertion");

        assertEquals("junit4:TestMethodWithoutAssertion", constraint.getId());
        assertEquals("All test methods must perform assertions.", constraint.getDescription());
        Executable constraintExecutable = constraint.getExecutable();
        assertThat(constraintExecutable, instanceOf(CypherExecutable.class));
        assertEquals(true, ((CypherExecutable) constraintExecutable).getStatement().contains("not (testMethod)-[:INVOKES*]->(:Method:Assert)"));
        assertEquals(new HashSet<>(ruleSet.getConceptBucket().getIds()), constraint.getRequiresConcepts());

    }

    @Test
    public void scriptRules() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/javascript-rules.adoc");
        ConceptBucket concepts = ruleSet.getConceptBucket();
        assertEquals(1, concepts.size());
        Concept concept1 = concepts.getById("concept:JavaScript");
        assertEquals("concept:JavaScript", concept1.getId());
        assertEquals(true, concept1.getDescription().contains("Demonstrates a concept using JavaScript."));
        Executable executable = concept1.getExecutable();
        assertThat(executable, instanceOf(ScriptExecutable.class));
        ScriptExecutable scriptExecutable = (ScriptExecutable) executable;
        assertThat(scriptExecutable, notNullValue());
        assertThat(scriptExecutable.getLanguage(), equalTo("javascript"));
        assertThat(scriptExecutable.getSource(), containsString("var row = new java.util.HashMap();"));
        assertEquals(Collections.emptySet(), concept1.getRequiresConcepts());
    }

    @Test
    public void groups() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/group.adoc");
        assertThat(ruleSet.getConceptBucket().getIds(), hasItems("test:Concept", "test:CriticalConcept"));
        assertThat(ruleSet.getConstraintBucket().getIds(), hasItems("test:Constraint", "test:CriticalConstraint"));
        GroupsBucket groups = ruleSet.getGroupsBucket();
        Group defaultGroup = groups.getById("test:Default");
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
        Map<String, Severity> includedGroups = defaultGroup.getGroups();
        assertThat(includedGroups.keySet(), hasItems("test:Group"));
        Group group = groups.getById("test:Group");
        assertThat(group, notNullValue());
    }
}
