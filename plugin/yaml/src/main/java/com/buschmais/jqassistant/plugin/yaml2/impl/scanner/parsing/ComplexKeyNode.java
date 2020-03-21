package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import org.snakeyaml.engine.v2.events.NodeEvent;

public class ComplexKeyNode extends KeyNode {

    private BaseNode<?> valueNode;
    private BaseNode<?> keyNode;

    public ComplexKeyNode(NodeEvent event) {
        super(event);
    }

    public void setValue(BaseNode<?> node) {
        this.valueNode = node;
    }

    public BaseNode<?> getValue() {
        return valueNode;
    }

    public BaseNode<?> getKeyNode() {
        return keyNode;
    }

    public void setKeyNode(BaseNode<?> keyNode) {
        this.keyNode = keyNode;
    }
}
