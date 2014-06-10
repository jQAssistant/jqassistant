package com.buschmais.jqassistant.core.scanner.api.iterable;

import java.util.Iterator;

public class AggregatingIterable<T> implements Iterable<T> {

    private Iterable<Iterable<? extends T>> delegates;

    public AggregatingIterable(Iterable<Iterable<? extends T>> delegates) {
        this.delegates = delegates;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private Iterator<Iterable<? extends T>> delegatesIterator = AggregatingIterable.this.delegates.iterator();

            private Iterator<? extends T> currentIterator = null;

            @Override
            public boolean hasNext() {
                while ((currentIterator == null || !currentIterator.hasNext()) && delegatesIterator.hasNext()) {
                    currentIterator = delegatesIterator.next().iterator();
                }
                return currentIterator != null && currentIterator.hasNext();
            }

            @Override
            public T next() {
                return currentIterator.next();
            }
        };
    }
}
