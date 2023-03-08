package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.rule.api.executor.RuleVisitor;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.store.api.Store;

/**
 * A delegating visitor that provides a transactional context.
 */
public class TransactionalRuleVisitor implements RuleVisitor {

    private final RuleVisitor delegate;
    private final TransactionContext transactionContext;

    public TransactionalRuleVisitor(RuleVisitor delegate, Store store) {
        this.delegate = delegate;
        this.transactionContext = new TransactionContext(store);
    }

    @Override
    public void beforeRules() throws RuleException {
        transactionContext.requireTX(() -> delegate.beforeRules());
    }

    @Override
    public void afterRules() throws RuleException {
        transactionContext.requireTX(() -> delegate.afterRules());
    }

    @Override
    public boolean visitConcept(Concept concept, Severity effectiveSeverity) throws RuleException {
        return transactionContext.requireTX(() -> delegate.visitConcept(concept, effectiveSeverity), concept.getExecutable().isTransactional());
    }

    @Override
    public void skipConcept(Concept concept, Severity effectiveSeverity) throws RuleException {
        transactionContext.requireTX(() -> delegate.skipConcept(concept, effectiveSeverity));
    }

    @Override
    public void visitConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleException {
        transactionContext.requireTX(() -> delegate.visitConstraint(constraint, effectiveSeverity), constraint.getExecutable().isTransactional());
    }

    @Override
    public void skipConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleException {
        transactionContext.requireTX(() -> delegate.skipConstraint(constraint, effectiveSeverity));
    }

    @Override
    public void beforeGroup(Group group, Severity effectiveSeverity) throws RuleException {
        transactionContext.requireTX(() -> delegate.beforeGroup(group, effectiveSeverity));
    }

    @Override
    public void afterGroup(Group group) throws RuleException {
        transactionContext.requireTX(() -> delegate.afterGroup(group));
    }
}
