package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

public class SeverityTest {

    @Test
    public void getSeverityFromName() throws RuleException {
        for (Severity severity : Severity.values()) {
            String name = severity.getValue();
            assertThat(Severity.fromValue(name), is(severity));
        }
    }

    @Test(expected = RuleException.class)
    public void unknownSeverity() throws RuleException {
        Severity.fromValue("foo");
    }

    @Test
    public void noSeverity() throws RuleException {
        assertThat(Severity.fromValue(null), nullValue());
    }

    @Test
    public void lowerCaseSeverity() throws RuleException {
        String value = Severity.INFO.name();

        Severity result = Severity.fromValue(value);

        assertThat(result, equalTo(Severity.INFO));
    }

    @Test
    public void asciidocSeverity() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/severity.adoc");
        verifySeverities(ruleSet, "test:GroupWithoutSeverity", null, "test:Concept", null, "test:Constraint", null);
        verifySeverities(ruleSet, "test:GroupWithSeverity", Severity.BLOCKER, "test:Concept", null, "test:Constraint", null);
        verifySeverities(ruleSet, "test:GroupWithOverridenSeverities", Severity.BLOCKER, "test:Concept", Severity.CRITICAL, "test:Constraint",
                Severity.CRITICAL);
    }

    @Test
    public void xmlSeverity() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/severity.xml");
        verifySeverities(ruleSet, "test:GroupWithoutSeverity", null, "test:Concept", null, "test:Constraint", null);
        verifySeverities(ruleSet, "test:GroupWithSeverity", Severity.BLOCKER, "test:Concept", null, "test:Constraint", null);
        verifySeverities(ruleSet, "test:GroupWithOverridenSeverities", Severity.BLOCKER, "test:Concept", Severity.CRITICAL, "test:Constraint",
                Severity.CRITICAL);
    }

    private void verifySeverities(RuleSet ruleSet, String groupId, Severity expectedGroupSeverity, String conceptId, Severity expectedConceptSeverity,
            String constraintId, Severity expectedConstraintSeverity) throws NoGroupException {
        assertThat(ruleSet.getConceptBucket().getIds(), hasItems(conceptId));
        assertThat(ruleSet.getConstraintBucket().getIds(), hasItems(constraintId));
        GroupsBucket groups = ruleSet.getGroupsBucket();
        // Group without any severity definition
        Group group = groups.getById(groupId);
        assertThat(group, notNullValue());
        assertThat(group.getSeverity(), equalTo(expectedGroupSeverity));
        Map<String, Severity> includedConcepts = group.getConcepts();
        assertThat(includedConcepts.containsKey(conceptId), equalTo(true));
        assertThat(includedConcepts.get(conceptId), equalTo(expectedConceptSeverity));
        Map<String, Severity> includedConstraints = group.getConstraints();
        assertThat(includedConstraints.containsKey(constraintId), equalTo(true));
        assertThat(includedConstraints.get(constraintId), equalTo(expectedConstraintSeverity));
    }

    @Test
    public void asciidocDefaultSeverity() throws RuleException {
        RuleConfiguration ruleConfiguration = RuleConfiguration.builder().defaultConceptSeverity(Severity.CRITICAL).defaultConstraintSeverity(Severity.CRITICAL)
                .defaultGroupSeverity(Severity.CRITICAL).build();
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/severity.adoc", ruleConfiguration);
        verifyDefaultSeverities(ruleSet, Severity.CRITICAL);
    }

    @Test
    public void xmlDefaultSeverity() throws RuleException {
        RuleConfiguration ruleConfiguration = RuleConfiguration.builder().defaultConceptSeverity(Severity.CRITICAL).defaultConstraintSeverity(Severity.CRITICAL)
                .defaultGroupSeverity(Severity.CRITICAL).build();
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/severity.xml", ruleConfiguration);
        verifyDefaultSeverities(ruleSet, Severity.CRITICAL);
    }

    private void verifyDefaultSeverities(RuleSet ruleSet, Severity defaultSeverity) throws NoGroupException, NoConceptException, NoConstraintException {
        Group groupWithoutSeverity = ruleSet.getGroupsBucket().getById("test:GroupWithoutSeverity");
        assertThat(groupWithoutSeverity.getSeverity(), equalTo(defaultSeverity));
        Group groupWithSeverity = ruleSet.getGroupsBucket().getById("test:GroupWithSeverity");
        assertThat(groupWithSeverity.getSeverity(), equalTo(Severity.BLOCKER));
        Concept concept = ruleSet.getConceptBucket().getById("test:Concept");
        assertThat(concept.getSeverity(), equalTo(defaultSeverity));
        Constraint constraint = ruleSet.getConstraintBucket().getById("test:Constraint");
        assertThat(constraint.getSeverity(), equalTo(defaultSeverity));
    }

    @Test
    public void xmlRuleDefaultSeverity() throws RuleException {
        RuleConfiguration ruleConfiguration = RuleConfiguration.builder().build();
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/severity.xml", ruleConfiguration);
        verifyRuleDefaultSeverity(ruleSet);
    }

    @Test
    public void asciidocRuleDefaultSeverity() throws RuleException {
        RuleConfiguration ruleConfiguration = RuleConfiguration.builder().build();
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/severity.adoc", ruleConfiguration);
        verifyRuleDefaultSeverity(ruleSet);
    }

    private void verifyRuleDefaultSeverity(RuleSet ruleSet) throws NoGroupException, NoConceptException, NoConstraintException {
        Group groupWithoutSeverity = ruleSet.getGroupsBucket().getById("test:GroupWithoutSeverity");
        assertThat(groupWithoutSeverity.getSeverity(), equalTo(null));
        Concept concept = ruleSet.getConceptBucket().getById("test:Concept");
        assertThat(concept.getSeverity(), equalTo(Severity.MINOR));
        Constraint constraint = ruleSet.getConstraintBucket().getById("test:Constraint");
        assertThat(constraint.getSeverity(), equalTo(Severity.MAJOR));
    }

}
