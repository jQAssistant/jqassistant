package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import org.snakeyaml.engine.v2.events.NodeEvent;

public class KeyNode extends BaseNode<NodeEvent> {
    private BaseNode<?> valueNode;

    public KeyNode(NodeEvent event) {
        super(event);
    }

    public void setValue(BaseNode<?> node) {
        this.valueNode = node;
    }

    public BaseNode<?> getValue() {
        return valueNode;
    }
}
