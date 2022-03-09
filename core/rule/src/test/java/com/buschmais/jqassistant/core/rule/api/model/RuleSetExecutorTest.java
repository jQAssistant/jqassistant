package com.buschmais.jqassistant.core.rule.api.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.rule.api.executor.RuleSetExecutor;
import com.buschmais.jqassistant.core.rule.api.executor.RuleVisitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import static com.buschmais.jqassistant.core.rule.api.model.Severity.CRITICAL;
import static java.util.Collections.emptyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleSetExecutorTest {

    @Mock
    private RuleVisitor visitor;

    @Mock
    private Rule configuration;

    private RuleSetExecutor ruleExecutor;

    private Concept defaultConcept;
    private Concept overriddenConcept;
    private Constraint defaultConstraint;
    private Constraint overriddenConstraint;

    @BeforeEach
    void setUp() {
        ruleExecutor = new RuleSetExecutor(visitor, configuration);
        defaultConcept = Concept.builder().id("concept:Default").severity(Severity.MAJOR).build();
        overriddenConcept = Concept.builder().id("concept:Overridden").severity(Severity.MAJOR).build();
        defaultConstraint = Constraint.builder().id("constraint:Default").severity(Severity.MAJOR).build();
        overriddenConstraint = Constraint.builder().id("constraint:Overridden").severity(Severity.MAJOR).build();
    }

    @Test
    void defaultGroupSeverity() throws RuleException {
        Group group = Group.builder().id("group").concept(defaultConcept.getId(), null).concept(overriddenConcept.getId(), CRITICAL)
                .constraint(defaultConstraint.getId(), null).constraint(overriddenConstraint.getId(), CRITICAL).build();
        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(defaultConcept).addConcept(overriddenConcept).addConstraint(defaultConstraint)
                .addConstraint(overriddenConstraint).addGroup(group).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder().groupId(group.getId()).build();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).beforeGroup(group, null);
        verify(visitor).visitConcept(defaultConcept, Severity.MAJOR);
        verify(visitor).visitConcept(overriddenConcept, CRITICAL);
        verify(visitor).visitConstraint(defaultConstraint, Severity.MAJOR);
        verify(visitor).visitConstraint(overriddenConstraint, CRITICAL);
        verify(visitor).afterGroup(group);
    }

    @Test
    void overriddenGroupSeverity() throws RuleException {
        Group group = Group.builder().id("group").severity(Severity.BLOCKER).concept(defaultConcept.getId(), null).concept(overriddenConcept.getId(), CRITICAL)
                .constraint(defaultConstraint.getId(), null).constraint(overriddenConstraint.getId(), CRITICAL).build();
        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(defaultConcept).addConcept(overriddenConcept).addConstraint(defaultConstraint)
                .addConstraint(overriddenConstraint).addGroup(group).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder().groupId(group.getId()).build();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).beforeGroup(group, Severity.BLOCKER);
        verify(visitor).visitConcept(defaultConcept, Severity.BLOCKER);
        verify(visitor).visitConcept(overriddenConcept, CRITICAL);
        verify(visitor).visitConstraint(defaultConstraint, Severity.BLOCKER);
        verify(visitor).visitConstraint(overriddenConstraint, CRITICAL);
        verify(visitor).afterGroup(group);
    }

    @Test
    void optionalFailingConceptDependencies() throws RuleException {
        verifyRequiredConcepts(true, false, times(1), never());
    }

    @Test
    void defaultOptionalFailingConceptDependencies() throws RuleException {
        doReturn(true).when(configuration).requiredConceptsAreOptionalByDefault();
        verifyRequiredConcepts(null, false, times(1), never());
    }

    @Test
    void requiredFailingConceptDependencies() throws RuleException {
        verifyRequiredConcepts(false, false, never(), times(1));
    }

    @Test
    void defaultRequiredFailingConceptDependencies() throws RuleException {
        verifyRequiredConcepts(null, false, never(), times(1));
    }

    @Test
    void requiredSuccessfulConceptDependencies() throws RuleException {
        verifyRequiredConcepts(false, true, times(1), never());
    }

    @Test
    void optionalSuccessfulConceptDependencies() throws RuleException {
        verifyRequiredConcepts(true, true, times(1), never());
    }

    @Test
    void executionOrder() throws RuleException {
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
        RuleSelection ruleSelection = RuleSelection.builder().conceptId("concept:Root").constraintId("constraint:Root").groupId("group:Parent").build();

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
    void wildcards() throws RuleException {
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
        Map<String, Severity> includesConstraints = new HashMap<>();
        includesConstraints.put("constraint:Dependent", null);
        Group nestedGroup = Group.builder().id("group:Nested").concepts(includesConcepts).constraints(includesConstraints).build();
        doReturn(true).when(configuration).requiredConceptsAreOptionalByDefault();

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

    @Test
    void conceptRequiresItselfByWildcard() throws RuleException {
        Concept requiredConcept = Concept.builder().id("concept:RequiredConcept").requiresConcepts(emptyMap()).build();
        Map<String, Boolean> requiredConcepts = new HashMap<>();
        requiredConcepts.put("concept:*", null); // matches both DependentConcept and RequiredConcept
        Concept dependentConcept = Concept.builder().id("concept:DependentConcept").requiresConcepts(requiredConcepts).build();
        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(requiredConcept).addConcept(dependentConcept).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder().conceptId("concept:DependentConcept").build();
        doReturn(true).when(configuration).requiredConceptsAreOptionalByDefault();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).visitConcept(requiredConcept, null);
        verify(visitor).visitConcept(dependentConcept, null);
    }

    private void verifyRequiredConcepts(Boolean optional, boolean status, VerificationMode visitVerification, VerificationMode skipVerification)
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

    @Test
    void providedConcepts() throws RuleException {
        Concept baseConcept = Concept.builder().id("concept:BaseConcept").severity(CRITICAL).requiresConcepts(emptyMap()).build();
        Set<String> providedConcepts = new HashSet<>();
        providedConcepts.add("concept:BaseConcept");
        Concept providingConcept = Concept.builder().id("concept:ProvidingConcept").providesConcepts(providedConcepts).build();
        RuleSet ruleSet = RuleSetBuilder.newInstance().addConcept(baseConcept).addConcept(providingConcept).getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder().conceptId("concept:BaseConcept").build();

        ruleExecutor.execute(ruleSet, ruleSelection);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor).visitConcept(providingConcept, CRITICAL);
        inOrder.verify(visitor).visitConcept(baseConcept, CRITICAL);
    }
}
