package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import org.snakeyaml.engine.v2.events.NodeEvent;

public abstract class KeyNode<T extends NodeEvent> extends BaseNode<T> {
    private BaseNode<?> valueNode;

    protected KeyNode(T event, int o) {
        super(event, o);
    }

    public void setValue(BaseNode<?> node) {
        this.valueNode = node;
    }

    public BaseNode<?> getValue() {
        return valueNode;
    }
}
