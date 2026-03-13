package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.graph;

import java.util.function.Predicate;

import com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing.AbstractBaseNode;

interface NodeProcessorPredicate extends Predicate<AbstractBaseNode> {
    boolean accepts(AbstractBaseNode node);

    @Override
    default boolean test(AbstractBaseNode node) {
        return accepts(node);
    }
}
