package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.RuleException;
import com.buschmais.jqassistant.core.analysis.api.RuleSelection;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RuleExecutorTest {

    @Mock
    private RuleVisitor visitor;

    private Concept defaultConcept;
    private Concept overriddenConcept;
    private Constraint defaultConstraint;
    private Constraint overriddenConstraint;

    @Before
    public void setUp() throws Exception {
        defaultConcept = Concept.Builder.newConcept().id("concept:Default").severity(Severity.MAJOR).get();
        overriddenConcept = Concept.Builder.newConcept().id("concept:Overridden").severity(Severity.MAJOR).get();
        defaultConstraint = Constraint.Builder.newConstraint().id("constraint:Default").severity(Severity.MAJOR).get();
        overriddenConstraint = Constraint.Builder.newConstraint().id("constraint:Overridden").severity(Severity.MAJOR).get();
    }

    @Test
    public void defaultGroupSeverity() throws RuleException, AnalysisException {
        Group group =  Group.Builder.newGroup().id("group").conceptId(defaultConcept.getId()).conceptId(overriddenConcept.getId(), Severity.CRITICAL).constraintId(defaultConstraint.getId()).constraintId(overriddenConstraint.getId(), Severity.CRITICAL).get();
        RuleSet ruleSet =  RuleSetBuilder.newInstance().addConcept(defaultConcept).addConcept(overriddenConcept).addConstraint(defaultConstraint).addConstraint(overriddenConstraint).addGroup(group).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.Builder.newInstance().addGroupId(group.getId()).get();
        RuleExecutor executor = new RuleExecutor(visitor);

        executor.execute(ruleSet, ruleSelection);

        verify(visitor).beforeGroup(group, null);
        verify(visitor).visitConcept(defaultConcept, Severity.MAJOR);
        verify(visitor).visitConcept(overriddenConcept, Severity.CRITICAL);
        verify(visitor).visitConstraint(defaultConstraint, Severity.MAJOR);
        verify(visitor).visitConstraint(overriddenConstraint, Severity.CRITICAL);
        verify(visitor).afterGroup(group);
    }

    @Test
    public void overriddenGroupSeverity() throws RuleException, AnalysisException {
        Group group =  Group.Builder.newGroup().id("group").severity(Severity.BLOCKER).conceptId(defaultConcept.getId()).conceptId(overriddenConcept.getId(), Severity.CRITICAL).constraintId(defaultConstraint.getId()).constraintId(overriddenConstraint.getId(), Severity.CRITICAL).get();
        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(defaultConcept).addConcept(overriddenConcept).addConstraint(defaultConstraint).addConstraint(overriddenConstraint).addGroup(group).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.Builder.newInstance().addGroupId(group.getId()).get();
        RuleExecutor executor = new RuleExecutor(visitor);

        executor.execute(ruleSet, ruleSelection);

        verify(visitor).beforeGroup(group, Severity.BLOCKER);
        verify(visitor).visitConcept(defaultConcept, Severity.BLOCKER);
        verify(visitor).visitConcept(overriddenConcept, Severity.CRITICAL);
        verify(visitor).visitConstraint(defaultConstraint, Severity.BLOCKER);
        verify(visitor).visitConstraint(overriddenConstraint, Severity.CRITICAL);
        verify(visitor).afterGroup(group);
    }
}