package com.buschmais.jqassistant.core.analysis.impl;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;

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
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        AsciiDocRuleSetReader reader = new AsciiDocRuleSetReader();
        final URL url = getClass().getResource("/junit-without-assert.adoc");
        RuleSource ruleSource = new UrlRuleSource(url);
        reader.read(asList(ruleSource), ruleSetBuilder);
        RuleSet ruleSet = ruleSetBuilder.getRuleSet();

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

        Executable constraintExecutable = constraint.getExecutable();
        assertThat(constraintExecutable, instanceOf(CypherExecutable.class));
        assertThat(((CypherExecutable) constraintExecutable).getStatement(), containsString("not (testMethod)-[:INVOKES*]->(:Method:Assert)"));

        assertThat(ruleSet.getConceptBucket().getIds(), containsInAnyOrder(constraint.getRequiresConcepts().toArray()));
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
    }

    @Test
    public void groups() throws Exception {
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        AsciiDocRuleSetReader reader = new AsciiDocRuleSetReader();
        URL url = getClass().getResource("/group.adoc");
        RuleSource ruleSource = new UrlRuleSource(url);
        reader.read(asList(ruleSource), ruleSetBuilder);
        RuleSet ruleSet = ruleSetBuilder.getRuleSet();
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
        Set<String> includedGroups = defaultGroup.getGroups();
        assertThat(includedGroups, IsCollectionContaining.hasItems("test:Group"));
        Group group = groups.getById("test:Group");
        assertThat(group, notNullValue());
    }
}
