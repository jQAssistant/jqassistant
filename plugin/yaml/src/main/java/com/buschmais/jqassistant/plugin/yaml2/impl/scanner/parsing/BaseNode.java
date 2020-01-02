package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import org.snakeyaml.engine.v2.events.Event;

public abstract class BaseNode<E extends Event>
    implements EventSupport<E> {
    private E sourceEvent;

    public BaseNode(E event) {
        sourceEvent = event;
    }

    public E getEvent() {
        return sourceEvent;
    }
}
