package com.buschmais.jqassistant.plugin.yaml2.impl.scanner.parsing;

import org.snakeyaml.engine.v2.events.NodeEvent;

// todo A key is actual also a node which can have an anchor
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

    // todo rename to getKeyValue or something like this
    public BaseNode<?> getKeyNode() {
        return keyNode;
    }

    public void setKeyNode(BaseNode<?> keyNode) {
        this.keyNode = keyNode;
    }
}
