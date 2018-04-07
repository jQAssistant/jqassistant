package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.executor.RuleSetExecutor;
import com.buschmais.jqassistant.core.rule.api.executor.RuleSetExecutorConfiguration;
import com.buschmais.jqassistant.core.rule.api.executor.RuleVisitor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RuleSetExecutorTest {

    @Mock
    private RuleVisitor visitor;

    private RuleSetExecutorConfiguration configuration;

    private RuleSetExecutor ruleExecutor;

    private Concept defaultConcept;
    private Concept overriddenConcept;
    private Constraint defaultConstraint;
    private Constraint overriddenConstraint;

    @Before
    public void setUp() throws Exception {
        configuration = new RuleSetExecutorConfiguration();
        ruleExecutor = new RuleSetExecutor(visitor, configuration);
        defaultConcept = Concept.builder().id("concept:Default").severity(Severity.MAJOR).build();
        overriddenConcept = Concept.builder().id("concept:Overridden").severity(Severity.MAJOR).build();
        defaultConstraint = Constraint.builder().id("constraint:Default").severity(Severity.MAJOR).build();
        overriddenConstraint = Constraint.builder().id("constraint:Overridden").severity(Severity.MAJOR).build();
    }

    @Test
    public void defaultGroupSeverity() throws RuleException {
        Group group = Group.builder().id("group").conceptId(defaultConcept.getId()).conceptId(overriddenConcept.getId(), Severity.CRITICAL)
                .constraintId(defaultConstraint.getId()).constraintId(overriddenConstraint.getId(), Severity.CRITICAL).build();
        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(defaultConcept).addConcept(overriddenConcept).addConstraint(defaultConstraint)
                .addConstraint(overriddenConstraint).addGroup(group).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder().addGroupId(group.getId()).build();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).beforeGroup(group, null);
        verify(visitor).visitConcept(defaultConcept, Severity.MAJOR);
        verify(visitor).visitConcept(overriddenConcept, Severity.CRITICAL);
        verify(visitor).visitConstraint(defaultConstraint, Severity.MAJOR);
        verify(visitor).visitConstraint(overriddenConstraint, Severity.CRITICAL);
        verify(visitor).afterGroup(group);
    }

    @Test
    public void overriddenGroupSeverity() throws RuleException {
        Group group = Group.builder().id("group").severity(Severity.BLOCKER).conceptId(defaultConcept.getId())
                .conceptId(overriddenConcept.getId(), Severity.CRITICAL).constraintId(defaultConstraint.getId())
                .constraintId(overriddenConstraint.getId(), Severity.CRITICAL).build();
        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(defaultConcept).addConcept(overriddenConcept).addConstraint(defaultConstraint)
                .addConstraint(overriddenConstraint).addGroup(group).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder().addGroupId(group.getId()).build();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).beforeGroup(group, Severity.BLOCKER);
        verify(visitor).visitConcept(defaultConcept, Severity.BLOCKER);
        verify(visitor).visitConcept(overriddenConcept, Severity.CRITICAL);
        verify(visitor).visitConstraint(defaultConstraint, Severity.BLOCKER);
        verify(visitor).visitConstraint(overriddenConstraint, Severity.CRITICAL);
        verify(visitor).afterGroup(group);
    }

    @Test
    public void optionalFailingConceptDependencies() throws RuleException {
        verifyConceptDependencies(true, false, times(1), never());
    }

    @Test
    public void defaultOptionalFailingConceptDependencies() throws RuleException {
        verifyConceptDependencies(null, false, times(1), never());
    }

    @Test
    public void requiredFailingConceptDependencies() throws RuleException {
        verifyConceptDependencies(false, false, never(), times(1));
    }

    @Test
    public void defaultRequiredFailingConceptDependencies() throws RuleException {
        configuration.setRequiredConceptsAreOptionalByDefault(false);
        verifyConceptDependencies(null, false, never(), times(1));
    }

    @Test
    public void requiredSuccessfulConceptDependencies() throws RuleException {
        verifyConceptDependencies(false, true, times(1), never());
    }

    @Test
    public void optionalSuccessfulConceptDependencies() throws RuleException {
        verifyConceptDependencies(true, true, times(1), never());
    }

    private void verifyConceptDependencies(Boolean optional, boolean status, VerificationMode visitVerification, VerificationMode skipVerification)
            throws RuleException {
        Concept dependencyConcept1 = Concept.builder().id("test:DependencyConcept1").build();
        Concept dependencyConcept2 = Concept.builder().id("test:DependencyConcept2").build();
        Map<String, Boolean> requiresConcepts = new HashMap<>();
        requiresConcepts.put("test:DependencyConcept1", optional);
        requiresConcepts.put("test:DependencyConcept2", optional);
        Concept concept = Concept.builder().id("test:Concept").requiresConceptIds(requiresConcepts).build();
        Constraint constraint = Constraint.builder().id("test:Constraint").requiresConceptIds(requiresConcepts).build();

        when(visitor.visitConcept(dependencyConcept1, null)).thenReturn(status);
        when(visitor.visitConcept(dependencyConcept2, null)).thenReturn(status);

        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(dependencyConcept1).addConcept(dependencyConcept2).addConcept(concept)
                .addConstraint(constraint).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder().addConceptId(concept.getId()).addConstraintId(constraint.getId()).build();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).visitConcept(dependencyConcept1, null);
        verify(visitor).visitConcept(dependencyConcept2, null);
        verify(visitor, visitVerification).visitConcept(concept, null);
        verify(visitor, skipVerification).skipConcept(concept, null);
        verify(visitor, visitVerification).visitConstraint(constraint, null);
        verify(visitor, skipVerification).skipConstraint(constraint, null);
    }

}
