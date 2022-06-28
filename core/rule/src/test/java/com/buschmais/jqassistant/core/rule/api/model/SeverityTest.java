package com.buschmais.jqassistant.core.rule.api.model;

import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.core.rule.api.model.Severity.*;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class SeverityTest {

    @Mock
    private Rule rule;

    @Test
    public void getSeverityFromName() throws RuleException {
        for (Severity severity : Severity.values()) {
            String name = severity.getValue();
            assertThat(Severity.fromValue(name), is(severity));
        }
    }

    @Test
    public void unknownSeverity() {
        assertThatThrownBy(() -> Severity.fromValue("foo")).isInstanceOf(RuleException.class);
    }

    @Test
    void noSeverity() throws RuleException {
        assertThat(Severity.fromValue(null), nullValue());
    }

    @Test
    void lowerCaseSeverity() throws RuleException {
        String value = Severity.INFO.name();

        Severity result = Severity.fromValue(value);

        assertThat(result, equalTo(Severity.INFO));
    }

    @Test
    void severityThreshold() throws RuleException {
        assertThat(Severity.Threshold.from(BLOCKER.name()).getThreshold(), equalTo(of(BLOCKER)));
        assertThat(Severity.Threshold.from(CRITICAL.name()).getThreshold(), equalTo(of(CRITICAL)));
        assertThat(Severity.Threshold.from(MAJOR.name()).getThreshold(), equalTo(of(MAJOR)));
        assertThat(Severity.Threshold.from(MINOR.name()).getThreshold(), equalTo(of(MINOR)));
        assertThat(Severity.Threshold.from(INFO.name()).getThreshold(), equalTo(of(INFO)));
        assertThat(Severity.Threshold.from("never").getThreshold(), equalTo(empty()));
        assertThat(Severity.Threshold.from("NEVER").getThreshold(), equalTo(empty()));
    }

    @Test
    void asciidocSeverity() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/severity.adoc", rule);
        verifySeverities(ruleSet, "test:GroupWithoutSeverity", null, "test:Concept", null, "test:Constraint", null);
        verifySeverities(ruleSet, "test:GroupWithSeverity", BLOCKER, "test:Concept", null, "test:Constraint", null);
        verifySeverities(ruleSet, "test:GroupWithOverridenSeverities", BLOCKER, "test:Concept", Severity.CRITICAL, "test:Constraint",
                Severity.CRITICAL);
    }

    @Test
    void xmlSeverity() throws Exception {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/severity.xml", rule);
        verifySeverities(ruleSet, "test:GroupWithoutSeverity", null, "test:Concept", null, "test:Constraint", null);
        verifySeverities(ruleSet, "test:GroupWithSeverity", BLOCKER, "test:Concept", null, "test:Constraint", null);
        verifySeverities(ruleSet, "test:GroupWithOverridenSeverities", BLOCKER, "test:Concept", Severity.CRITICAL, "test:Constraint",
                Severity.CRITICAL);
    }

    private void verifySeverities(RuleSet ruleSet, String groupId, Severity expectedGroupSeverity, String conceptId, Severity expectedIncludedConceptSeverity,
            String constraintId, Severity expectedIncludedConstraintSeverity) throws RuleException {
        assertThat(ruleSet.getConceptBucket().getIds(), hasItems(conceptId));
        assertThat(ruleSet.getConstraintBucket().getIds(), hasItems(constraintId));
        GroupsBucket groups = ruleSet.getGroupsBucket();
        // Group without any severity definition
        Group group = groups.getById(groupId);
        assertThat(group, notNullValue());
        assertThat(group.getSeverity(), equalTo(expectedGroupSeverity));
        Map<String, Severity> includedConcepts = group.getConcepts();
        assertThat(includedConcepts.containsKey(conceptId), equalTo(true));
        assertThat(includedConcepts.get(conceptId), equalTo(expectedIncludedConceptSeverity));
        Map<String, Severity> includedConstraints = group.getConstraints();
        assertThat(includedConstraints.containsKey(constraintId), equalTo(true));
        assertThat(includedConstraints.get(constraintId), equalTo(expectedIncludedConstraintSeverity));
    }

    @Test
    void asciidocDefaultSeverity() throws RuleException {
        doReturn(of(Severity.CRITICAL)).when(rule).defaultConceptSeverity();
        doReturn(of(Severity.CRITICAL)).when(rule).defaultConstraintSeverity();
        doReturn(of(Severity.CRITICAL)).when(rule).defaultGroupSeverity();
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/severity.adoc", rule);
        verifyDefaultSeverities(ruleSet, Severity.CRITICAL);
    }

    @Test
    void xmlDefaultSeverity() throws RuleException {
        doReturn(of(Severity.CRITICAL)).when(rule).defaultConceptSeverity();
        doReturn(of(Severity.CRITICAL)).when(rule).defaultConstraintSeverity();
        doReturn(of(Severity.CRITICAL)).when(rule).defaultGroupSeverity();
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/severity.xml", rule);
        verifyDefaultSeverities(ruleSet, Severity.CRITICAL);
    }

    private void verifyDefaultSeverities(RuleSet ruleSet, Severity defaultSeverity) throws RuleException {
        Group groupWithoutSeverity = ruleSet.getGroupsBucket().getById("test:GroupWithoutSeverity");
        assertThat(groupWithoutSeverity.getSeverity(), equalTo(defaultSeverity));
        Group groupWithSeverity = ruleSet.getGroupsBucket().getById("test:GroupWithSeverity");
        assertThat(groupWithSeverity.getSeverity(), equalTo(BLOCKER));
        Concept concept = ruleSet.getConceptBucket().getById("test:Concept");
        assertThat(concept.getSeverity(), equalTo(defaultSeverity));
        Constraint constraint = ruleSet.getConstraintBucket().getById("test:Constraint");
        assertThat(constraint.getSeverity(), equalTo(defaultSeverity));
    }

    @Test
    void xmlRuleDefaultSeverity() throws RuleException {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/severity.xml", rule);
        verifyRuleDefaultSeverity(ruleSet);
    }

    @Test
    void asciidocRuleDefaultSeverity() throws RuleException {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/severity.adoc", rule);
        verifyRuleDefaultSeverity(ruleSet);
    }

    private void verifyRuleDefaultSeverity(RuleSet ruleSet) throws RuleException {
        Group groupWithoutSeverity = ruleSet.getGroupsBucket().getById("test:GroupWithoutSeverity");
        assertThat(groupWithoutSeverity.getSeverity(), nullValue());
        Concept concept = ruleSet.getConceptBucket().getById("test:Concept");
        assertThat(concept.getSeverity(), equalTo(Severity.MINOR));
        Constraint constraint = ruleSet.getConstraintBucket().getById("test:Constraint");
        assertThat(constraint.getSeverity(), equalTo(Severity.MAJOR));
    }

}
