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
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class SeverityTest {

    @Mock
    private Rule rule;

    @Test
    public void getSeverityFromName() throws RuleException {
        for (Severity severity : Severity.values()) {
            String name = severity.getValue();
            assertThat(Severity.fromValue(name)).isEqualTo(severity);
        }
    }

    @Test
    public void unknownSeverity() {
        assertThatThrownBy(() -> Severity.fromValue("foo")).isInstanceOf(RuleException.class);
    }

    @Test
    void noSeverity() throws RuleException {
        assertThat(Severity.fromValue(null)).isNull();
    }

    @Test
    void lowerCaseSeverity() throws RuleException {
        String value = Severity.INFO.name();

        Severity result = Severity.fromValue(value);

        assertThat(result).isEqualTo(Severity.INFO);
    }

    @Test
    void severityThreshold() throws RuleException {
        assertThat(Severity.Threshold.from(BLOCKER.name()).getThreshold()).isEqualTo(of(BLOCKER));
        assertThat(Severity.Threshold.from(CRITICAL.name()).getThreshold()).isEqualTo(of(CRITICAL));
        assertThat(Severity.Threshold.from(MAJOR.name()).getThreshold()).isEqualTo(of(MAJOR));
        assertThat(Severity.Threshold.from(MINOR.name()).getThreshold()).isEqualTo(of(MINOR));
        assertThat(Severity.Threshold.from(INFO.name()).getThreshold()).isEqualTo(of(INFO));
        assertThat(Severity.Threshold.from("never").getThreshold()).isEqualTo(empty());
        assertThat(Severity.Threshold.from("NEVER").getThreshold()).isEqualTo(empty());
    }

    @Test
    void exceedsSeverityThreshold() throws RuleException {
        assertThat(BLOCKER.exceeds(Severity.Threshold.from("never"))).isFalse();
        assertThat(BLOCKER.exceeds(Severity.Threshold.from(BLOCKER))).isTrue();
        assertThat(BLOCKER.exceeds(Severity.Threshold.from(CRITICAL))).isTrue();

        assertThat(CRITICAL.exceeds(Severity.Threshold.from("never"))).isFalse();
        assertThat(CRITICAL.exceeds(Severity.Threshold.from(CRITICAL))).isTrue();
        assertThat(CRITICAL.exceeds(Severity.Threshold.from(MAJOR))).isTrue();

        assertThat(MAJOR.exceeds(Severity.Threshold.from("never"))).isFalse();
        assertThat(MAJOR.exceeds(Severity.Threshold.from(MAJOR))).isTrue();
        assertThat(MAJOR.exceeds(Severity.Threshold.from(MINOR))).isTrue();

        assertThat(MINOR.exceeds(Severity.Threshold.from("never"))).isFalse();
        assertThat(MINOR.exceeds(Severity.Threshold.from(MINOR))).isTrue();
        assertThat(MINOR.exceeds(Severity.Threshold.from(INFO))).isTrue();

        assertThat(INFO.exceeds(Severity.Threshold.from("never"))).isFalse();
        assertThat(INFO.exceeds(Severity.Threshold.from(INFO))).isTrue();
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
        assertThat(ruleSet.getConceptBucket().getIds()).contains(conceptId);
        assertThat(ruleSet.getConstraintBucket().getIds()).contains(constraintId);
        GroupsBucket groups = ruleSet.getGroupsBucket();
        // Group without any severity definition
        Group group = groups.getById(groupId);
        assertThat(group).isNotNull();
        assertThat(group.getSeverity()).isEqualTo(expectedGroupSeverity);
        Map<String, Severity> includedConcepts = group.getConcepts();
        assertThat(includedConcepts.containsKey(conceptId)).isEqualTo(true);
        assertThat(includedConcepts.get(conceptId)).isEqualTo(expectedIncludedConceptSeverity);
        Map<String, Severity> includedConstraints = group.getConstraints();
        assertThat(includedConstraints.containsKey(constraintId)).isEqualTo(true);
        assertThat(includedConstraints.get(constraintId)).isEqualTo(expectedIncludedConstraintSeverity);
    }

    @Test
    void xmlDefaultSeverity() throws RuleException {
        doReturn(of(Severity.CRITICAL)).when(rule).defaultConceptSeverity();
        doReturn(of(Severity.CRITICAL)).when(rule).defaultConstraintSeverity();
        doReturn(of(Severity.CRITICAL)).when(rule).defaultGroupSeverity();

        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/severity.xml", rule);

        Group groupWithoutSeverity = ruleSet.getGroupsBucket().getById("test:GroupWithoutSeverity");
        assertThat(groupWithoutSeverity.getSeverity()).isEqualTo(Severity.CRITICAL);
        Group groupWithSeverity = ruleSet.getGroupsBucket().getById("test:GroupWithSeverity");
        assertThat(groupWithSeverity.getSeverity()).isEqualTo(BLOCKER);
        Concept concept = ruleSet.getConceptBucket().getById("test:Concept");
        assertThat(concept.getSeverity()).isEqualTo(Severity.CRITICAL);
        Constraint constraint = ruleSet.getConstraintBucket().getById("test:Constraint");
        assertThat(constraint.getSeverity()).isEqualTo(Severity.CRITICAL);
    }

    @Test
    void xmlRuleDefaultSeverity() throws RuleException {
        RuleSet ruleSet = RuleSetTestHelper.readRuleSet("/severity.xml", rule);

        Group groupWithoutSeverity = ruleSet.getGroupsBucket().getById("test:GroupWithoutSeverity");
        assertThat(groupWithoutSeverity.getSeverity()).isNull();
        Concept concept = ruleSet.getConceptBucket().getById("test:Concept");
        assertThat(concept.getSeverity()).isEqualTo(Severity.MINOR);
        Constraint constraint = ruleSet.getConstraintBucket().getById("test:Constraint");
        assertThat(constraint.getSeverity()).isEqualTo(Severity.MAJOR);
    }
}
