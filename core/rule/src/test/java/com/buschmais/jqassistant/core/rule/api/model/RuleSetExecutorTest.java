package com.buschmais.jqassistant.core.rule.api.model;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
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

import static com.buschmais.jqassistant.core.rule.api.model.Severity.*;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyMap;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleSetExecutorTest {

    @Mock
    private RuleVisitor<Boolean> visitor;

    @Mock
    private Rule configuration;

    private RuleSetExecutor<Boolean> ruleExecutor;

    private Concept defaultConcept;
    private Concept overriddenConcept;
    private Constraint defaultConstraint;
    private Constraint overriddenConstraint;

    @BeforeEach
    void setUp() throws RuleException {
        ruleExecutor = new RuleSetExecutor<>(visitor, configuration);
        defaultConcept = Concept.builder()
            .id("concept:Default")
            .severity(MAJOR)
            .build();
        overriddenConcept = Concept.builder()
            .id("concept:Overridden")
            .severity(MAJOR)
            .build();
        defaultConstraint = Constraint.builder()
            .id("constraint:Default")
            .severity(MAJOR)
            .build();
        overriddenConstraint = Constraint.builder()
            .id("constraint:Overridden")
            .severity(MAJOR)
            .build();
        lenient().doReturn(TRUE)
            .when(visitor)
            .visitConcept(any(Concept.class), any(Severity.class), anyMap(), anyMap());
    }

    @Test
    void defaultGroupSeverity() throws RuleException {
        Group group = Group.builder()
            .id("group")
            .concept(defaultConcept.getId(), null)
            .concept(overriddenConcept.getId(), CRITICAL)
            .constraint(defaultConstraint.getId(), null)
            .constraint(overriddenConstraint.getId(), CRITICAL)
            .build();
        RuleSet ruleSet = RuleSetBuilder.newInstance()
            .addConcept(defaultConcept)
            .addConcept(overriddenConcept)
            .addConstraint(defaultConstraint)
            .addConstraint(overriddenConstraint)
            .addGroup(group)
            .getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder()
            .groupId(group.getId())
            .build();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).beforeGroup(group, null);
        verify(visitor).visitConcept(defaultConcept, MAJOR, emptyMap(), emptyMap());
        verify(visitor).visitConcept(overriddenConcept, CRITICAL, emptyMap(), emptyMap());
        verify(visitor).visitConstraint(defaultConstraint, MAJOR, emptyMap());
        verify(visitor).visitConstraint(overriddenConstraint, CRITICAL, emptyMap());
        verify(visitor).afterGroup(group);
    }

    @Test
    void overriddenGroupSeverity() throws RuleException {
        Group group = Group.builder()
            .id("group")
            .severity(BLOCKER)
            .concept(defaultConcept.getId(), null)
            .concept(overriddenConcept.getId(), CRITICAL)
            .constraint(defaultConstraint.getId(), null)
            .constraint(overriddenConstraint.getId(), CRITICAL)
            .build();
        RuleSet ruleSet = RuleSetBuilder.newInstance()
            .addConcept(defaultConcept)
            .addConcept(overriddenConcept)
            .addConstraint(defaultConstraint)
            .addConstraint(overriddenConstraint)
            .addGroup(group)
            .getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder()
            .groupId(group.getId())
            .build();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).beforeGroup(group, BLOCKER);
        verify(visitor).visitConcept(defaultConcept, BLOCKER, emptyMap(), emptyMap());
        verify(visitor).visitConcept(overriddenConcept, CRITICAL, emptyMap(), emptyMap());
        verify(visitor).visitConstraint(defaultConstraint, BLOCKER, emptyMap());
        verify(visitor).visitConstraint(overriddenConstraint, CRITICAL, emptyMap());
        verify(visitor).afterGroup(group);
    }

    @Test
    void optionalFailingConceptDependencies() throws RuleException {
        verifyRequiredConcepts(true, false, times(1), never());
    }

    @Test
    void defaultOptionalFailingConceptDependencies() throws RuleException {
        doReturn(true).when(configuration)
            .requiredConceptsAreOptionalByDefault();
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
        Concept nestedConcept1 = Concept.builder()
            .id("concept:Nested1")
            .severity(MINOR)
            .build();
        Concept nestedConcept2 = Concept.builder()
            .id("concept:Nested2")
            .severity(MINOR)
            .build();
        Constraint nestetConstraint = Constraint.builder()
            .id("constraint:Nested")
            .severity(MAJOR)
            .build();
        Group nestedGroup = Group.builder()
            .id("group:Nested")
            .concept("concept:Nested1", null)
            .concept("concept:Nested2", null)
            .constraint("constraint:Nested", null)
            .build();
        Concept parentConcept1 = Concept.builder()
            .id("concept:Parent1")
            .severity(MINOR)
            .build();
        Concept parentConcept2 = Concept.builder()
            .id("concept:Parent2")
            .severity(MINOR)
            .build();
        Constraint parentConstraint = Constraint.builder()
            .id("constraint:Parent")
            .severity(MAJOR)
            .build();
        Group parentGroup = Group.builder()
            .id("group:Parent")
            .concept("concept:Parent1", null)
            .concept("concept:Parent2", null)
            .constraint("constraint:Parent", null)
            .group("group:Nested", null)
            .build();
        Concept rootConcept = Concept.builder()
            .id("concept:Root")
            .severity(MINOR)
            .build();
        Constraint rootConstraint = Constraint.builder()
            .id("constraint:Root")
            .severity(MAJOR)
            .build();
        RuleSet ruleSet = RuleSetBuilder.newInstance()
            .addConcept(nestedConcept1)
            .addConcept(nestedConcept2)
            .addConstraint(nestetConstraint)
            .addGroup(nestedGroup)
            .addConcept(parentConcept1)
            .addConcept(parentConcept2)
            .addConstraint(parentConstraint)
            .addGroup(parentGroup)
            .addConcept(rootConcept)
            .addConstraint(rootConstraint)
            .getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder()
            .conceptId("concept:Root")
            .constraintId("constraint:Root")
            .groupId("group:Parent")
            .build();

        ruleExecutor.execute(ruleSet, ruleSelection);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor)
            .visitConcept(rootConcept, MINOR, emptyMap(), emptyMap());
        inOrder.verify(visitor)
            .beforeGroup(parentGroup, null);
        inOrder.verify(visitor)
            .visitConcept(parentConcept1, MINOR, emptyMap(), emptyMap());
        inOrder.verify(visitor)
            .visitConcept(parentConcept2, MINOR, emptyMap(), emptyMap());
        inOrder.verify(visitor)
            .beforeGroup(nestedGroup, null);
        inOrder.verify(visitor)
            .visitConcept(nestedConcept1, MINOR, emptyMap(), emptyMap());
        inOrder.verify(visitor)
            .visitConcept(nestedConcept2, MINOR, emptyMap(), emptyMap());
        inOrder.verify(visitor)
            .visitConstraint(nestetConstraint, MAJOR, emptyMap());
        inOrder.verify(visitor)
            .afterGroup(nestedGroup);
        inOrder.verify(visitor)
            .visitConstraint(parentConstraint, MAJOR, emptyMap());
        inOrder.verify(visitor)
            .afterGroup(parentGroup);
        inOrder.verify(visitor)
            .visitConstraint(rootConstraint, MAJOR, emptyMap());
    }

    @Test
    void wildcards() throws RuleException {
        Concept requiredConcept1 = Concept.builder()
            .id("concept:Required1")
            .severity(MINOR)
            .build();
        Concept requiredConcept2 = Concept.builder()
            .id("concept:Required2")
            .severity(MINOR)
            .build();
        Map<String, Boolean> requiresConcepts1 = new HashMap<>();
        requiresConcepts1.put("concept:Req*1", null);
        Concept dependentConcept = Concept.builder()
            .id("concept:Dependent")
            .severity(MINOR)
            .requiresConcepts(requiresConcepts1)
            .build();
        Map<String, Boolean> requiresConcepts2 = new HashMap<>();
        requiresConcepts2.put("concept:Req*2", null);
        Constraint dependentConstraint = Constraint.builder()
            .id("constraint:Dependent")
            .severity(MAJOR)
            .requiresConcepts(requiresConcepts2)
            .build();
        Map<String, Severity> includesConcepts = new HashMap<>();
        includesConcepts.put("concept:Dependent", null);
        Map<String, Severity> includesConstraints = new HashMap<>();
        includesConstraints.put("constraint:Dependent", null);
        Group nestedGroup = Group.builder()
            .id("group:Nested")
            .concepts(includesConcepts)
            .constraints(includesConstraints)
            .build();
        doReturn(true).when(configuration)
            .requiredConceptsAreOptionalByDefault();

        Group group = Group.builder()
            .id("group")
            .group("*:Nested", null)
            .concept("*:Default", null)
            .constraint("*:Default", null)
            .build();
        RuleSet ruleSet = RuleSetBuilder.newInstance()
            .addConcept(defaultConcept)
            .addConcept(overriddenConcept)
            .addConcept(requiredConcept1)
            .addConcept(requiredConcept2)
            .addConcept(dependentConcept)
            .addConstraint(defaultConstraint)
            .addConstraint(overriddenConstraint)
            .addConstraint(dependentConstraint)
            .addGroup(group)
            .addGroup(nestedGroup)
            .getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder()
            .groupId("*")
            .build();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).beforeGroup(group, null);
        verify(visitor).afterGroup(group);
        verify(visitor).beforeGroup(nestedGroup, null);
        verify(visitor).afterGroup(nestedGroup);
        verify(visitor).visitConcept(defaultConcept, MAJOR, emptyMap(), emptyMap());
        verify(visitor).visitConcept(eq(dependentConcept), eq(MINOR), anyMap(), anyMap());
        verify(visitor).visitConcept(requiredConcept1, MINOR, emptyMap(), emptyMap());
        verify(visitor).visitConcept(requiredConcept2, MINOR, emptyMap(), emptyMap());
        verify(visitor).visitConstraint(defaultConstraint, MAJOR, emptyMap());
        verify(visitor).visitConstraint(eq(dependentConstraint), eq(MAJOR), anyMap());
        verify(visitor, never()).visitConcept(overriddenConcept, MAJOR, emptyMap(), emptyMap());
        verify(visitor, never()).visitConstraint(overriddenConstraint, MAJOR, emptyMap());
    }

    @Test
    void conceptRequiresItselfByWildcard() throws RuleException {
        Concept requiredConcept = Concept.builder()
            .id("concept:RequiredConcept")
            .severity(MINOR)
            .requiresConcepts(emptyMap())
            .build();
        Map<String, Boolean> requiredConcepts = new HashMap<>();
        requiredConcepts.put("concept:*", null); // matches both DependentConcept and RequiredConcept
        Concept dependentConcept = Concept.builder()
            .id("concept:DependentConcept")
            .severity(MINOR)
            .requiresConcepts(requiredConcepts)
            .build();
        RuleSet ruleSet = RuleSetBuilder.newInstance()
            .addConcept(requiredConcept)
            .addConcept(dependentConcept)
            .getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder()
            .conceptId("concept:DependentConcept")
            .build();
        doReturn(true).when(configuration)
            .requiredConceptsAreOptionalByDefault();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).visitConcept(requiredConcept, MINOR, emptyMap(), emptyMap());
        verify(visitor).visitConcept(eq(dependentConcept), eq(MINOR), anyMap(), anyMap());
    }

    private void verifyRequiredConcepts(Boolean optional, boolean status, VerificationMode visitVerification, VerificationMode skipVerification)
        throws RuleException {
        Concept dependencyConcept1 = Concept.builder()
            .id("test:DependencyConcept1")
            .severity(MINOR)
            .build();
        Concept dependencyConcept2 = Concept.builder()
            .id("test:DependencyConcept2")
            .severity(MINOR)
            .build();
        Map<String, Boolean> requiresConcepts = new HashMap<>();
        requiresConcepts.put("test:DependencyConcept1", optional);
        requiresConcepts.put("test:DependencyConcept2", optional);
        Concept concept = Concept.builder()
            .id("test:Concept")
            .severity(MINOR)
            .requiresConcepts(requiresConcepts)
            .build();
        Constraint constraint = Constraint.builder()
            .id("test:Constraint")
            .severity(MAJOR)
            .requiresConcepts(requiresConcepts)
            .build();

        lenient().when(visitor.visitConcept(dependencyConcept1, MINOR, emptyMap(), emptyMap()))
            .thenReturn(status);
        lenient().when(visitor.visitConcept(dependencyConcept2, MINOR, emptyMap(), emptyMap()))
            .thenReturn(status);
        lenient().doAnswer(i -> i.getArgument(0))
            .when(visitor)
            .isSuccess(any());

        RuleSet ruleSet = RuleSetBuilder.newInstance()
            .addConcept(dependencyConcept1)
            .addConcept(dependencyConcept2)
            .addConcept(concept)
            .addConstraint(constraint)
            .getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder()
            .conceptId(concept.getId())
            .constraintId(constraint.getId())
            .build();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).visitConcept(dependencyConcept1, MINOR, emptyMap(), emptyMap());
        verify(visitor).visitConcept(dependencyConcept2, MINOR, emptyMap(), emptyMap());
        verify(visitor, visitVerification).visitConcept(eq(concept), eq(MINOR), anyMap(), anyMap());
        verify(visitor, skipVerification).skipConcept(eq(concept), eq(MINOR), anyMap());
        verify(visitor, visitVerification).visitConstraint(eq(constraint), eq(MAJOR), anyMap());
        verify(visitor, skipVerification).skipConstraint(eq(constraint), eq(MAJOR), anyMap());
    }

    @Test
    void providedConcepts() throws RuleException {
        Concept concept = Concept.builder()
            .id("concept:Concept")
            .severity(CRITICAL)
            .requiresConcepts(Map.of("concept:AbstractConcept", true))
            .build();
        Concept requiredConcept = Concept.builder()
            .id("concept:RequiredConcept")
            .severity(MINOR)
            .requiresConcepts(emptyMap())
            .build();
        Concept abstractConcept = Concept.builder()
            .id("concept:AbstractConcept")
            .severity(MINOR)
            .requiresConcepts(Map.of("concept:RequiredConcept", true))
            .build();
        // provides the required concept directly
        Concept providingConcept1 = Concept.builder()
            .id("concept:ProvidingConcept1")
            .severity(MINOR)
            .providedConcepts(Set.of("concept:AbstractConcept"))
            .build();
        // provides the required concept indirectly via the group below
        Concept providingConcept2 = Concept.builder()
            .id("concept:ProvidingConcept2")
            .severity(MINOR)
            .build();
        Group group = Group.builder()
            .id("group")
            .concept("concept:ProvidingConcept1", null)
            .concept("concept:Concept", null)
            .providedConcepts(Map.of("concept:AbstractConcept", Set.of("concept:ProvidingConcept2")))
            .build();
        RuleSet ruleSet = RuleSetBuilder.newInstance()
            .addConcept(concept)
            .addConcept(requiredConcept)
            .addConcept(abstractConcept)
            .addConcept(providingConcept1)
            .addConcept(providingConcept2)
            .addGroup(group)
            .getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder()
            .groupId("group")
            .build();

        ruleExecutor.execute(ruleSet, ruleSelection);

        InOrder inOrder = inOrder(visitor);
        inOrder.verify(visitor)
            .visitConcept(requiredConcept, MINOR, emptyMap(), emptyMap());
        inOrder.verify(visitor)
            .visitConcept(eq(providingConcept1), eq(MINOR), anyMap(), anyMap());
        inOrder.verify(visitor)
            .visitConcept(eq(providingConcept2), eq(MINOR), anyMap(), anyMap());
        inOrder.verify(visitor)
            .visitConcept(abstractConcept, MINOR, ofEntries(entry(new SimpleEntry<>(requiredConcept, TRUE), TRUE)),
                ofEntries(entry(providingConcept1, TRUE), entry(providingConcept2, TRUE)));
        inOrder.verify(visitor)
            .visitConcept(eq(concept), eq(CRITICAL), anyMap(), anyMap());
    }

    @Test
    void excludeConstraint() throws RuleException {
        Constraint constraint = Constraint.builder()
            .id("test:Constraint")
            .severity(MAJOR)
            .build();
        Constraint excludedConstraint = Constraint.builder()
            .id("test:ExcludedConstraint")
            .severity(MAJOR)
            .build();
        Group group = Group.builder()
            .id("group")
            .constraint("test:Constraint", null)
            .constraint("test:ExcludedConstraint", null)
            .build();
        RuleSet ruleSet = RuleSetBuilder.newInstance()
            .addConstraint(constraint)
            .addConstraint(excludedConstraint)
            .addGroup(group)
            .getRuleSet();
        RuleSelection ruleSelection = RuleSelection.builder()
            .groupId("group")
            .excludeConstraintId("test:ExcludedConstraint")
            .build();

        ruleExecutor.execute(ruleSet, ruleSelection);

        verify(visitor).visitConstraint(constraint, MAJOR, emptyMap());
        verify(visitor, never()).visitConstraint(excludedConstraint, MAJOR, emptyMap());
    }
}
