package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.rule.api.executor.RuleVisitor;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.XOException;

/**
 * A delegating visitor that provides a transactional context.
 */
public class TransactionalRuleVisitor implements RuleVisitor {

    private final RuleVisitor delegate;
    private final Store store;

    public TransactionalRuleVisitor(RuleVisitor delegate, Store store) {
        this.delegate = delegate;
        this.store = store;
    }

    @Override
    public void beforeRules() throws RuleException {
        doInXOTransaction(() -> delegate.beforeRules());
    }

    @Override
    public void afterRules() throws RuleException {
        doInXOTransaction(() -> delegate.afterRules());
    }

    @Override
    public boolean visitConcept(Concept concept, Severity effectiveSeverity) throws RuleException {
        return doInXOTransaction(() -> delegate.visitConcept(concept, effectiveSeverity));
    }

    @Override
    public void skipConcept(Concept concept, Severity effectiveSeverity) throws RuleException {
        doInXOTransaction(() -> delegate.skipConcept(concept, effectiveSeverity));
    }

    @Override
    public void visitConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleException {
        doInXOTransaction(() -> delegate.visitConstraint(constraint, effectiveSeverity));
    }

    @Override
    public void skipConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleException {
        doInXOTransaction(() -> delegate.skipConstraint(constraint, effectiveSeverity));
    }

    @Override
    public void beforeGroup(Group group, Severity effectiveSeverity) throws RuleException {
        doInXOTransaction(() -> delegate.beforeGroup(group, effectiveSeverity));
    }

    @Override
    public void afterGroup(Group group) throws RuleException {
        doInXOTransaction(() -> delegate.afterGroup(group));
    }

    private void doInXOTransaction(TransactionalAction transactionalAction) throws RuleException {
        doInXOTransaction((TransactionalSupplier<Void>) () -> {
            transactionalAction.execute();
            return null;
        });
    }

    /**
     * Executes a {@link TransactionalSupplier} within a transaction.
     *
     * @param txSupplier
     *            The {@link TransactionalSupplier}.
     * @param <T>
     *            The return type of the {@link TransactionalSupplier}.
     * @return The value provided by the {@link TransactionalSupplier}.
     * @throws RuleException
     *             If the transaction failed due to an underlying
     *             {@link XOException}.
     */
    private <T> T doInXOTransaction(TransactionalSupplier<T> txSupplier) throws RuleException {
        try {
            store.beginTransaction();
            T result = txSupplier.execute();
            store.commitTransaction();
            return result;
        } catch (RuleException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuleException("Caught unexpected exception from store.", e);
        } finally {
            if (store.hasActiveTransaction()) {
                store.rollbackTransaction();
            }
        }
    }

    /**
     * Defines a transactional action.
     */
    private interface TransactionalAction {
        void execute() throws RuleException;
    }

    /**
     * Defines a transactional supplier.
     */
    private interface TransactionalSupplier<T> {
        T execute() throws RuleException;
    }


}
