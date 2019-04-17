package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.rule.api.executor.RuleVisitor;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.XOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static com.buschmais.jqassistant.core.analysis.api.rule.Severity.MAJOR;
import static com.buschmais.jqassistant.core.analysis.api.rule.Severity.MINOR;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Verifies the functionality of the TransactionalRuleVisitor.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class TransactionalVisitorTest {

    @Mock
    private RuleVisitor delegate;

    @Mock
    private Store store;

    private boolean activeTransaction;

    private TransactionalRuleVisitor visitor;

    @BeforeEach
    public void setUp() {
        visitor = new TransactionalRuleVisitor(delegate, store);
        doAnswer(invocation -> activeTransaction = true).when(store).beginTransaction();
        doAnswer(invocation -> activeTransaction = false).when(store).commitTransaction();
        doAnswer(invocation -> activeTransaction = false).when(store).rollbackTransaction();
        doAnswer(invocation -> activeTransaction).when(store).hasActiveTransaction();
    }

    @Test
    public void beforeRules() throws RuleException {
        visitor.beforeRules();

        verify(delegate).beforeRules();
        verifySuccessfulTransaction();
    }

    @Test
    public void afterRules() throws RuleException {
        visitor.afterRules();

        verify(delegate).afterRules();
        verifySuccessfulTransaction();
    }

    @Test
    public void beforeGroup() throws RuleException {
        Group group = mock(Group.class);

        visitor.beforeGroup(group, MAJOR);

        verify(delegate).beforeGroup(group, MAJOR);
        verifySuccessfulTransaction();
    }

    @Test
    public void afterGroup() throws RuleException {
        Group group = mock(Group.class);

        visitor.afterGroup(group);

        verify(delegate).afterGroup(group);
        verifySuccessfulTransaction();
    }

    @Test
    public void visitConcept() throws RuleException {
        Concept concept = mock(Concept.class);

        visitor.visitConcept(concept, MINOR);

        verify(delegate).visitConcept(concept, MINOR);
        verifySuccessfulTransaction();
    }

    @Test
    public void skipConcept() throws RuleException {
        Concept concept = mock(Concept.class);

        visitor.skipConcept(concept, MINOR);

        verify(delegate).skipConcept(concept, MINOR);
        verifySuccessfulTransaction();
    }

    @Test
    public void visitConstraint() throws RuleException {
        Constraint constraint = mock(Constraint.class);

        visitor.visitConstraint(constraint, MINOR);

        verify(delegate).visitConstraint(constraint, MINOR);
        verifySuccessfulTransaction();
    }

    @Test
    public void skipConstraint() throws RuleException {
        Constraint constraint = mock(Constraint.class);

        visitor.skipConstraint(constraint, MAJOR);

        verify(delegate).skipConstraint(constraint, MAJOR);
        verifySuccessfulTransaction();
    }

    @Test
    public void ruleException() throws RuleException {
        Constraint constraint = mock(Constraint.class);
        doThrow(new RuleException("Test")).when(delegate).visitConstraint(constraint, MAJOR);

        try {
            visitor.visitConstraint(constraint, MAJOR);
            fail("Expecting a " + RuleException.class);
        } catch (RuleException e) {
            verify(delegate).visitConstraint(constraint, MAJOR);
            verifyFailedTransaction();
        }
    }

    @Test
    public void runtimeException() throws RuleException {
        Constraint constraint = mock(Constraint.class);
        doThrow(new NullPointerException()).when(delegate).visitConstraint(constraint, MAJOR);

        try {
            visitor.visitConstraint(constraint, MAJOR);
            fail("Expecting a " + XOException.class);
        } catch (RuleException e) {
            verify(delegate).visitConstraint(constraint, MAJOR);
            verifyFailedTransaction();
        }
    }

    private void verifySuccessfulTransaction() {
        verify(store).beginTransaction();
        verify(store).commitTransaction();
        verify(store, never()).rollbackTransaction();
    }

    private void verifyFailedTransaction() {
        verify(store).beginTransaction();
        verify(store).rollbackTransaction();
        verify(store, never()).commitTransaction();
    }
}
