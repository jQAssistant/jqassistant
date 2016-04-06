package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.rule.*;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hamcrest.CoreMatchers;
import org.hamcrest.collection.IsEmptyCollection;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.api.rule.source.UrlRuleSource;

public class AsciiDocRuleSetReaderTest {

    @Test
    public void cypherRules() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/junit-without-assert.adoc");
        ConceptBucket concepts = ruleSet.getConceptBucket();
        assertThat(concepts.size(), equalTo(2));

        Concept concept1 = concepts.getById("junit4:TestClassOrMethod");
        assertThat(concept1.getId(), equalTo("junit4:TestClassOrMethod"));
        assertThat(concept1.getDescription(), CoreMatchers.containsString("labels them and their containing classes with `:Test` and `:Junit4`."));

        Executable executable1 = concept1.getExecutable();
        assertThat(executable1, instanceOf(CypherExecutable.class));
        assertThat(((CypherExecutable)executable1).getStatement(), containsString("c:Test:Junit4, m:Test:Junit4"));
        assertThat(concept1.getRequiresConcepts(), IsEmptyCollection.<String>empty());

        Concept concept2 = concepts.getById("junit4:AssertMethod");
        assertThat(concept2.getId(), containsString("junit4:AssertMethod"));
        assertThat(concept2.getDescription(), containsString("Labels all assertion methods declared by `org.junit.Assert` with `:Assert`."));

        Executable executable2 = concept2.getExecutable();
        assertThat(executable2, instanceOf(CypherExecutable.class));
        assertThat(((CypherExecutable)executable2).getStatement(), containsString("and assertMethod.signature =~ 'void assert.*'"));
        assertThat(concept2.getRequiresConcepts(), IsEmptyCollection.<String>empty());

        ConstraintBucket constraints = ruleSet.getConstraintBucket();
        assertThat(constraints.size(), equalTo(1));

        Constraint constraint = constraints.getById("junit4:TestMethodWithoutAssertion");
        assertThat(constraint.getId(), containsString("junit4:TestMethodWithoutAssertion"));
        assertThat(constraint.getDescription(), containsString("All test methods must perform assertions."));
        assertEquals("junit4:TestMethodWithoutAssertion", constraint.getId());
        assertEquals("All test methods must perform assertions.", constraint.getDescription());

        Executable constraintExecutable = constraint.getExecutable();
        assertThat(constraintExecutable, instanceOf(CypherExecutable.class));
        assertThat(((CypherExecutable) constraintExecutable).getStatement(), containsString("not (testMethod)-[:INVOKES*]->(:Method:Assert)"));

        assertThat(ruleSet.getConceptBucket().getIds(), containsInAnyOrder(constraint.getRequiresConcepts().toArray()));
    }

    @Test
    public void scriptRules() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/javascript-rules.adoc");
        ConceptBucket concepts = ruleSet.getConceptBucket();
        assertThat(concepts.size(), equalTo(1));

        Concept concept1 = concepts.getById("concept:JavaScript");
        assertThat(concept1.getId(), equalTo("concept:JavaScript"));
        assertThat(concept1.getDescription(), containsString("Demonstrates a concept using JavaScript."));
        assertThat(concept1.getRequiresConcepts(), IsEmptyCollection.<String>empty());

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
