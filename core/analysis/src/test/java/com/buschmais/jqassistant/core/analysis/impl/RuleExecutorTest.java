package com.buschmais.jqassistant.core.analysis.impl;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

import com.buschmais.jqassistant.core.analysis.api.*;
import com.buschmais.jqassistant.core.analysis.api.rule.*;

@RunWith(MockitoJUnitRunner.class)
public class RuleExecutorTest {

    @Mock
    private RuleVisitor visitor;

    private RuleExecutorConfiguration configuration;

    private RuleExecutor ruleExecutor;

    private Concept defaultConcept;
    private Concept overriddenConcept;
    private Constraint defaultConstraint;
    private Constraint overriddenConstraint;

    @Before
    public void setUp() throws Exception {
        configuration = new RuleExecutorConfiguration();
        ruleExecutor = new RuleExecutor(visitor, configuration);
        defaultConcept = Concept.Builder.newConcept().id("concept:Default").severity(Severity.MAJOR).get();
        overriddenConcept = Concept.Builder.newConcept().id("concept:Overridden").severity(Severity.MAJOR).get();
        defaultConstraint = Constraint.Builder.newConstraint().id("constraint:Default").severity(Severity.MAJOR).get();
        overriddenConstraint = Constraint.Builder.newConstraint().id("constraint:Overridden").severity(Severity.MAJOR).get();
    }

    @Test
    public void defaultGroupSeverity() throws RuleException, AnalysisException {
        Group group = Group.Builder.newGroup().id("group").conceptId(defaultConcept.getId()).conceptId(overriddenConcept.getId(), Severity.CRITICAL)
                .constraintId(defaultConstraint.getId()).constraintId(overriddenConstraint.getId(), Severity.CRITICAL).get();
        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(defaultConcept).addConcept(overriddenConcept).addConstraint(defaultConstraint)
                .addConstraint(overriddenConstraint).addGroup(group).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.Builder.newInstance().addGroupId(group.getId()).get();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).beforeGroup(group, null);
        verify(visitor).visitConcept(defaultConcept, Severity.MAJOR);
        verify(visitor).visitConcept(overriddenConcept, Severity.CRITICAL);
        verify(visitor).visitConstraint(defaultConstraint, Severity.MAJOR);
        verify(visitor).visitConstraint(overriddenConstraint, Severity.CRITICAL);
        verify(visitor).afterGroup(group);
    }

    @Test
    public void overriddenGroupSeverity() throws RuleException, AnalysisException {
        Group group = Group.Builder.newGroup().id("group").severity(Severity.BLOCKER).conceptId(defaultConcept.getId())
                .conceptId(overriddenConcept.getId(), Severity.CRITICAL).constraintId(defaultConstraint.getId())
                .constraintId(overriddenConstraint.getId(), Severity.CRITICAL).get();
        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(defaultConcept).addConcept(overriddenConcept).addConstraint(defaultConstraint)
                .addConstraint(overriddenConstraint).addGroup(group).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.Builder.newInstance().addGroupId(group.getId()).get();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).beforeGroup(group, Severity.BLOCKER);
        verify(visitor).visitConcept(defaultConcept, Severity.BLOCKER);
        verify(visitor).visitConcept(overriddenConcept, Severity.CRITICAL);
        verify(visitor).visitConstraint(defaultConstraint, Severity.BLOCKER);
        verify(visitor).visitConstraint(overriddenConstraint, Severity.CRITICAL);
        verify(visitor).afterGroup(group);
    }

    @Test
    public void optionalFailingConceptDependencies() throws RuleException, AnalysisException {
        verifyConceptDependencies(true, Result.Status.FAILURE, times(1), never());
    }

    @Test
    public void defaultOptionalFailingConceptDependencies() throws RuleException, AnalysisException {
        verifyConceptDependencies(null, Result.Status.FAILURE, times(1), never());
    }

    @Test
    public void requiredFailingConceptDependencies() throws RuleException, AnalysisException {
        verifyConceptDependencies(false, Result.Status.FAILURE, never(), times(1));
    }

    @Test
    public void defaultRequiredFailingConceptDependencies() throws RuleException, AnalysisException {
        configuration.setRequiredConceptsAreOptionalByDefault(false);
        verifyConceptDependencies(null, Result.Status.FAILURE, never(), times(1));
    }

    @Test
    public void requiredSuccessfulConceptDependencies() throws RuleException, AnalysisException {
        verifyConceptDependencies(false, Result.Status.SUCCESS, times(1), never());
    }

    @Test
    public void optionalSuccessfulConceptDependencies() throws RuleException, AnalysisException {
        verifyConceptDependencies(true, Result.Status.SUCCESS, times(1), never());
    }

    private void verifyConceptDependencies(Boolean optional, Result.Status status, VerificationMode visitVerification, VerificationMode skipVerification)
            throws RuleException, AnalysisException {
        Concept dependencyConcept1 = Concept.Builder.newConcept().id("test:DependencyConcept1").get();
        Concept dependencyConcept2 = Concept.Builder.newConcept().id("test:DependencyConcept2").get();
        Map<String, Boolean> requiresConcepts = new HashMap<>();
        requiresConcepts.put("test:DependencyConcept1", optional);
        requiresConcepts.put("test:DependencyConcept2", optional);
        Concept concept = Concept.Builder.newConcept().id("test:Concept").requiresConceptIds(requiresConcepts).get();
        Constraint constraint = Constraint.Builder.newConstraint().id("test:Constraint").requiresConceptIds(requiresConcepts).get();

        when(visitor.visitConcept(dependencyConcept1, null)).thenReturn(Result.Status.SUCCESS.equals(status));
        when(visitor.visitConcept(dependencyConcept2, null)).thenReturn(Result.Status.SUCCESS.equals(status));

        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(dependencyConcept1).addConcept(dependencyConcept2).addConcept(concept)
                .addConstraint(constraint).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.Builder.newInstance().addConceptId(concept.getId()).addConstraintId(constraint.getId()).get();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).visitConcept(dependencyConcept1, null);
        verify(visitor).visitConcept(dependencyConcept2, null);
        verify(visitor, visitVerification).visitConcept(concept, null);
        verify(visitor, skipVerification).skipConcept(concept, null);
        verify(visitor, visitVerification).visitConstraint(constraint, null);
        verify(visitor, skipVerification).skipConstraint(constraint, null);
    }

}