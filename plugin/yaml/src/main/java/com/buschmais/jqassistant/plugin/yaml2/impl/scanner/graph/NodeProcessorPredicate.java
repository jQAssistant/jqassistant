package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import java.util.function.Predicate;

import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.BaseNode;

import org.snakeyaml.engine.v2.events.Event;

public interface NodeProcessorPredicate extends Predicate<BaseNode<? extends Event>> {
    boolean accepts(BaseNode<? extends Event> node);

    @Override
    default boolean test(BaseNode<? extends Event> node) {
        return accepts(node);
    }
}
