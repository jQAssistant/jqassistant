package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.XOException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionContext {

    @Getter
    private final Store store;

    void requireTX(TransactionalAction transactionalAction, boolean transactional) throws RuleException {
        if (transactional) {
            requireTX(transactionalAction);
        } else {
            transactionalAction.execute();
        }
    }

    void requireTX(TransactionalAction transactionalAction) throws RuleException {
        requireTX((TransactionalSupplier<Void>) () -> {
            transactionalAction.execute();
            return null;
        });
    }

    <T> T requireTX(TransactionalSupplier<T> txSupplier, boolean transactional) throws RuleException {
        if (transactional) {
            return requireTX(txSupplier);
        } else {
            return txSupplier.execute();
        }
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
    <T> T requireTX(TransactionalSupplier<T> txSupplier) throws RuleException {
        if (store.hasActiveTransaction()) {
            return txSupplier.execute();
        }
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
     interface TransactionalAction {
        void execute() throws RuleException;
    }

    /**
     * Defines a transactional supplier.
     */
     interface TransactionalSupplier<T> {
        T execute() throws RuleException;
    }
}
