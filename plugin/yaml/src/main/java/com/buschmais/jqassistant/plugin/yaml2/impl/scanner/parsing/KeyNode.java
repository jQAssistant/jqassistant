package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import org.snakeyaml.engine.v2.events.ScalarEvent;

public class KeyNode extends ParseNode<ScalarEvent> {

    private ParseNode<?> node;

    public KeyNode(ScalarEvent event) {
        super(event);
    }

    public void setValue(ParseNode<?> node) {
        this.node = node;
    }

    public ParseNode<?> getValue() {
        return node;
    }
}
