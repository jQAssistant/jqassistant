package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import org.snakeyaml.engine.v2.events.Event;

public abstract class ParseNode<E extends Event> {
    private E sourceEvent;

    public ParseNode(E event) {
        sourceEvent = event;
    }

    public E getEvent() {
        return sourceEvent;
    }
}
