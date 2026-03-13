package com.buschmais.jqassistant.core.shared.transaction;

/**
 * Defines the interface for transactional actions.
 */
public interface Transactional {

    /**
     * Execute a transactional action.
     * @param transactionalAction The {@link TransactionalAction}.
     * @param <E> The execption type.
     * @throws E The execption type.
     */
    <E extends Exception> void requireTransaction(TransactionalAction<E> transactionalAction) throws E;

    /**
     * Execute a transactional supplier.
     * @param transactionalSupplier The {@link TransactionalAction}.
     * @param <E> The execption type.
     * @throws E The execption type.
     */
    <T, E extends Exception> T requireTransaction(TransactionalSupplier<T, E> transactionalSupplier) throws E;

    /**
     * Defines a transactional action.
     */
    interface TransactionalAction<E extends Exception> {
        void execute() throws E;
    }

    /**
     * Defines a transactional supplier.
     */
    interface TransactionalSupplier<T, E extends Exception> {
        T execute() throws E;
    }
}
