package com.buschmais.jqassistant.core.scanner.api.iterable;

public class IterableConsumer<T> {

    private IterableConsumer() {
    }

    public interface Consumer<T> {

        void next(T t);

    }

    public static <T> void consume(Iterable<? extends T> iterable, Consumer<T> consumer) {
        for (T t : iterable) {
            consumer.next(t);
        }
    }
}
