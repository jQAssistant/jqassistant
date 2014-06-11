package com.buschmais.jqassistant.core.scanner.api.iterable;

import java.io.IOException;
import java.util.Iterator;

public abstract class MappingIterable<S, T> implements Iterable<T> {

    private final Iterable<? extends S> source;

    protected MappingIterable(Iterable<? extends S> source) {
        this.source = source;
    }

    @Override
    public Iterator<T> iterator() {
        final Iterator<? extends S> sourceIterator = source.iterator();
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return sourceIterator.hasNext();
            }

            @Override
            public T next() {
                S element = sourceIterator.next();
                try {
                    return MappingIterable.this.map(element);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }

    protected abstract T map(S element) throws IOException;
}
