package com.buschmais.jqassistant.core.analysis.api.rule;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;

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
    public void asciidocSeverity() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/severity.adoc");
        verifySeverities(ruleSet, "test:GroupWithoutSeverity", null, "test:Concept", null, "test:Constraint", null);
        verifySeverities(ruleSet, "test:GroupWithSeverity", Severity.BLOCKER, "test:Concept", null, "test:Constraint", null);
        verifySeverities(ruleSet, "test:GroupWithOverridenSeverities", Severity.BLOCKER, "test:Concept", Severity.CRITICAL, "test:Constraint", Severity.CRITICAL);
    }

    @Test
    public void xmlSeverity() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/severity.xml");
        verifySeverities(ruleSet, "test:GroupWithoutSeverity", null, "test:Concept", null, "test:Constraint", null);
        verifySeverities(ruleSet, "test:GroupWithSeverity", Severity.BLOCKER, "test:Concept", null, "test:Constraint", null);
        verifySeverities(ruleSet, "test:GroupWithOverridenSeverities", Severity.BLOCKER, "test:Concept", Severity.CRITICAL, "test:Constraint", Severity.CRITICAL);
    }

    private void verifySeverities(RuleSet ruleSet, String groupId, Severity expectedGroupSeverity, String conceptId, Severity expectedConceptSeverity, String constraintId, Severity expectedConstraintSeverity) throws NoGroupException {
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
}
