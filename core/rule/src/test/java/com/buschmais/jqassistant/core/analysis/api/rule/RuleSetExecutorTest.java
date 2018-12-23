package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.executor.RuleSetExecutor;
import com.buschmais.jqassistant.core.rule.api.executor.RuleSetExecutorConfiguration;
import com.buschmais.jqassistant.core.rule.api.executor.RuleVisitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RuleSetExecutorTest {

    @Mock
    private RuleVisitor visitor;

    private RuleSetExecutorConfiguration configuration;

    private RuleSetExecutor ruleExecutor;

    private Concept defaultConcept;
    private Concept overriddenConcept;
    private Constraint defaultConstraint;
    private Constraint overriddenConstraint;

    @BeforeEach
    public void setUp() {
        configuration = new RuleSetExecutorConfiguration();
        ruleExecutor = new RuleSetExecutor(visitor, configuration);
        defaultConcept = Concept.builder().id("concept:Default").severity(Severity.MAJOR).build();
        overriddenConcept = Concept.builder().id("concept:Overridden").severity(Severity.MAJOR).build();
        defaultConstraint = Constraint.builder().id("constraint:Default").severity(Severity.MAJOR).build();
        overriddenConstraint = Constraint.builder().id("constraint:Overridden").severity(Severity.MAJOR).build();
    }

    @Test
    public void defaultGroupSeverity() throws RuleException {
        Group group = Group.builder().id("group").concept(defaultConcept.getId(), null).concept(overriddenConcept.getId(), Severity.CRITICAL)
                .constraint(defaultConstraint.getId(), null).constraint(overriddenConstraint.getId(), Severity.CRITICAL).build();
        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(defaultConcept).addConcept(overriddenConcept).addConstraint(defaultConstraint)
                .addConstraint(overriddenConstraint).addGroup(group).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder().groupId(group.getId()).build();

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
        Group group = Group.builder().id("group").severity(Severity.BLOCKER).concept(defaultConcept.getId(), null)
                .concept(overriddenConcept.getId(), Severity.CRITICAL).constraint(defaultConstraint.getId(), null)
                .constraint(overriddenConstraint.getId(), Severity.CRITICAL).build();
        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(defaultConcept).addConcept(overriddenConcept).addConstraint(defaultConstraint)
                .addConstraint(overriddenConstraint).addGroup(group).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder().groupId(group.getId()).build();

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

    @Test
    public void executionOrder() throws RuleException {
        Concept nestedConcept1 = Concept.builder().id("concept:Nested1").build();
        Concept nestedConcept2 = Concept.builder().id("concept:Nested2").build();
        Constraint nestetConstraint = Constraint.builder().id("constraint:Nested").build();
        Group nestedGroup = Group.builder().id("group:Nested").concept("concept:Nested1", null).concept("concept:Nested2", null)
                .constraint("constraint:Nested", null).build();
        Concept parentConcept1 = Concept.builder().id("concept:Parent1").build();
        Concept parentConcept2 = Concept.builder().id("concept:Parent2").build();
        Constraint parentConstraint = Constraint.builder().id("constraint:Parent").build();
        Group parentGroup = Group.builder().id("group:Parent").concept("concept:Parent1", null).concept("concept:Parent2", null)
                .constraint("constraint:Parent", null).group("group:Nested", null).build();
        Concept rootConcept = Concept.builder().id("concept:Root").build();
        Constraint rootConstraint = Constraint.builder().id("constraint:Root").build();
        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(nestedConcept1).addConcept(nestedConcept2).addConstraint(nestetConstraint)
                .addGroup(nestedGroup).addConcept(parentConcept1).addConcept(parentConcept2).addConstraint(parentConstraint).addGroup(parentGroup)
                .addConcept(rootConcept).addConstraint(rootConstraint).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder().conceptId("concept:Root").constraintId("constraint:Root").groupId("group:Parent")
                .build();

        ruleExecutor.execute(ruleSet, ruleSelection);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visitConcept(rootConcept, null);
        inOrder.verify(visitor).beforeGroup(parentGroup, null);
        inOrder.verify(visitor).visitConcept(parentConcept1, null);
        inOrder.verify(visitor).visitConcept(parentConcept2, null);
        inOrder.verify(visitor).beforeGroup(nestedGroup, null);
        inOrder.verify(visitor).visitConcept(nestedConcept1, null);
        inOrder.verify(visitor).visitConcept(nestedConcept2, null);
        inOrder.verify(visitor).visitConstraint(nestetConstraint, null);
        inOrder.verify(visitor).afterGroup(nestedGroup);
        inOrder.verify(visitor).visitConstraint(parentConstraint, null);
        inOrder.verify(visitor).afterGroup(parentGroup);
        inOrder.verify(visitor).visitConstraint(rootConstraint, null);
    }

    @Test
    public void wildcards() throws RuleException {
        Concept requiredConcept1 = Concept.builder().id("concept:Required1").build();
        Concept requiredConcept2 = Concept.builder().id("concept:Required2").build();
        Map<String, Boolean> requiresConcepts1 = new HashMap<>();
        requiresConcepts1.put("concept:Req*1", null);
        Concept dependentConcept = Concept.builder().id("concept:Dependent").requiresConcepts(requiresConcepts1).build();
        Map<String, Boolean> requiresConcepts2 = new HashMap<>();
        requiresConcepts2.put("concept:Req*2", null);
        Constraint dependentConstraint = Constraint.builder().id("constraint:Dependent").requiresConcepts(requiresConcepts2).build();
        Map<String, Severity> includesConcepts = new HashMap<>();
        includesConcepts.put("concept:Dependent", null);
        Map<String, Severity> includesConstraints= new HashMap<>();
        includesConstraints.put("constraint:Dependent", null);
        Group nestedGroup = Group.builder().id("group:Nested").concepts(includesConcepts).constraints(includesConstraints).build();

        Group group = Group.builder().id("group").group("*:Nested", null).concept("*:Default", null).constraint("*:Default", null).build();
        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(defaultConcept).addConcept(overriddenConcept).addConcept(requiredConcept1)
                .addConcept(requiredConcept2).addConcept(dependentConcept).addConstraint(defaultConstraint).addConstraint(overriddenConstraint)
                .addConstraint(dependentConstraint).addGroup(group).addGroup(nestedGroup).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder().groupId("*").build();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).beforeGroup(group, null);
        verify(visitor).afterGroup(group);
        verify(visitor).beforeGroup(nestedGroup, null);
        verify(visitor).afterGroup(nestedGroup);
        verify(visitor).visitConcept(defaultConcept, Severity.MAJOR);
        verify(visitor).visitConcept(dependentConcept, null);
        verify(visitor).visitConcept(requiredConcept1, null);
        verify(visitor).visitConcept(requiredConcept2, null);
        verify(visitor).visitConstraint(defaultConstraint, Severity.MAJOR);
        verify(visitor).visitConstraint(dependentConstraint, null);
        verify(visitor, never()).visitConcept(overriddenConcept, Severity.MAJOR);
        verify(visitor, never()).visitConstraint(overriddenConstraint, Severity.MAJOR);
    }

    private void verifyConceptDependencies(Boolean optional, boolean status, VerificationMode visitVerification, VerificationMode skipVerification)
            throws RuleException {
        Concept dependencyConcept1 = Concept.builder().id("test:DependencyConcept1").build();
        Concept dependencyConcept2 = Concept.builder().id("test:DependencyConcept2").build();
        Map<String, Boolean> requiresConcepts = new HashMap<>();
        requiresConcepts.put("test:DependencyConcept1", optional);
        requiresConcepts.put("test:DependencyConcept2", optional);
        Concept concept = Concept.builder().id("test:Concept").requiresConcepts(requiresConcepts).build();
        Constraint constraint = Constraint.builder().id("test:Constraint").requiresConcepts(requiresConcepts).build();

        lenient().when(visitor.visitConcept(dependencyConcept1, null)).thenReturn(status);
        lenient().when(visitor.visitConcept(dependencyConcept2, null)).thenReturn(status);

        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(dependencyConcept1).addConcept(dependencyConcept2).addConcept(concept)
                .addConstraint(constraint).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder().conceptId(concept.getId()).constraintId(constraint.getId()).build();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).visitConcept(dependencyConcept1, null);
        verify(visitor).visitConcept(dependencyConcept2, null);
        verify(visitor, visitVerification).visitConcept(concept, null);
        verify(visitor, skipVerification).skipConcept(concept, null);
        verify(visitor, visitVerification).visitConstraint(constraint, null);
        verify(visitor, skipVerification).skipConstraint(constraint, null);
    }

}
